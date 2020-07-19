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

import elemental2.promise.Promise;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;

/** Tests for the history system. */
// TODO (El Hoss):
//  Test does not work using hte plugin. Running the test inside a brwoser works ...
//
// @J2clTestInput(NoDoubleTokenUnEscapingTest.class)
public class NoDoubleTokenUnEscapingTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  private void addHistoryListenerImpl(ValueChangeHandler<String> handler) {
    this.handlerRegistration = History.addValueChangeHandler(handler);
  }

  /** Verify that no events are issued via newItem if there were not reqeuested. */
  //  @Test(timeout = 5000)
  public Promise<Void> testNoDoubleTokenUnEscaping() {
    return new Promise<>(
        (resolve, reject) -> {
          final String shouldBeEncoded = "abc%20abc";

          History.newItem(shouldBeEncoded);
          History.newItem("someOtherToken");
          History.back();
          // allow browser to update the url
          timer =
              new Timer() {
                @Override
                public void run() {
                  // make sure that value in url actually matches the original token
                  assertEquals(shouldBeEncoded, History.getToken());
                  resolve.onInvoke((Void) null);
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
