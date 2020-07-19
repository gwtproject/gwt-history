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

import static org.junit.Assert.*;

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.promise.Promise;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the history system.
 *
 * <p>TODO: find a way to test unescaping of the initial hash value.
 */
@J2clTestInput(TokenWithQuestionmarkTest.class)
public class TokenWithQuestionmarkTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  /**
   * Test that using an empty history token works properly. There have been problems (see issue
   * 2905) with this in the past on Safari.
   *
   * <p>Seems like a HtmlUnit bug. Need more investigation.
   */
  @Test(timeout = 5000)
  public Promise<Void> testTokenWithQuestionmark() {
    return new Promise<>(
        (resolve, reject) -> {
          final String token = "foo?bar";
          timer =
              new Timer() {

                @Override
                public void run() {
                  History.newItem(token);
                }
              };
          addHistoryListenerImpl(
              event -> {
                String historyToken = event.getValue();
                if (historyToken == null) {
                  fail("historyToken should not be null");
                }
                assertEquals(token, historyToken);
                timer.cancel();
                resolve.onInvoke((Void) null);
              });
          History.newItem(token);
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
