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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.cloud.tools.eclipse.appengine.login.ui.LoginServiceUi;
import com.google.cloud.tools.ide.login.Account;
import com.google.cloud.tools.ide.login.GoogleLoginState;
import com.google.cloud.tools.ide.login.JavaPreferenceOAuthDataStore;
import com.google.cloud.tools.ide.login.LoggerFacade;
import com.google.cloud.tools.ide.login.OAuthDataStore;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Provides service related to login, e.g., account management, getting a credential of a
 * currently active user, etc.
 */
public class GoogleLoginService implements IGoogleLoginService {

  private static final String PREFERENCE_KEY_ACTIVE_ACCOUNT_EMAIL = "ACTIVE_ACCOUNT_EMAIL";
  private static final String PREFERENCE_PATH_ROOT = "/com/google/cloud/tools/eclipse/login";
  private static final String PREFERENCE_PATH_OAUTH_DATA_STORE =
      PREFERENCE_PATH_ROOT + "/datastore";

  // For the detailed info about each scope, see
  // https://github.com/GoogleCloudPlatform/gcloud-eclipse-tools/wiki/Cloud-Tools-for-Eclipse-Technical-Design#oauth-20-scopes-requested
  private static final SortedSet<String> OAUTH_SCOPES = Collections.unmodifiableSortedSet(
      new TreeSet<>(Arrays.asList(
          "email", //$NON-NLS-1$
          "https://www.googleapis.com/auth/cloud-platform" //$NON-NLS-1$
      )));

  /**
   * Returns a URL through which users can login.
   *
   * @param redirectUrl URL to which the login result is directed. For example, a local web
   *     server listening on the URL can receive an authorization code from it.
   */
  public static String getGoogleLoginUrl(String redirectUrl) {
    return new GoogleAuthorizationCodeRequestUrl(Constants.getOAuthClientId(), redirectUrl,
        GoogleLoginService.OAUTH_SCOPES).toString();
  }

  // We expose the references 'activeAccount' and 'accounts' to callers as-is. 'GoogleLoginService'
  // must not modify the states of the objects, rather than updating the references.
  private Account activeAccount;
  private Set<Account> accounts;

  private String preferencePathRoot;

  private GoogleLoginState loginState;

  private LoginServiceUi loginServiceUi;

  /**
   * Called by OSGi Declarative Services Runtime when the {@link GoogleLoginService} is activated
   * as an OSGi service.
   */
  protected void activate() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    LoginServiceLogger logger = new LoginServiceLogger();
    IShellProvider shellProvider = new IShellProvider() {
      @Override
      public Shell getShell() {
        return workbench.getDisplay().getActiveShell();
      }
    };

    loginServiceUi = new LoginServiceUi(workbench, shellProvider, workbench.getDisplay());
    loginState = new GoogleLoginState(
        Constants.getOAuthClientId(), Constants.getOAuthClientSecret(), OAUTH_SCOPES,
        new JavaPreferenceOAuthDataStore(PREFERENCE_PATH_OAUTH_DATA_STORE, logger),
        loginServiceUi, logger);

    preferencePathRoot = PREFERENCE_PATH_ROOT;
    restoreActiveAccount();
  }

  /**
   * 0-arg constructor is necessary for OSGi Declarative Services. Initialization will be done
   * by {@link activate()}.
   */
  public GoogleLoginService() {}

  @VisibleForTesting
  GoogleLoginService(String preferencePathRoot,
      OAuthDataStore dataStore, LoginServiceUi uiFacade, LoggerFacade loggerFacade) {
    this(new GoogleLoginState(Constants.getOAuthClientId(), Constants.getOAuthClientSecret(),
                              OAUTH_SCOPES, dataStore, uiFacade, loggerFacade),
        preferencePathRoot, dataStore, uiFacade, loggerFacade);
  }

  @VisibleForTesting
  GoogleLoginService(GoogleLoginState loginState, String preferencePathRoot,
      OAuthDataStore dataStore, LoginServiceUi uiFacade, LoggerFacade loggerFacade) {
    loginServiceUi = uiFacade;
    this.loginState = loginState;
    this.preferencePathRoot = preferencePathRoot;
    restoreActiveAccount();
  }

  @VisibleForTesting
  void restoreActiveAccount() {
    Preferences preferences = Preferences.userRoot().node(preferencePathRoot);
    String activeAccountEmail = preferences.get(PREFERENCE_KEY_ACTIVE_ACCOUNT_EMAIL, null);

    accounts = loginState.listAccounts();
    activeAccount = findAccount(activeAccountEmail);
  }

  @VisibleForTesting
  void persistActiveAccount() {
    Preferences preferences = Preferences.userRoot().node(preferencePathRoot);
    if (activeAccount == null) {
      preferences.remove(PREFERENCE_KEY_ACTIVE_ACCOUNT_EMAIL);
    } else {
      preferences.put(PREFERENCE_KEY_ACTIVE_ACCOUNT_EMAIL, activeAccount.getEmail());
    }

    try {
      preferences.flush();
    } catch (BackingStoreException bse) {
      logger.log(Level.WARNING, bse.getLocalizedMessage());
    }
  }

  @Override
  public Account logIn(String dialogMessage) {
    // TODO: holding a lock for a long period of time (especially when waiting for UI events)
    // should be avoided. Make the login library thread-safe, and don't lock during UI events.
    // (https://github.com/GoogleCloudPlatform/ide-login/issues/21)
    synchronized (loginState) {
      Account account = loginState.logInWithLocalServer(dialogMessage);
      if (account != null) {
        activeAccount = account;
        accounts = loginState.listAccounts();
        persistActiveAccount();
      }
      return activeAccount;
    }
  }

  @Override
  public Account getActiveAccountWithAutoLogin(String dialogMessage) {
    synchronized (loginState) {
      if (activeAccount != null) {
        return activeAccount;
      }
      return logIn(dialogMessage);
    }
  }

  @Override
  public Account getActiveAccount() {
    synchronized (loginState) {
      return activeAccount;
    }
  }

  @Override
  public void logOutAll() {
    synchronized (loginState) {
      loginState.logOutAll(false /* Don't prompt for logout. */);
      activeAccount = null;
      accounts = new HashSet<>();
      persistActiveAccount();
    }
  }

  @Override
  public boolean isLoggedIn() {
    synchronized (loginState) {
      return activeAccount != null;
    }
  };

  @Override
  public boolean switchActiveAccount(String email) {
    Preconditions.checkNotNull(email);

    synchronized (loginState) {
      Account account = findAccount(email);
      if (account != null) {
        activeAccount = account;
        persistActiveAccount();
        return true;
      }
      return false;
    }
  }

  @Override
  public Set<Account> listAccounts() {
    synchronized (loginState) {
      // 'accounts' is a reference to a copy of Accounts maintained in 'loginState'.
      // ('loginState.listAccounts()' returns a copy.) We intend to return this
      // reference to callers, while never modifying the set itself.
      return accounts;
    }
  }

  private Account findAccount(String email) {
    for (Account account : accounts) {
      if (account.getEmail().equals(email)) {
        return account;
      }
    }
    return null;
  }

  private static final Logger logger = Logger.getLogger(GoogleLoginService.class.getName());

  private static class LoginServiceLogger implements LoggerFacade {

    @Override
    public void logError(String message, Throwable thrown) {
      logger.log(Level.SEVERE, message, thrown);
    }

    @Override
    public void logWarning(String message) {
      logger.log(Level.WARNING, message);
    }
  };
}
