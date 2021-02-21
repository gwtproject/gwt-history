/*
 * Copyright 2008 The GWT Project Authors
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

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;

import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLInputElement;
import java.util.ArrayList;
import jsinterop.base.Js;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;

/**
 * Tests for the history system.
 *
 * <p>TODO: find a way to test unescaping of the initial hash value.
 */
public class HistoryTest {

  private static String getCurrentLocationHash() {
    String hash = window.location.hash;
    if (hash.isEmpty()) {
      fail("can not read history token");
    }
    return hash.substring(1);
  }

  static final int TEST_CLICK_LINK_TIMEOUT = 5000;

  static Element setup_testClickLink() {
    HTMLAnchorElement anchorElement = (HTMLAnchorElement) document.createElement("a");
    anchorElement.href = "#href1";
    document.body.appendChild(anchorElement);

    try {
      History.newItem("something_as_base");

      return anchorElement;
    } catch (RuntimeException e) {
      teardown_testClickLink(anchorElement);
      throw e;
    }
  }

  static void action_testClickLink(Element anchorElement) {
    // Element is missing click, so unchecked-casting to HTMLInputElement which has it
    // https://github.com/google/elemental2/issues/86
    Js.<HTMLInputElement>uncheckedCast(anchorElement).click();
  }

  static void verify_testClickLink(ValueChangeEvent<String> event) {
    assertEquals("href1", event.getValue());
  }

  static void teardown_testClickLink(Element anchorElement) {
    document.body.removeChild(anchorElement);
  }

  private static final String TEST_DOUBLE_ESCAPING_TOKEN = "%24%24%24";
  static final int TEST_DOUBLE_ESCAPING_TIMEOUT = 5000;

  /* Tests against issue #572: Double unescaping of history tokens. */
  static void verify_testDoubleEscaping(ValueChangeEvent<String> event) {
    assertEquals(TEST_DOUBLE_ESCAPING_TOKEN, event.getValue());
  }

  static void action_testDoubleEscaping() {
    History.newItem(TEST_DOUBLE_ESCAPING_TOKEN);
  }

  static final int TEST_EMPTY_HISTORY_TOKENS_TIMEOUT = 5000;

  /*
   * Tests against issue #879: Ensure that empty history tokens do not add
   * additional characters after the '#' symbol in the URL.
   */
  static boolean verify_testEmptyHistoryTokens(ValueChangeEvent<String> event) {
    String historyToken = event.getValue();
    if (historyToken == null) {
      fail("historyToken should not be null");
    }

    if (historyToken.equals("foobar")) {
      History.newItem("");
      return false;
    } else {
      assertEquals("", historyToken);
      return true;
    }
  }

  static void action_testEmptyHistoryTokens() {
    // We must first start out with a non-blank history token. Adding a blank
    // history token in the initial state will not cause an onHistoryChanged
    // event to fire.
    History.newItem("foobar");
  }

  static final int TEST_NO_EVENTS_TIMEOUT = 5000;
  static final int TEST_NO_EVENTS_DELAY = 500;

  /** Verify that no events are issued via newItem if there were not reqeuested. */
  static void verify_testNoEvents() {
    fail("onHistoryChanged should not have been called");
  }

  static void action_testNoEvents() {
    History.newItem("testNoEvents", false);
  }

  static final int TEST_HISTORY_TIMEOUT = 10000;
  private static final String TEST_HISTORY_TOKEN_1 = "token1";
  private static final String TEST_HISTORY_TOKEN_2 = "token 2";

  /*
   * Ensure that non-url-safe strings (such as those containing spaces) are
   * encoded/decoded correctly, and that programmatic 'back' works.
   */
  static void setup_testHistory() {
    /*
     * Sentinel token which should only be seen if tokens are lost during the
     * rest of the test. Without this, History.back() might send the browser too
     * far back, i.e. back to before the web app containing our test module.
     */
    History.newItem("if-you-see-this-then-history-went-back-too-far");
  }

  /**
   * @param state needs to be incremented before the method is called, but the previous state be
   *     passed to the method. E.g. {@code verify_testHistory(event, state++)}
   */
  static boolean verify_testHistory(ValueChangeEvent<String> event, int state) {
    String historyToken = event.getValue();
    switch (state) {
      case 0:
        if (!historyToken.equals(TEST_HISTORY_TOKEN_1)) {
          fail("Expecting token '" + TEST_HISTORY_TOKEN_1 + "', but got: " + historyToken);
        }

        History.newItem(TEST_HISTORY_TOKEN_2);
        return false;

      case 1:
        if (!historyToken.equals(TEST_HISTORY_TOKEN_2)) {
          fail("Expecting token '" + TEST_HISTORY_TOKEN_2 + "', but got: " + historyToken);
        }

        History.back();
        return false;

      case 2:
        if (!historyToken.equals(TEST_HISTORY_TOKEN_1)) {
          fail("Expecting token '" + TEST_HISTORY_TOKEN_1 + "', but got: " + historyToken);
        }
        return true;

      default:
        fail("Unexpected state " + state);
        return true;
    }
  }

  static void action_testHistory() {
    History.newItem(TEST_HISTORY_TOKEN_1);
  }

  static final int TEST_HISTORY_CHANGED_COUNT_TIMEOUT = 5000;
  static final int TEST_HISTORY_CHANGED_COUNT_DELAY = 500;

  /**
   * Verify that {@link ValueChangeHandler#onValueChange(ValueChangeEvent)} is only called once per
   * {@link History#newItem(String)}.
   */
  static boolean timer_testHistoryChangedCount(int count) {
    if (count == 0) {
      // verify that duplicates don't issue another event
      History.newItem("testHistoryChangedCount");
      return false;
    } else {
      return true;
    }
  }

  static boolean verify_testHistoryChangedCount(int count) {
    if (count != 0) {
      fail("onHistoryChanged called multiple times");
      return true;
    } else {
      // wait again to see if we get called multiple times
      return false;
    }
  }

  static void start_testHistoryChangedCount() {
    History.newItem("testHistoryChangedCount");
  }

  static final int TEST_REPLACE_ITEM_TIMEOUT = 10000;
  private static final String TEST_REPLACE_ITEM_TOKEN_1 = "token1";
  private static final String TEST_REPLACE_ITEM_TOKEN_2 = "token 2";
  private static final String TEST_REPLACE_ITEM_TOKEN_3 = "token3";

  static void setup_testReplaceItem() {
    /*
     * Sentinel token which should only be seen if tokens are lost during the rest of the test.
     * Without this, History.back() might send the browser too far back, i.e. back to before the web
     * app containing our test module.
     */
    History.newItem("if-you-see-this-then-history-went-back-too-far");
  }

  static boolean verify_testReplaceItem(ValueChangeEvent<String> event, int state) {
    String historyToken = event.getValue();
    switch (state) {
      case 0:
        if (!historyToken.equals(TEST_REPLACE_ITEM_TOKEN_1)) {
          fail("Expecting token '" + TEST_REPLACE_ITEM_TOKEN_1 + "', but got: " + historyToken);
        }

        History.newItem(TEST_REPLACE_ITEM_TOKEN_2);
        return false;

      case 1:
        if (!historyToken.equals(TEST_REPLACE_ITEM_TOKEN_2)) {
          fail("Expecting token '" + TEST_REPLACE_ITEM_TOKEN_2 + "', but got: " + historyToken);
        }

        History.replaceItem(TEST_REPLACE_ITEM_TOKEN_3, true);
        return false;

      case 2:
        if (!historyToken.equals(TEST_REPLACE_ITEM_TOKEN_3)) {
          fail("Expecting token '" + TEST_REPLACE_ITEM_TOKEN_3 + "', but got: " + historyToken);
        }
        History.back();
        return false;

      case 3:
        if (!historyToken.equals(TEST_REPLACE_ITEM_TOKEN_1)) {
          fail("Expecting token '" + TEST_REPLACE_ITEM_TOKEN_1 + "', but got: " + historyToken);
        }
        return true;

      default:
        fail("Unexpected state " + state);
        return true;
    }
  }

  static void action_testReplaceItem() {
    History.newItem(TEST_REPLACE_ITEM_TOKEN_1);
  }

  static final int TEST_REPLACE_ITEM_NO_EVENT_TIMEOUT = 500;
  static final int TEST_REPLACE_ITEM_NO_EVENT_DELAY = 200;
  private static final String TEST_REPLACE_ITEM_NO_EVENT_TOKEN_1 = "token1";
  private static final String TEST_REPLACE_ITEM_NO_EVENT_TOKEN_2 = "token 2";
  private static final String TEST_REPLACE_ITEM_NO_EVENT_TOKEN_2_ENCODED = "token%202";

  /*
   * HtmlUnit tends to fire events after adding a new item with fireEvent set
   * to false. This makes 'testEmptyHistoryTokens' randomly fail after running
   * this test.
   */
  static void setup_testReplaceItemNoEvent() {
    /*
     * Sentinel token which should only be seen if tokens are lost during the rest of the test.
     * Without this, History.back() might send the browser too far back, i.e. back to before the web
     * app containing our test module.
     */
    History.newItem("if-you-see-this-then-history-went-back-too-far");

    History.newItem(TEST_REPLACE_ITEM_NO_EVENT_TOKEN_1);
  }

  static void verify_testReplaceItemNoEvent() {
    fail("No event expected");
  }

  static void action_testReplaceItemNoEvent() {
    History.replaceItem(TEST_REPLACE_ITEM_NO_EVENT_TOKEN_2, false);
    assertEquals(TEST_REPLACE_ITEM_NO_EVENT_TOKEN_2, History.getToken());
  }

  static void timer_testReplaceItemNoEvent() {
    // Make sure that we have updated the URL properly.
    assertEquals(TEST_REPLACE_ITEM_NO_EVENT_TOKEN_2_ENCODED, getCurrentLocationHash());
  }

  static final int TEST_TOKEN_ESCAPING_TIMEOUT = 5000;
  private static final String TEST_TOKEN_ESCAPING_SHOULD_BE_ENCODED = "% ^[]|\"<>{}\\";
  private static final String TEST_TOKEN_ESCAPING_SHOULD_BE_ENCODED_AS =
      "%25%20%5E%5B%5D%7C%22%3C%3E%7B%7D%5C";

  static void verify_testTokenEscaping(ValueChangeEvent<String> event) {
    assertEquals(TEST_TOKEN_ESCAPING_SHOULD_BE_ENCODED_AS, getCurrentLocationHash());
    assertEquals(TEST_TOKEN_ESCAPING_SHOULD_BE_ENCODED, event.getValue());
  }

  static void action_testTokenEscaping() {
    History.newItem(TEST_TOKEN_ESCAPING_SHOULD_BE_ENCODED);
  }

  static final int TEST_NO_DOUBLE_TOKEN_UNESCAPING_TIMEOUT = 5000;
  static final int TEST_NO_DOUBLE_TOKEN_UNESCAPING_DELAY = 200;
  private static final String TEST_NO_DOUBLE_TOKEN_UNESCAPING_SHOULD_BE_ENCODED = "abc%20abc";

  /**
   * Test to make sure that there is no double unescaping of hash values. See
   * https://bugzilla.mozilla.org/show_bug.cgi?id=483304
   */
  static void setup_testNoDoubleTokenUnEscaping() {
    History.newItem(TEST_NO_DOUBLE_TOKEN_UNESCAPING_SHOULD_BE_ENCODED);
    History.newItem("someOtherToken");
    History.back();
  }

  static void verify_testNoDoubleTokenUnEscaping() {
    // make sure that value in url actually matches the original token
    assertEquals(TEST_NO_DOUBLE_TOKEN_UNESCAPING_SHOULD_BE_ENCODED, History.getToken());
  }

  static final int TEST_TOKEN_NONESCAPING_TIMEOUT = 5000;
  private static final String TEST_TOKEN_NONESCAPING_SHOULD_NOT_CHANGE =
      "abc;,/?:@&=+$-_.!~*()ABC123foo";
  /*
   * HtmlUnit reports:
   *   expected=abc;,/?:@&=+$-_.!~*()ABC123foo
   *   actual  =abc;,/?:@&=%20$-_.!~*()ABC123foo
   */
  static void verify_testTokenNonescaping(ValueChangeEvent<String> event) {
    assertEquals(TEST_TOKEN_NONESCAPING_SHOULD_NOT_CHANGE, event.getValue());
  }

  static void action_testTokenNonescaping() {
    History.newItem(TEST_TOKEN_NONESCAPING_SHOULD_NOT_CHANGE);
  }

  static final int TEST_TOKEN_WITH_QUESTIONMARK_TIMEOUT = 5000;
  private static final String TEST_TOKEN_WITH_QUESTIONMARK_TOKEN = "foo?bar";

  /*
   * Test against issue #2500. IE6 has a bug that causes it to not report any
   * part of the current fragment after a '?' when read from location.hash; make
   * sure that on affected browsers, we're not relying on this.
   */
  static void verify_testTokenWithQuestionmark(ValueChangeEvent<String> event) {
    String historyToken = event.getValue();
    if (historyToken == null) {
      fail("historyToken should not be null");
    }
    assertEquals(TEST_TOKEN_WITH_QUESTIONMARK_TOKEN, historyToken);
  }

  static void action_testTokenWithQuestionmark() {
    History.newItem(TEST_TOKEN_WITH_QUESTIONMARK_TOKEN);
  }

  /**
   * Test that using an empty history token works properly. There have been problems (see issue
   * 2905) with this in the past on Safari.
   *
   * <p>Seems like a HtmlUnit bug. Need more investigation.
   */
  static void verify_testEmptyHistoryToken(ArrayList<Object> counter) {
    counter.add(new Object());
    assertFalse("Browser is borked by empty history token", isBorked());
  }

  static void action_testEmptyHistoryToken(ArrayList<Object> counter) {
    History.newItem("x");
    History.newItem("");

    assertEquals("Expected two history events", 2, counter.size());
  }

  // Used by testEmptyHistoryToken() to catch a bizarre failure mode on Safari.
  private static boolean isBorked() {
    Element e = document.createElement("div");
    e.innerHTML = "string";
    return e.innerHTML.length() == 0;
  }
}
