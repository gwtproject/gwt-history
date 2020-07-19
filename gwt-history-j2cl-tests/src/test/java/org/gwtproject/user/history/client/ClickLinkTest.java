/*
 * Copyright 2008 The GWT Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.user.history.client;

import static org.junit.Assert.assertEquals;

import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.promise.Promise;
import jsinterop.base.Js;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;
import org.junit.Test;

/** Tests for the history system. */
// @J2clTestInput(ClickLinkTest.class)
public class ClickLinkTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  /* Tests against issue #572: Double unescaping of history tokens. */
  @Test(timeout = 5000)
  public Promise<Void> testClickLink() {
    return new Promise<>(
        (resolve, reject) -> {
          HTMLAnchorElement anchorElement = Js.cast(DomGlobal.document.createElement("a"));
          anchorElement.href = "#href1";
          DomGlobal.document.body.appendChild(anchorElement);

          CustomEvent<String> clickEvent = new CustomEvent<>("click");

          //            NativeEvent clickEvent =
          //                DomGlobal.document.create.createEvent(0, 0, 0, 0, 0, false, false,
          // false, false);
          //
          timer =
              new Timer() {

                @Override
                public void run() {
                  anchorElement.dispatchEvent(clickEvent);
                }
              };

          try {
            History.newItem("something_as_base");

            addHistoryListenerImpl(
                event -> {
                  assertEquals("href1", event.getValue());
                  resolve.onInvoke((Void) null);
                });
            timer.schedule(1000);
          } finally {
            DomGlobal.document.body.removeChild(anchorElement);
          }
        });
  }

  private void addHistoryListenerImpl(ValueChangeHandler<String> handler) {
    this.handlerRegistration = History.addValueChangeHandler(handler);
  }

  @After
  public void gwtTearDown() throws Exception {
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
      handlerRegistration = null;
    }
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }
}
