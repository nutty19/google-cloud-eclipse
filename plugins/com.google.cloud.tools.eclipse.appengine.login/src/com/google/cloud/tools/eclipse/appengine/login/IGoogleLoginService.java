/*******************************************************************************
 * Copyright 2016 Google Inc.
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
 *******************************************************************************/

package com.google.cloud.tools.eclipse.appengine.login;

import com.google.cloud.tools.ide.login.Account;

import java.util.Set;

public interface IGoogleLoginService {

  /**
   * Initiates user login by launching an external browser that a user will interact with
   * to log in. The returned {@link Account}, if not {@code null}, becomes an active account.
   *
   * Must be called from a UI context.
   *
   * @param dialogMessage custom login dialog message. Can be {@code null}
   * @return signed-in {@link Account} for successful login; {@code null} otherwise,
   *     including failed and canceled login
   */
  Account logIn(String dialogMessage);

  /**
   * Returns an active {@link Account} (among multiple logged-in accounts). Unlike {@link
   * #getActiveAccountWithAutoLogin}, this version does not involve login process or make
   * network calls. Returns {@code null} if there is no account logged in.
   *
   * Safe to call from non-UI contexts.
   */
  Account getActiveAccount();

  /**
   * Returns an active {@link Account} (among multiple logged-in accounts). If there is no
   * account logged in, calls {@link #logIn}. The implementation is effectively as below:
   *
   * <pre>
   * {@code
   * if (activeAccount != null) {
   *    return activeAccount;
   *  }
   *  return logIn(dialogMessage);
   * }
   * </pre>
   *
   * Must be called from a UI context.
   *
   * @see #logIn
   */
  Account getActiveAccountWithAutoLogin(String dialogMessage);

  /**
   * Clears all accounts. ("Logging out" from users' perspective.)
   *
   * Safe to call from non-UI contexts.
   */
  void logOutAll();

  /**
   * @return true iff {@link #getActiveAccount} does not return {@code null}.
   */
  boolean isLoggedIn();

  /**
   * If there exists an account that matches the given {@code email}, makes it an active account.
   *
   * Safe to call from non-UI contexts.
   *
   * @param email cannot be {@null}
   * @return true if there existed an account matching the {@code email} and it became active;
   *     false otherwise
   */
  boolean switchActiveAccount(String email);

  /**
   * Returns a list of currently logged-in accounts.
   *
   * Safe to call from non-UI contexts.
   *
   * @return never {@code null}
   */
  Set<Account> listAccounts();
}
