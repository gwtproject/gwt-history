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

import static org.junit.Assert.fail;

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.promise.Promise;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;
import org.junit.Test;

/** Tests for the history system. */
@J2clTestInput(ReplaceItemTest.class)
public class ReplaceItemTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  private void addHistoryListenerImpl(ValueChangeHandler<String> handler) {
    this.handlerRegistration = History.addValueChangeHandler(handler);
  }

  /** Verify that no events are issued via newItem if there were not reqeuested. */
  @Test(timeout = 5000)
  public Promise<Void> testReplaceItem() {
    return new Promise<>(
        (resolve, reject) -> {
          /*
           * Sentinel token which should only be seen if tokens are lost during the rest of the test.
           * Without this, History.back() might send the browser too far back, i.e. back to before the web
           * app containing our test module.
           */
          History.newItem("if-you-see-this-then-history-went-back-too-far");

          final String historyToken1 = "token1";
          final String historyToken2 = "token 2";
          final String historyToken3 = "token3";

          addHistoryListenerImpl(
              new ValueChangeHandler<String>() {

                private int state = 0;

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                  String historyToken = event.getValue();
                  switch (state) {
                    case 0:
                      {
                        if (!historyToken.equals(historyToken1)) {
                          fail("Expecting token '" + historyToken1 + "', but got: " + historyToken);
                        }

                        state = 1;
                        History.newItem(historyToken2);
                        break;
                      }

                    case 1:
                      {
                        if (!historyToken.equals(historyToken2)) {
                          fail("Expecting token '" + historyToken2 + "', but got: " + historyToken);
                        }

                        state = 2;
                        History.replaceItem(historyToken3, true);
                        break;
                      }

                    case 2:
                      {
                        if (!historyToken.equals(historyToken3)) {
                          fail("Expecting token '" + historyToken3 + "', but got: " + historyToken);
                        }
                        state = 3;
                        History.back();
                        break;
                      }

                    case 3:
                      {
                        if (!historyToken.equals(historyToken1)) {
                          fail("Expecting token '" + historyToken1 + "', but got: " + historyToken);
                        }
                        resolve.onInvoke((Void) null);
                      }
                  }
                }
              });

          History.newItem(historyToken1);
          timer =
              new Timer() {

                @Override
                public void run() {
                  History.newItem(historyToken1);
                }
              };
          timer.schedule(500);
        });
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
