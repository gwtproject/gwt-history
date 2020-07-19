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
import static org.junit.Assert.assertFalse;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import java.util.ArrayList;
import java.util.List;
import jsinterop.base.Js;
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
// @J2clTestInput(HistoryTest.class)
public class EmptyHistoryTokenTest {

  private Timer timer;

  private HandlerRegistration handlerRegistration;

  /**
   * Test that using an empty history token works properly. There have been problems (see issue
   * 2905) with this in the past on Safari.
   *
   * <p>Seems like a HtmlUnit bug. Need more investigation.
   */
  @Test
  public void testEmptyHistoryToken() {
    final List<Object> counter = new ArrayList<>();

    addHistoryListenerImpl(
        event -> {
          counter.add(new Object());
          assertFalse("Browser is borked by empty history token", isBorked());
        });

    History.newItem("x");
    History.newItem("");

    assertEquals("Expected two history events", 2, counter.size());
  }

  // Used by testEmptyHistoryToken() to catch a bizarre failure mode on Safari.
  private static boolean isBorked() {
    HTMLDivElement e = Js.cast(DomGlobal.document.createElement("div"));
    e.innerHTML = "string";
    return e.innerHTML.length() == 0;
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
