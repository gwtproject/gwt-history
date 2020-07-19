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

import static elemental2.core.Global.decodeURI;
import static elemental2.core.Global.encodeURI;
import static elemental2.dom.DomGlobal.window;

import org.gwtproject.event.logical.shared.HasValueChangeHandlers;
import org.gwtproject.event.logical.shared.ValueChangeEvent;
import org.gwtproject.event.logical.shared.ValueChangeHandler;
import org.gwtproject.event.shared.Event;
import org.gwtproject.event.shared.HandlerRegistration;
import org.gwtproject.event.shared.SimpleEventBus;

/**
 * This class allows you to interact with the browser's history stack. Each "item" on the stack is
 * represented by a single string, referred to as a "token". You can create new history items (which
 * have a token associated with them when they are created), and you can programmatically force the
 * current history to move back or forward.
 *
 * <p>In order to receive notification of user-directed changes to the current history item,
 * implement the {@link ValueChangeHandler} interface and attach it via {@link
 * #addValueChangeHandler(ValueChangeHandler)}.
 *
 * <h3>URL Encoding</h3>
 *
 * <p>Any valid characters may be used in the history token and will survive round-trips through
 * {@link #newItem(String)} to {@link #getToken()}/ {@link
 * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} , but
 * most will be encoded in the user-visible URL. The following US-ASCII characters are not encoded
 * on any currently supported browser (but may be in the future due to future browser changes):
 *
 * <ul>
 *   <li>a-z
 *   <li>A-Z
 *   <li>0-9
 *   <li>;,/?:@&amp;=+$-_.!~*()
 * </ul>
 */
public class History {

  private static final HistoryEventSource historyEventSource = new HistoryEventSource();
  // XXX: use 2-args overload of System.getProperty to not fail compilation is property is not
  // defined.
  @SuppressWarnings(
      "ReferenceEquality") // '==' makes it compile out faster (we're in client-only code)
  private static final HistoryTokenEncoder tokenEncoder =
      System.getProperty("history.noDoubleEncoding", null) == "true"
          ? new NoopHistoryTokenEncoder()
          : new HistoryTokenEncoder();

  private static String token = getDecodedHash();

  static {
    window.addEventListener("hashchange", evt -> onHashChanged());
  }

  /**
   * Adds a {@link org.gwtproject.event.logical.shared.ValueChangeEvent} handler to be informed of
   * changes to the browser's history stack.
   *
   * @param handler the handler
   * @return the registration used to remove this value change handler
   */
  public static HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return historyEventSource.addValueChangeHandler(handler);
  }

  /** Programmatic equivalent to the user pressing the browser's 'back' button. */
  public static void back() {
    window.history.back();
  }

  /** Programmatic equivalent to the user pressing the browser's 'forward' button. */
  public static void forward() {
    window.history.forward();
  }

  /**
   * Adds a new browser history entry. Calling this method will cause {@link
   * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} to be
   * called as well.
   *
   * @param historyToken the token to associate with the new history item
   */
  public static void newItem(String historyToken) {
    newItem(historyToken, true);
  }

  /**
   * Adds a new browser history entry. Calling this method will cause {@link
   * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} to be
   * called as well if and only if issueEvent is true.
   *
   * @param historyToken the token to associate with the new history item
   * @param issueEvent true if a {@link
   *     ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)}
   *     event should be issued
   */
  public static void newItem(String historyToken, boolean issueEvent) {
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(getToken())) {
      token = historyToken;
      String updateToken = encodeHistoryToken(historyToken);
      newToken(updateToken);
      if (issueEvent) {
        historyEventSource.fireValueChangedEvent(historyToken);
      }
    }
  }

  /**
   * Gets the current history token. The handler will not receive a {@link
   * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} event
   * for the initial token; requiring that an application request the token explicitly on startup
   * gives it an opportunity to run different initialization code in the presence or absence of an
   * initial token.
   *
   * @return the initial token, or the empty string if none is present.
   */
  public static String getToken() {
    return token;
  }

  /**
   * Encode a history token for use as part of a URI.
   *
   * @param historyToken the token to encode
   * @return the encoded token, suitable for use as part of a URI
   */
  public static String encodeHistoryToken(String historyToken) {
    return tokenEncoder.encode(historyToken);
  }

  private static void newToken(String historyToken) {
    window.location.hash = historyToken;
  }

  /**
   * Replace the current history token on top of the browsers history stack.
   *
   * <p>Calling this method will cause {@link
   * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} to be
   * called as well.
   *
   * @param historyToken history token to replace current top entry
   */
  public static void replaceItem(String historyToken) {
    replaceItem(historyToken, true);
  }

  /**
   * Replace the current history token on top of the browsers history stack.
   *
   * <p>Calling this method will cause {@link
   * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} to be
   * called as well if and only if issueEvent is true.
   *
   * @param historyToken history token to replace current top entry
   * @param issueEvent issueEvent true if a {@link
   *     ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)}
   *     event should be issued
   */
  public static void replaceItem(String historyToken, boolean issueEvent) {
    token = historyToken;
    window.location.replace("#" + encodeHistoryToken(historyToken));
    if (issueEvent) {
      fireCurrentHistoryState();
    }
  }

  /**
   * Fire {@link
   * ValueChangeHandler#onValueChange(org.gwtproject.event.logical.shared.ValueChangeEvent)} events
   * with the current history state. This is most often called at the end of the application's
   * startup to inform history handlers of the initial application state.
   */
  public static void fireCurrentHistoryState() {
    String currentToken = getToken();
    historyEventSource.fireValueChangedEvent(currentToken);
  }

  private static void onHashChanged() {
    /*
     * We guard against firing events twice, some browser (e.g. safari) tend to
     * fire events on startup if HTML5 pushstate is used.
     */
    String hashToken = getDecodedHash();
    if (!hashToken.equals(getToken())) {
      token = hashToken;
      historyEventSource.fireValueChangedEvent(hashToken);
    }
  }

  private static String getDecodedHash() {
    String hashToken = window.location.hash;
    if (hashToken == null || hashToken.isEmpty()) {
      return "";
    }
    return tokenEncoder.decode(hashToken.substring(1));
  }

  private static class HistoryEventSource implements HasValueChangeHandlers<String> {

    private final SimpleEventBus handlers = new SimpleEventBus();

    @Override
    public void fireEvent(Event<?> event) {
      handlers.fireEvent(event);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      return handlers.addHandler(ValueChangeEvent.getType(), handler);
    }

    public void fireValueChangedEvent(String newToken) {
      ValueChangeEvent.fire(this, newToken);
    }
  }

  /**
   * HistoryTokenEncoder is responsible for encoding and decoding history token, thus ensuring that
   * tokens are safe to use in the browsers URL.
   */
  private static class HistoryTokenEncoder {

    String encode(String toEncode) {
      // encodeURI() does *not* encode the '#' character.
      return encodeURI(toEncode).replace("#", "%23");
    }

    String decode(String toDecode) {
      return decodeURI(toDecode.replace("%23", "#"));
    }
  }

  /** NoopHistoryTokenEncoder does not perform any encoding. */
  private static class NoopHistoryTokenEncoder extends HistoryTokenEncoder {

    @Override
    String encode(String toEncode) {
      return toEncode;
    }

    @Override
    String decode(String toDecode) {
      return toDecode;
    }
  }
}
