package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.widgets.Composite;

abstract class DeployPreferencesPanel extends Composite {

  DeployPreferencesPanel(Composite parent, int style) {
    super(parent, style);
  }

  public abstract DataBindingContext getDataBindingContext();

  public abstract void resetToDefaults();

  public abstract boolean savePreferences();

}
