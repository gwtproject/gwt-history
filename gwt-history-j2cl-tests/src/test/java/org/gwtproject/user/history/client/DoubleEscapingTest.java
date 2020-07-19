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

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.promise.Promise;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;
import org.junit.Test;

/** Tests for the history system. */
@J2clTestInput(DoubleEscapingTest.class)
public class DoubleEscapingTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  /* Tests against issue #572: Double unescaping of history tokens. */
  @Test(timeout = 5000)
  public Promise<Void> testDoubleEscaping() {
    return new Promise<>(
        (resolve, reject) -> {
          final String escToken = "%24%24%24";
          timer =
              new Timer() {
                @Override
                public void run() {
                  History.newItem(escToken);
                }
              };
          addHistoryListenerImpl(
              event -> {
                assertEquals(escToken, event.getValue());
                resolve.onInvoke((Void) null);
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
