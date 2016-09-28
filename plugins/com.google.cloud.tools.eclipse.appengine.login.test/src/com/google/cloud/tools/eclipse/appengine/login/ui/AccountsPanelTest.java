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

package com.google.cloud.tools.eclipse.appengine.login.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.eclipse.appengine.login.IGoogleLoginService;
import com.google.cloud.tools.ide.login.Account;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AccountsPanelTest {

  @Mock private IGoogleLoginService loginService;
  private Display display;

  @Mock private Account account1;
  @Mock private Account account2;
  @Mock private Account account3;

  @Before
  public void setUp() {
    display = new Display();
    when(account1.getEmail()).thenReturn("some-email-1@example.com");
    when(account2.getEmail()).thenReturn("some-email-2@example.com");
    when(account3.getEmail()).thenReturn("some-email-3@example.com");
  }

  @After
  public void tearDown() {
    display.dispose();
  }

  @Test
  public void testLogOutButton_notLoggedIn() {
    setUpLoginService();

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertNull(panel.logOutButton);
  }

  @Test
  public void testLogOutButton_loggedIn() {
    setUpLoginService(Arrays.asList(account1));

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertNotNull(panel.logOutButton);
  }

  @Test
  public void testActiveAccount_notLoggedIn() {
    setUpLoginService();

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertNull(panel.activeAccountLabel);
  }

  @Test
  public void testActiveAccount_loggedIn() {
    setUpLoginService(Arrays.asList(account1));

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertNotNull(panel.activeAccountLabel);
  }

  @Test
  public void testAccountsArea_zeroInactiveAccounts() {
    setUpLoginService(Arrays.asList(account1));

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertTrue(panel.inactiveAccountLinks.isEmpty());
  }

  @Test
  public void testAccountsArea_oneInactiveAccounts() {
    setUpLoginService(Arrays.asList(account1, account2));

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertEquals(1, panel.inactiveAccountLinks.size());
    panel.inactiveAccountLinks.get(0).getText().contains(account2.getEmail());
  }

  @Test
  public void testAccountsArea_twoInactiveAccounts() {
    setUpLoginService(Arrays.asList(account1, account2, account3));

    AccountsPanel panel = new AccountsPanel(null, loginService);
    panel.createDialogArea(new Shell(display));

    assertEquals(2, panel.inactiveAccountLinks.size());
    String text1 = panel.inactiveAccountLinks.get(0).getText();
    String text2 = panel.inactiveAccountLinks.get(1).getText();
    assertTrue((text1 + text2).contains(account2.getEmail()));
    assertTrue((text1 + text2).contains(account3.getEmail()));
  }

  private void setUpLoginService(List<Account> accounts) {
    when(loginService.isLoggedIn()).thenReturn(!accounts.isEmpty());
    when(loginService.listAccounts()).thenReturn(new HashSet<>(accounts));
    if (!accounts.isEmpty()) {
      when(loginService.getActiveAccount()).thenReturn(accounts.get(0));
    }
  }

  private void setUpLoginService() {
    setUpLoginService(new ArrayList<Account>());  // Simulate no signed-in account.
  }
}
