/*
 * Copyright 2020 The GWT Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gwtproject.user.history.client;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.gwtproject.user.history.client.HistoryTest.TEST_CLICK_LINK_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_DOUBLE_ESCAPING_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_EMPTY_HISTORY_TOKENS_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_HISTORY_CHANGED_COUNT_DELAY;
import static org.gwtproject.user.history.client.HistoryTest.TEST_HISTORY_CHANGED_COUNT_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_HISTORY_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_NO_DOUBLE_TOKEN_UNESCAPING_DELAY;
import static org.gwtproject.user.history.client.HistoryTest.TEST_NO_DOUBLE_TOKEN_UNESCAPING_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_NO_EVENTS_DELAY;
import static org.gwtproject.user.history.client.HistoryTest.TEST_NO_EVENTS_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_REPLACE_ITEM_NO_EVENT_DELAY;
import static org.gwtproject.user.history.client.HistoryTest.TEST_REPLACE_ITEM_NO_EVENT_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_REPLACE_ITEM_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_TOKEN_ESCAPING_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_TOKEN_NONESCAPING_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.TEST_TOKEN_WITH_QUESTIONMARK_TIMEOUT;
import static org.gwtproject.user.history.client.HistoryTest.action_testClickLink;
import static org.gwtproject.user.history.client.HistoryTest.action_testDoubleEscaping;
import static org.gwtproject.user.history.client.HistoryTest.action_testEmptyHistoryToken;
import static org.gwtproject.user.history.client.HistoryTest.action_testEmptyHistoryTokens;
import static org.gwtproject.user.history.client.HistoryTest.action_testHistory;
import static org.gwtproject.user.history.client.HistoryTest.action_testNoEvents;
import static org.gwtproject.user.history.client.HistoryTest.action_testReplaceItem;
import static org.gwtproject.user.history.client.HistoryTest.action_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.action_testTokenEscaping;
import static org.gwtproject.user.history.client.HistoryTest.action_testTokenNonescaping;
import static org.gwtproject.user.history.client.HistoryTest.action_testTokenWithQuestionmark;
import static org.gwtproject.user.history.client.HistoryTest.setup_testClickLink;
import static org.gwtproject.user.history.client.HistoryTest.setup_testHistory;
import static org.gwtproject.user.history.client.HistoryTest.setup_testNoDoubleTokenUnEscaping;
import static org.gwtproject.user.history.client.HistoryTest.setup_testReplaceItem;
import static org.gwtproject.user.history.client.HistoryTest.setup_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.start_testHistoryChangedCount;
import static org.gwtproject.user.history.client.HistoryTest.teardown_testClickLink;
import static org.gwtproject.user.history.client.HistoryTest.timer_testHistoryChangedCount;
import static org.gwtproject.user.history.client.HistoryTest.timer_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.verify_testClickLink;
import static org.gwtproject.user.history.client.HistoryTest.verify_testDoubleEscaping;
import static org.gwtproject.user.history.client.HistoryTest.verify_testEmptyHistoryToken;
import static org.gwtproject.user.history.client.HistoryTest.verify_testEmptyHistoryTokens;
import static org.gwtproject.user.history.client.HistoryTest.verify_testHistory;
import static org.gwtproject.user.history.client.HistoryTest.verify_testHistoryChangedCount;
import static org.gwtproject.user.history.client.HistoryTest.verify_testNoDoubleTokenUnEscaping;
import static org.gwtproject.user.history.client.HistoryTest.verify_testNoEvents;
import static org.gwtproject.user.history.client.HistoryTest.verify_testReplaceItem;
import static org.gwtproject.user.history.client.HistoryTest.verify_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.verify_testTokenEscaping;
import static org.gwtproject.user.history.client.HistoryTest.verify_testTokenNonescaping;
import static org.gwtproject.user.history.client.HistoryTest.verify_testTokenWithQuestionmark;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.client.GWTTestCase;
import elemental2.dom.DomGlobal;
import elemental2.dom.DomGlobal.SetTimeoutCallbackFn;
import elemental2.dom.Element;
import java.util.ArrayList;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;

public class HistoryGWTTest extends GWTTestCase {
  protected Double timer;
  private HandlerRegistration handlerRegistration;

  // TODO(dankurka): Fix up HTML unit hash change handling
  @DoNotRunWith(Platform.HtmlUnitUnknown)
  public void testClickLink() {
    Element anchorElement = setup_testClickLink();
    try {
      addHistoryListenerImpl(
          event -> {
            verify_testClickLink(event);
            finishTest();
          });

      delayTestFinish(TEST_CLICK_LINK_TIMEOUT);

      action_testClickLink(anchorElement);
    } finally {
      teardown_testClickLink(anchorElement);
    }
  }

  public void testDoubleEscaping() {
    delayTestFinish(TEST_DOUBLE_ESCAPING_TIMEOUT);
    addHistoryListenerImpl(
        event -> {
          verify_testDoubleEscaping(event);
          finishTest();
        });
    action_testDoubleEscaping();
  }

  public void testEmptyHistoryTokens() {
    delayTestFinish(TEST_EMPTY_HISTORY_TOKENS_TIMEOUT);
    addHistoryListenerImpl(
        event -> {
          boolean finished = verify_testEmptyHistoryTokens(event);
          if (finished) {
            finishTest();
          }
        });
    action_testEmptyHistoryTokens();
  }

  public void testNoEvents() {
    delayTestFinish(TEST_NO_EVENTS_TIMEOUT);

    addHistoryListenerImpl(event -> verify_testNoEvents());

    action_testNoEvents();

    timer = setTimeout((Object... p0) -> finishTest(), TEST_NO_EVENTS_DELAY);
  }

  @DoNotRunWith(Platform.HtmlUnitUnknown)
  public void testHistory() {
    setup_testHistory();

    delayTestFinish(TEST_HISTORY_TIMEOUT);

    addHistoryListenerImpl(
        new ValueChangeHandler<String>() {

          private int state = 0;

          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            boolean finished = verify_testHistory(event, state++);
            if (finished) {
              finishTest();
            }
          }
        });
    action_testHistory();
  }

  public void testHistoryChangedCount() {
    delayTestFinish(TEST_HISTORY_CHANGED_COUNT_TIMEOUT);
    final SetTimeoutCallbackFn callback =
        new SetTimeoutCallbackFn() {
          private int count = 0;

          @Override
          public void onInvoke(Object... p0) {
            boolean finished = timer_testHistoryChangedCount(count++);
            if (finished) {
              finishTest();
            } else {
              timer = setTimeout(this, TEST_HISTORY_CHANGED_COUNT_DELAY);
            }
          }
        };

    addHistoryListenerImpl(
        new ValueChangeHandler<String>() {
          private int count = 0;

          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            boolean failed = verify_testHistoryChangedCount(count++);
            if (!failed) {
              timer = setTimeout(callback, 500);
            }
          }
        });

    start_testHistoryChangedCount();
  }

  @DoNotRunWith(Platform.HtmlUnitUnknown)
  public void testReplaceItem() {
    setup_testReplaceItem();

    delayTestFinish(TEST_REPLACE_ITEM_TIMEOUT);

    addHistoryListenerImpl(
        new ValueChangeHandler<String>() {

          private int state = 0;

          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            boolean finished = verify_testReplaceItem(event, state++);
            if (finished) {
              finishTest();
            }
          }
        });

    action_testReplaceItem();
  }

  @DoNotRunWith(Platform.HtmlUnitBug)
  public void testReplaceItemNoEvent() {
    setup_testReplaceItemNoEvent();

    addHistoryListenerImpl(event -> verify_testReplaceItemNoEvent());

    delayTestFinish(TEST_REPLACE_ITEM_NO_EVENT_TIMEOUT);

    action_testReplaceItemNoEvent();

    timer =
        setTimeout(
            (Object... p0) -> {
              timer_testReplaceItemNoEvent();
              finishTest();
            },
            TEST_REPLACE_ITEM_NO_EVENT_DELAY);
  }

  public void testTokenEscaping() {
    delayTestFinish(TEST_TOKEN_ESCAPING_TIMEOUT);
    addHistoryListenerImpl(
        event -> {
          verify_testTokenEscaping(event);
          finishTest();
        });
    action_testTokenEscaping();
  }

  @DoNotRunWith(Platform.HtmlUnitUnknown)
  public void testNoDoubleTokenUnEscaping() {
    delayTestFinish(TEST_NO_DOUBLE_TOKEN_UNESCAPING_TIMEOUT);

    setup_testNoDoubleTokenUnEscaping();
    // allow browser to update the url
    timer =
        setTimeout(
            (Object... p0) -> {
              verify_testNoDoubleTokenUnEscaping();
              finishTest();
            },
            TEST_NO_DOUBLE_TOKEN_UNESCAPING_DELAY);
  }

  @DoNotRunWith(Platform.HtmlUnitBug)
  public void testTokenNonescaping() {
    delayTestFinish(TEST_TOKEN_NONESCAPING_TIMEOUT);

    addHistoryListenerImpl(
        event -> {
          verify_testTokenNonescaping(event);
          finishTest();
        });

    action_testTokenNonescaping();
  }

  public void testTokenWithQuestionmark() {
    delayTestFinish(TEST_TOKEN_WITH_QUESTIONMARK_TIMEOUT);

    addHistoryListenerImpl(
        event -> {
          verify_testTokenWithQuestionmark(event);
          finishTest();
        });
    action_testTokenWithQuestionmark();
  }

  @DoNotRunWith(Platform.HtmlUnitBug)
  public void testEmptyHistoryToken() {
    final ArrayList<Object> counter = new ArrayList<>();

    addHistoryListenerImpl(
        event -> {
          verify_testEmptyHistoryToken(counter);
        });

    action_testEmptyHistoryToken(counter);
  }

  @Override
  public String getModuleName() {
    return "org.gwtproject.user.history.History";
  }

  @Override
  protected void gwtTearDown() throws Exception {
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
      handlerRegistration = null;
    }
    if (timer != null) {
      DomGlobal.clearTimeout(timer);
      timer = null;
    }
  }

  protected void addHistoryListenerImpl(ValueChangeHandler<String> handler) {
    this.handlerRegistration = History.addValueChangeHandler(handler);
  }
}
