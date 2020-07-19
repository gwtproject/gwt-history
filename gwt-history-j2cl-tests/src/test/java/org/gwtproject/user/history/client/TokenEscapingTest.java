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
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.timer.client.Timer;
import org.junit.After;
import org.junit.Test;

/** Tests for the history system. */
@J2clTestInput(TokenEscapingTest.class)
public class TokenEscapingTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  private void addHistoryListenerImpl(ValueChangeHandler<String> handler) {
    this.handlerRegistration = History.addValueChangeHandler(handler);
  }

  /** Verify that no events are issued via newItem if there were not reqeuested. */
  @Test(timeout = 5000)
  public Promise<Void> testTokenEscaping() {
    return new Promise<>(
        (resolve, reject) -> {
          final String shouldBeEncoded = "% ^[]|\"<>{}\\";
          final String shouldBeEncodedAs = "%25%20%5E%5B%5D%7C%22%3C%3E%7B%7D%5C";

          addHistoryListenerImpl(
              event -> {
                assertEquals(shouldBeEncodedAs, getCurrentLocationHash());
                assertEquals(shouldBeEncoded, event.getValue());
                resolve.onInvoke((Void) null);
              });
          timer =
              new Timer() {

                @Override
                public void run() {
                  History.newItem(shouldBeEncoded);
                }
              };
          timer.schedule(500);
        });
  }

  private static String getCurrentLocationHash() {
    String hash = DomGlobal.location.hash;
    if (hash.isEmpty()) {
      fail("can not read history token");
    }
    return hash.substring(1);
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
