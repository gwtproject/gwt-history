/*
 * Copyright 2014 The GWT Project Authors
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

import com.google.j2cl.junit.apt.J2clTestInput;
import elemental2.promise.Promise;
import org.junit.Test;

/** Tests for the history system without encoding of history tokens. */
@J2clTestInput(HistoryTestNoopTokenEncoder.class)
public class HistoryTestNoopTokenEncoder extends HistoryTest {

  @Override
  @Test
  public Promise<Void> testEmptyHistoryTokens() {
    return super.testEmptyHistoryTokens();
  }

  //  @Override
  //  public void testTokenEscaping() {
  //     FIXME: test is broken in all browsers
  //  }

}
