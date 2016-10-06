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

package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

// TODO: persist values
public class FlexDeployPreferencesPanel extends DeployPreferencesPanel{
  private Button useValuesButton;
  private Label gaeConfigFolderLabel;
  private Text gaeConfigFolderText;
  private Label dockerFileLabel;
  private Text dockerFileText;

  public FlexDeployPreferencesPanel(Composite parent) {
    super(parent, SWT.NONE);
    createConfigurationFilesSection();
  }

  @Override
  public DataBindingContext getDataBindingContext() {
    return new DataBindingContext();
  }

  @Override
  public void resetToDefaults() {

  }

  @Override
  public boolean savePreferences() {
    return false;
  }

  private void createConfigurationFilesSection() {
    useValuesButton = new Button(this, SWT.CHECK);
    useValuesButton.setText(Messages.getString("use.config.values"));
    useValuesButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

    gaeConfigFolderLabel = new Label(this, SWT.LEFT);
    gaeConfigFolderLabel.setText(Messages.getString("config.folder.location"));
    gaeConfigFolderText = new Text(this, SWT.LEFT | SWT.SINGLE | SWT.BORDER);

    dockerFileLabel = new Label(this, SWT.LEFT);
    dockerFileLabel.setText(Messages.getString("docker.file.location"));
    dockerFileText = new Text(this, SWT.LEFT | SWT.SINGLE | SWT.BORDER);

    GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(this);
  }

}
