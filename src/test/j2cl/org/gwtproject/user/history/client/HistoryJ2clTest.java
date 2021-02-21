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
import static org.gwtproject.user.history.client.HistoryTest.action_testEmptyHistoryToken;
import static org.gwtproject.user.history.client.HistoryTest.action_testNoEvents;
import static org.gwtproject.user.history.client.HistoryTest.action_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.setup_testClickLink;
import static org.gwtproject.user.history.client.HistoryTest.setup_testHistory;
import static org.gwtproject.user.history.client.HistoryTest.setup_testNoDoubleTokenUnEscaping;
import static org.gwtproject.user.history.client.HistoryTest.setup_testReplaceItem;
import static org.gwtproject.user.history.client.HistoryTest.setup_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.start_testHistoryChangedCount;
import static org.gwtproject.user.history.client.HistoryTest.teardown_testClickLink;
import static org.gwtproject.user.history.client.HistoryTest.timer_testHistoryChangedCount;
import static org.gwtproject.user.history.client.HistoryTest.timer_testReplaceItemNoEvent;
import static org.gwtproject.user.history.client.HistoryTest.verify_testEmptyHistoryToken;
import static org.gwtproject.user.history.client.HistoryTest.verify_testHistory;
import static org.gwtproject.user.history.client.HistoryTest.verify_testHistoryChangedCount;
import static org.gwtproject.user.history.client.HistoryTest.verify_testNoDoubleTokenUnEscaping;
import static org.gwtproject.user.history.client.HistoryTest.verify_testNoEvents;
import static org.gwtproject.user.history.client.HistoryTest.verify_testReplaceItem;
import static org.gwtproject.user.history.client.HistoryTest.verify_testReplaceItemNoEvent;

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.dom.DomGlobal;
import elemental2.dom.DomGlobal.SetTimeoutCallbackFn;
import elemental2.dom.Element;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;
import java.util.ArrayList;
import java.util.function.Predicate;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.junit.After;
import org.junit.Test;

@J2clTestInput(HistoryJ2clTest.class)
public class HistoryJ2clTest {

  private static boolean isHtmlUnit() {
    return "htmlunit".equals(System.getProperty("test.webdriver", "htmlunit"));
  }

  private static boolean isNoopTokenEncoder() {
    return "true".equals(System.getProperty("gwt.history.noDoubleEncoding", null));
  }

  private HandlerRegistration handlerRegistration;
  private Double timer;

  @After
  public void tearDown() {
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
      handlerRegistration = null;
    }
    if (timer != null) {
      DomGlobal.clearTimeout(timer);
      timer = null;
    }
  }

  private void addHistoryListenerImpl(ValueChangeHandler<String> handler) {
    handlerRegistration = History.addValueChangeHandler(handler);
  }

  @SuppressWarnings("overloads")
  private void addHistoryListenerImpl(
      Predicate<ValueChangeEvent<String>> handler,
      ResolveCallbackFn<Void> resolve,
      RejectCallbackFn reject) {
    addHistoryListenerImpl(wrapHandler(handler, resolve, reject));
  }

  @SuppressWarnings("overloads")
  private void addHistoryListenerImpl(
      ValueChangeHandler<String> handler,
      ResolveCallbackFn<Void> resolve,
      RejectCallbackFn reject) {
    addHistoryListenerImpl(wrapHandler(handler, resolve, reject));
  }

  @SuppressWarnings("overloads")
  private ValueChangeHandler<String> wrapHandler(
      ValueChangeHandler<String> handler,
      ResolveCallbackFn<Void> resolve,
      RejectCallbackFn reject) {
    return event -> {
      try {
        handler.onValueChange(event);
        resolve.onInvoke((Void) null);
      } catch (Throwable throwable) {
        reject.onInvoke(throwable);
      }
    };
  }

  @SuppressWarnings("overloads")
  private ValueChangeHandler<String> wrapHandler(
      Predicate<ValueChangeEvent<String>> handler,
      ResolveCallbackFn<Void> resolve,
      RejectCallbackFn reject) {
    return event -> {
      try {
        boolean finished = handler.test(event);
        if (finished) {
          resolve.onInvoke((Void) null);
        }
      } catch (Throwable throwable) {
        reject.onInvoke(throwable);
      }
    };
  }

  @SuppressWarnings("overloads")
  private Promise<Void> withHandler(ValueChangeHandler<String> handler, Runnable action) {
    return new Promise<>(
        (resolve, reject) -> {
          try {
            addHistoryListenerImpl(handler, resolve, reject);
            action.run();
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          }
        });
  }

  @SuppressWarnings("overloads")
  private Promise<Void> withHandler(Predicate<ValueChangeEvent<String>> handler, Runnable action) {
    return new Promise<>(
        (resolve, reject) -> {
          try {
            addHistoryListenerImpl(handler, resolve, reject);
            action.run();
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          }
        });
  }

  @Test(timeout = TEST_CLICK_LINK_TIMEOUT)
  public Promise<Void> testClickLink() {
    if (isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    Element anchorElement = setup_testClickLink();

    return new Promise<>(
        (resolve, reject) -> {
          try {
            addHistoryListenerImpl(HistoryTest::verify_testClickLink, resolve, reject);

            action_testClickLink(anchorElement);
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          } finally {
            teardown_testClickLink(anchorElement);
          }
        });
  }

  @Test(timeout = TEST_DOUBLE_ESCAPING_TIMEOUT)
  public Promise<Void> testDoubleEscaping() {
    return withHandler(
        HistoryTest::verify_testDoubleEscaping, HistoryTest::action_testDoubleEscaping);
  }

  @Test(timeout = TEST_EMPTY_HISTORY_TOKENS_TIMEOUT)
  public Promise<Void> testEmptyHistoryTokens() {
    if (isNoopTokenEncoder() && isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    return withHandler(
        HistoryTest::verify_testEmptyHistoryTokens, HistoryTest::action_testEmptyHistoryTokens);
  }

  @Test(timeout = TEST_NO_EVENTS_TIMEOUT)
  public Promise<Void> testNoEvents() {
    return new Promise<>(
        (resolve, reject) -> {
          try {
            addHistoryListenerImpl(
                event -> {
                  verify_testNoEvents();
                },
                resolve,
                reject);

            action_testNoEvents();
            setTimeout((Object... p0) -> resolve.onInvoke((Void) null), TEST_NO_EVENTS_DELAY);
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          }
        });
  }

  @Test(timeout = TEST_HISTORY_TIMEOUT)
  public Promise<Void> testHistory() {
    if (isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    setup_testHistory();

    return withHandler(
        new Predicate<ValueChangeEvent<String>>() {
          int state = 0;

          @Override
          public boolean test(ValueChangeEvent<String> event) {
            return verify_testHistory(event, state++);
          }
        },
        HistoryTest::action_testHistory);
  }

  @Test(timeout = TEST_HISTORY_CHANGED_COUNT_TIMEOUT)
  public Promise<Void> testHistoryChangedCount() {
    return new Promise<>(
        (resolve, reject) -> {
          final SetTimeoutCallbackFn callback =
              new SetTimeoutCallbackFn() {
                private int count = 0;

                @Override
                public void onInvoke(Object... p0) {
                  try {
                    boolean finished = timer_testHistoryChangedCount(count++);
                    if (finished) {
                      resolve.onInvoke((Void) null);
                    } else {
                      timer = setTimeout(this, TEST_HISTORY_CHANGED_COUNT_DELAY);
                    }
                  } catch (Throwable throwable) {
                    reject.onInvoke(throwable);
                  }
                }
              };

          try {
            addHistoryListenerImpl(
                new ValueChangeHandler<String>() {
                  private int count = 0;

                  @Override
                  public void onValueChange(ValueChangeEvent<String> event) {
                    try {
                      boolean failed = verify_testHistoryChangedCount(count++);
                      if (!failed) {
                        timer = setTimeout(callback, 500);
                      }
                    } catch (Throwable throwable) {
                      reject.onInvoke(throwable);
                    }
                  }
                });

            start_testHistoryChangedCount();
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          }
        });
  }

  @Test(timeout = TEST_REPLACE_ITEM_TIMEOUT)
  public Promise<Void> testReplaceItem() {
    if (isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    setup_testReplaceItem();

    return withHandler(
        new Predicate<ValueChangeEvent<String>>() {
          int state = 0;

          @Override
          public boolean test(ValueChangeEvent<String> event) {
            return verify_testReplaceItem(event, state++);
          }
        },
        HistoryTest::action_testReplaceItem);
  }

  @Test(timeout = TEST_REPLACE_ITEM_NO_EVENT_TIMEOUT)
  public Promise<Void> testReplaceItemNoEvent() {
    if (isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    setup_testReplaceItemNoEvent();

    return new Promise<>(
        (resolve, reject) -> {
          try {
            addHistoryListenerImpl(event -> verify_testReplaceItemNoEvent());

            action_testReplaceItemNoEvent();

            timer =
                setTimeout(
                    (Object... p0) -> {
                      try {
                        timer_testReplaceItemNoEvent();
                        resolve.onInvoke((Void) null);
                      } catch (Throwable throwable) {
                        reject.onInvoke(throwable);
                      }
                    },
                    TEST_REPLACE_ITEM_NO_EVENT_DELAY);
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          }
        });
  }

  @Test(timeout = TEST_TOKEN_ESCAPING_TIMEOUT)
  public Promise<Void> testTokenEscaping() {
    if (isNoopTokenEncoder()) {
      // FIXME: test is broken in all browsers
      return Promise.resolve((Void) null);
    }

    return withHandler(
        HistoryTest::verify_testTokenEscaping, HistoryTest::action_testTokenEscaping);
  }

  @Test(timeout = TEST_NO_DOUBLE_TOKEN_UNESCAPING_TIMEOUT)
  public Promise<Void> testNoDoubleTokenUnEscaping() {
    if (isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    setup_testNoDoubleTokenUnEscaping();

    return new Promise<>(
        (resolve, reject) -> {
          try {
            // allow browser to update the url
            timer =
                setTimeout(
                    (Object... p0) -> {
                      try {
                        verify_testNoDoubleTokenUnEscaping();
                        resolve.onInvoke((Void) null);
                      } catch (Throwable throwable) {
                        reject.onInvoke(throwable);
                      }
                    },
                    TEST_NO_DOUBLE_TOKEN_UNESCAPING_DELAY);
          } catch (Throwable throwable) {
            reject.onInvoke(throwable);
          }
        });
  }

  @Test(timeout = TEST_TOKEN_NONESCAPING_TIMEOUT)
  public Promise<Void> testTokenNonescaping() {
    if (isHtmlUnit()) {
      return Promise.resolve((Void) null);
    }

    return withHandler(
        HistoryTest::verify_testTokenNonescaping, HistoryTest::action_testTokenNonescaping);
  }

  @Test(timeout = TEST_TOKEN_WITH_QUESTIONMARK_TIMEOUT)
  public Promise<Void> testTokenWithQuestionmark() {
    return withHandler(
        HistoryTest::verify_testTokenWithQuestionmark,
        HistoryTest::action_testTokenWithQuestionmark);
  }

  @Test
  public void testEmptyHistoryToken() {
    if (isHtmlUnit()) {
      return;
    }

    final ArrayList<Object> counter = new ArrayList<>();

    addHistoryListenerImpl(
        event -> {
          verify_testEmptyHistoryToken(counter);
        });

    action_testEmptyHistoryToken(counter);
  }
}
