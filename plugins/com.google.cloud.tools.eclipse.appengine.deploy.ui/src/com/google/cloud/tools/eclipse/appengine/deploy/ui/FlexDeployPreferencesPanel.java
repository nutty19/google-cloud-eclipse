package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FlexDeployPreferencesPanel extends DeployPreferencesPanel{

  public FlexDeployPreferencesPanel(Composite parent) {
    super(parent, SWT.NONE);
    Label label = new Label(parent, SWT.NONE);
    label.setText("Flex deployment settings coming.");
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

}
