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

import com.google.cloud.tools.eclipse.appengine.login.IGoogleLoginService;
import com.google.cloud.tools.eclipse.appengine.login.Messages;
import com.google.cloud.tools.eclipse.ui.util.FontUtil;
import com.google.cloud.tools.ide.login.Account;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import java.util.Set;

/**
 * A panel listing all currently logged-in accounts. The panel allows adding new accounts,
 * switching active accounts, and logging out all accounts.
 *
 * TODO(chanseok): move the panel to the bottom-right corner: https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/740
 */
public class AccountsPanel extends PopupDialog {

  private static final int DEFAULT_MARGIN = 5;
  private static final int EMAIL_LEFT_MARGIN = 8;

  private boolean showAtCenter;
  private IGoogleLoginService loginService;

  public AccountsPanel(Shell parent, IGoogleLoginService loginService, boolean showAtCenter) {
    super(parent, SWT.MODELESS,
        true /* takeFocusOnOpen */,
        false /* persistSize */,
        false /* persistLocation */,
        false /* showDialogMenu */,
        true /* showPersistActions */,
        null /* no title area */, null /* no info text area */);
    this.loginService = loginService;
    this.showAtCenter = showAtCenter;
  }

  @Override
  protected Color getBackground() {
    return getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
  }

  @Override
  protected Color getForeground() {
    return getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.swtDefaults().margins(DEFAULT_MARGIN, DEFAULT_MARGIN).applyTo(container);

    createAccountsPane(container);
    createButtons(container);
    return container;
  }

  private void createAccountsPane(Composite container) {
    Label messageLabel = new Label(container, SWT.NONE);
    messageLabel.setText(Messages.MESSAGE_LABEL_ACTIVE_ACCOUNT);
    FontUtil.convertFontToBold(messageLabel);

    Composite activeAccountIndenter = new Composite(container, SWT.NONE);
    GridLayoutFactory.swtDefaults().margins(EMAIL_LEFT_MARGIN, 0).applyTo(activeAccountIndenter);

    Account activeAccount = loginService.getActiveAccount();
    Label accountLabel = new Label(activeAccountIndenter, SWT.NONE);
    accountLabel.setText(activeAccount.getEmail());
    FontUtil.convertFontToBold(accountLabel);

    Set<Account> accounts = loginService.listAccounts();
    if (accounts.size() > 1) {
      new Label(container, SWT.NONE).setText(Messages.MESSAGE_LABEL_OTHER_ACCOUNTS);

      Composite accountsIndenter = new Composite(container, SWT.NONE);
      GridLayoutFactory.swtDefaults().margins(EMAIL_LEFT_MARGIN, 0).applyTo(accountsIndenter);

      for (Account account : accounts) {
        if (!account.getEmail().equals(activeAccount.getEmail())) {
          Link link = new Link(accountsIndenter, SWT.NO_FOCUS);
          link.setText("<a href=\"" + account.getEmail() + "\">" + account.getEmail() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          link.addSelectionListener(new SwitchAccountOnClick());
        }
      }
    }
  }

  private void createButtons(Composite container) {
    Composite buttonArea = new Composite(container, SWT.NONE);
    GridDataFactory.defaultsFor(buttonArea).align(SWT.END, SWT.BEGINNING).applyTo(buttonArea);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(buttonArea);

    Button addAccountButton = new Button(buttonArea, SWT.PUSH);
    addAccountButton.setText(Messages.BUTTON_ACCOUNTS_PANEL_ADD_ACCOUNT);
    addAccountButton.addSelectionListener(new LogInOnClick());
    GridDataFactory.defaultsFor(addAccountButton).applyTo(addAccountButton);

    boolean loggedIn = loginService.getActiveAccount() != null;
    if (loggedIn) {
      Button logOutButton = new Button(buttonArea, SWT.PUSH);
      logOutButton.setText(Messages.BUTTON_ACCOUNTS_PANEL_LOGOUT);
      logOutButton.addSelectionListener(new LogOutOnClick());
      GridDataFactory.defaultsFor(logOutButton).applyTo(logOutButton);
    }
  }

  private class LogInOnClick extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent event) {
      close();
      loginService.logIn(null /* no custom dialog title */);
    }
  };

  private class LogOutOnClick extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent event) {
      if (MessageDialog.openConfirm(getShell(),
          Messages.LOGOUT_CONFIRM_DIALOG_TITILE, Messages.LOGOUT_CONFIRM_DIALOG_MESSAGE)) {
        close();
        loginService.logOutAll();
      }
    }
  };

  private class SwitchAccountOnClick extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent event) {
      String email = event.text;
      loginService.switchActiveAccount(email);

      close();
      new AccountsPanel(getParentShell(), loginService, showAtCenter).open();
    }
  };
}
