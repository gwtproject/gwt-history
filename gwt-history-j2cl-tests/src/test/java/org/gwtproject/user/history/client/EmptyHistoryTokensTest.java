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
import static org.junit.Assert.fail;

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.promise.Promise;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;
import org.junit.Test;

/** Tests for the history system. */
@J2clTestInput(EmptyHistoryTokensTest.class)
public class EmptyHistoryTokensTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  /*
   * Tests against issue #879: Ensure that empty history tokens do not add
   * additional characters after the '#' symbol in the URL.
   */
  @Test(timeout = 5000)
  public Promise<Void> testEmptyHistoryTokens() {
    // We must first start out with a non-blank history token. Adding a blank
    // history token in the initial state will not cause an onHistoryChanged
    // event to fire.
    return new Promise<>(
        (resolve, reject) -> {
          timer =
              new Timer() {

                @Override
                public void run() {
                  History.newItem("foobar");
                }
              };
          addHistoryListenerImpl(
              event -> {
                String historyToken = event.getValue();
                if (historyToken == null) {
                  fail("historyToken should not be null");
                }

                if (historyToken.equals("foobar")) {
                  History.newItem("");
                } else {
                  assertEquals("", historyToken);
                  resolve.onInvoke((Void) null);
                }
              });
          timer.scheduleRepeating(500);
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
