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

/**
 * Tests for the history system.
 *
 * <p>TODO: find a way to test unescaping of the initial hash value.
 */
@J2clTestInput(HistoryChangeCountTest.class)
public class HistoryChangeCountTest {

  private HandlerRegistration handlerRegistration;

  private Timer timer;

  /**
   * Verify that {@link ValueChangeHandler#onValueChange(ValueChangeEvent)} is only called onceper
   * {@link History#newItem(String)}.
   *
   * @return
   */
  @Test(timeout = 5000)
  public Promise<Object> testHistoryChangedCount() {
    return new Promise<>(
        (resolve, reject) -> {
          timer =
              new Timer() {

                private int count = 0;

                @Override
                public void run() {
                  if (count++ == 0) {
                    // verify that duplicates don't issue another event
                    History.newItem("testHistoryChangedCount");
                    timer.schedule(500);
                  } else {
                    resolve.onInvoke((Void) null);
                  }
                }
              };
          addHistoryListenerImpl(
              new ValueChangeHandler<String>() {

                private int count = 0;

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                  if (count++ != 0) {
                    fail("onHistoryChanged called multiple times");
                  }
                  // wait 500ms to see if we get called multiple times
                  timer.schedule(500);
                }
              });

          History.newItem("testHistoryChangedCount");
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
