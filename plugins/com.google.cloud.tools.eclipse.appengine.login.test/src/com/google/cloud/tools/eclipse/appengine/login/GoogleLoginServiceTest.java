/*******************************************************************************
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/

package com.google.cloud.tools.eclipse.appengine.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.eclipse.appengine.login.ui.LoginServiceUi;
import com.google.cloud.tools.ide.login.Account;
import com.google.cloud.tools.ide.login.GoogleLoginState;
import com.google.cloud.tools.ide.login.LoggerFacade;
import com.google.cloud.tools.ide.login.OAuthData;
import com.google.cloud.tools.ide.login.OAuthDataStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

@RunWith(MockitoJUnitRunner.class)
public class GoogleLoginServiceTest {

  @Mock private GoogleLoginState loginState;
  @Mock private OAuthDataStore dataStore;
  @Mock private OAuthData savedOAuthData;
  @Mock private LoginServiceUi uiFacade;
  @Mock private LoggerFacade loggerFacade;

  @Mock private Account account1;
  @Mock private Account account2;
  @Mock private Account account3;

  private static final String PREFERENCE_PATH_ROOT = "google-login-service-test-preference-path";
  private static final Set<String> OAUTH_SCOPES = Collections.unmodifiableSet(
      new HashSet<>(Arrays.asList(
          "email",
          "https://www.googleapis.com/auth/cloud-platform"
      )));

  @Before
  public void setUp() {
    when(account1.getEmail()).thenReturn("some-email-1@example.com");
    when(account2.getEmail()).thenReturn("some-email-2@example.com");
    when(account3.getEmail()).thenReturn("some-email-3@example.com");

    Set<OAuthData> oAuthDataSet = new HashSet<>(Arrays.asList(savedOAuthData));
    when(dataStore.loadOAuthData()).thenReturn(oAuthDataSet);
  }

  @Test
  public void testIsLoggedIn() {
    assertFalse(newLoginServiceWithMockLoginState(false).isLoggedIn());
  }

  @Test
  public void testGetActiveAccount() {
    assertNull(newLoginServiceWithMockLoginState(false).getActiveAccount());
  }

  @Test
  public void testListAccount() {
    assertTrue(newLoginServiceWithMockLoginState(false).listAccounts().isEmpty());
  }

  @Test
  public void testLogIn_successfulLogin() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);

    loginService.logIn(null /* no dialog message */);

    assertTrue(loginService.isLoggedIn());
    // Comparison between Account's is conveniently based only on email. (See 'Account.equals().')
    assertEquals(account1, loginService.getActiveAccount());
    assertEquals(1, loginService.listAccounts().size());
    assertEquals(account1, loginService.listAccounts().iterator().next());
  }

  @Test
  public void testNoAutoLoginAfterLogIn() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);
    loginService.logIn(null /* no dialog message */);

    verify(loginState, times(1)).logInWithLocalServer(anyString());
    assertEquals(account1, loginService.getActiveAccountWithAutoLogin(null));
    verify(loginState, times(1)).logInWithLocalServer(anyString());
  }

  @Test
  public void testMultipleLogins() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);

    loginService.logIn(null /* no dialog message */);
    assertEquals(account1, loginService.getActiveAccount());
    Set<Account> accounts1 = loginService.listAccounts();
    assertEquals(1, accounts1.size());
    assertTrue(accounts1.contains(account1));

    loginService.logIn(null);
    assertEquals(account2, loginService.getActiveAccount());
    Set<Account> accounts2 = loginService.listAccounts();
    assertEquals(2, accounts2.size());
    assertTrue(accounts2.contains(account1));
    assertTrue(accounts2.contains(account2));

    loginService.logIn(null);
    assertEquals(account3, loginService.getActiveAccount());
    Set<Account> accounts3 = loginService.listAccounts();
    assertEquals(3, accounts3.size());
    assertTrue(accounts3.contains(account1));
    assertTrue(accounts3.contains(account2));
    assertTrue(accounts3.contains(account3));
  }

  @Test
  public void testLogOutAll() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);

    loginService.logIn(null /* no dialog message */);
    loginService.logIn(null);
    loginService.logIn(null);

    assertTrue(loginService.isLoggedIn());
    assertEquals(account3, loginService.getActiveAccount());
    assertFalse(loginService.listAccounts().isEmpty());
    assertNotNull(
        Preferences.userRoot().node(PREFERENCE_PATH_ROOT).get("ACTIVE_ACCOUNT_EMAIL", null));

    loginService.logOutAll();

    assertFalse(loginService.isLoggedIn());
    assertNull(loginService.getActiveAccount());
    assertTrue(loginService.listAccounts().isEmpty());
    assertNull(Preferences.userRoot().node(PREFERENCE_PATH_ROOT).get("ACTIVE_ACCOUNT_EMAIL", null));
  }

  @Test
  public void testRestoreActiveAccount() {
    Preferences.userRoot().node(PREFERENCE_PATH_ROOT)
        .put("ACTIVE_ACCOUNT_EMAIL", "some-email-2@example.com");
    when(loginState.listAccounts()).thenReturn(
        new HashSet<Account>(Arrays.asList(account1, account2, account3)));

    GoogleLoginService loginService = newLoginServiceWithMockLoginState(false /* no login setup */);
    loginService.restoreActiveAccount();
    assertEquals(account2, loginService.getActiveAccount());
  }

  @Test
  public void testPersisteActiveAccount() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);

    loginService.logIn(null /* no dialog message */);
    loginService.logIn(null);
    loginService.logIn(null);
    loginService.persistActiveAccount();

    assertEquals("some-email-3@example.com",
        Preferences.userRoot().node(PREFERENCE_PATH_ROOT).get("ACTIVE_ACCOUNT_EMAIL", null));
  }

  @Test
  public void testLogIn_activeAccountPersisted() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);
    when(loginState.logInWithLocalServer(anyString())).thenReturn(account2);
    when(loginState.listAccounts()).thenReturn(new HashSet<>(Arrays.asList(account2)));

    loginService.logIn(null /* no dialog message */);

    assertTrue(loginService.isLoggedIn());
    assertEquals("some-email-2@example.com",
        Preferences.userRoot().node(PREFERENCE_PATH_ROOT).get("ACTIVE_ACCOUNT_EMAIL", null));
  }

  @Test
  public void testSwitchActiveAccount() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);
    loginService.logIn(null /* no dialog message */);
    loginService.logIn(null);
    loginService.logIn(null);

    assertEquals(account3, loginService.getActiveAccount());
    loginService.switchActiveAccount("some-email-1@example.com");
    assertEquals(account1, loginService.getActiveAccount());
    loginService.switchActiveAccount("some-email-2@example.com");
    assertEquals(account2, loginService.getActiveAccount());
    loginService.switchActiveAccount("some-email-3@example.com");
    assertEquals(account3, loginService.getActiveAccount());
  }

  @Test
  public void testSwitchActiveAccount_invalidEmail() {
    GoogleLoginService loginService = newLoginServiceWithMockLoginState(true /* set up logins */);
    loginService.logIn(null /* no dialog message */);
    loginService.logIn(null);
    loginService.logIn(null);
    loginService.switchActiveAccount("non-existing-email@example.com");
    assertEquals(account3, loginService.getActiveAccount());
  }

  @Test
  public void testGoogleLoginService_removeSavedCredentialIfNullRefreshToken() {
    when(savedOAuthData.getEmail()).thenReturn("my-email@example.com");
    when(savedOAuthData.getStoredScopes()).thenReturn(OAUTH_SCOPES);
    when(savedOAuthData.getRefreshToken()).thenReturn(null);

    GoogleLoginService loginService = newLoginService();
    verify(dataStore, times(1)).removeOAuthData("my-email@example.com");
    assertNull(loginService.getActiveAccount());
  }

  @Test
  public void testGoogleLoginService_removeSavedCredentialIfScopesChanged() {
    // Persisted credential in the data store has an out-dated scopes.
    Set<String> newScope = new HashSet<>(Arrays.asList("new_scope"));
    when(savedOAuthData.getEmail()).thenReturn("my-email@example.com");
    when(savedOAuthData.getStoredScopes()).thenReturn(newScope);
    when(savedOAuthData.getRefreshToken()).thenReturn("fake_refresh_token");

    GoogleLoginService loginService = newLoginService();
    verify(dataStore, times(1)).removeOAuthData("my-email@example.com");
    assertNull(loginService.getActiveAccount());
  }

  @Test
  public void testGoogleLoginService_restoreSavedCredential() {
    Preferences.userRoot().node(PREFERENCE_PATH_ROOT)
        .put("ACTIVE_ACCOUNT_EMAIL", "my-email@example.com");
    // Persisted credential in the data store is valid.
    when(savedOAuthData.getEmail()).thenReturn("my-email@example.com");
    when(savedOAuthData.getStoredScopes()).thenReturn(OAUTH_SCOPES);
    when(savedOAuthData.getRefreshToken()).thenReturn("fake_refresh_token");

    GoogleLoginService loginService = newLoginService();
    verify(dataStore, never()).removeOAuthData("my-email@example.com");
    verify(dataStore, never()).clearStoredOAuthData();
    assertNotNull(loginService.getActiveAccount());
  }

  @Test
  public void testGetGoogleLoginUrl() {
    String customRedirectUrl = "http://127.0.0.1:12345/Callback";

    String loginUrl = GoogleLoginService.getGoogleLoginUrl(customRedirectUrl);
    assertTrue(loginUrl.startsWith("https://accounts.google.com/o/oauth2/auth?"));
    assertTrue(loginUrl.contains("redirect_uri=" + customRedirectUrl));
  }

  private GoogleLoginService newLoginServiceWithMockLoginState(boolean setUpThreeLogins) {
    GoogleLoginService loginService = new GoogleLoginService(
        loginState, PREFERENCE_PATH_ROOT, dataStore, uiFacade, loggerFacade);

    if (setUpThreeLogins) {
      when(loginState.logInWithLocalServer(anyString()))
          .thenReturn(account1).thenReturn(account2).thenReturn(account3);
      when(loginState.listAccounts())
          .thenReturn(new HashSet<>(Arrays.asList(account1)))
          .thenReturn(new HashSet<>(Arrays.asList(account1, account2)))
          .thenReturn(new HashSet<>(Arrays.asList(account1, account2, account3)));
    }
    return loginService;
  }

  private GoogleLoginService newLoginService() {
    return new GoogleLoginService(PREFERENCE_PATH_ROOT, dataStore, uiFacade, loggerFacade);
  }

  @After
  public void tearDown() throws BackingStoreException {
    Preferences.userRoot().node(PREFERENCE_PATH_ROOT).removeNode();
  }
}
