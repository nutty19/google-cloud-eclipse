package com.google.cloud.tools.eclipse.appengine.deploy.standard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.annotations.VisibleForTesting;

public class StandardDeployPreferences {

  public static final String PREFERENCE_STORE_QUALIFIER = "com.google.cloud.tools.eclipse.appengine.deploy";

  static final String PREF_PROJECT_ID = "project.id";
  static final String PREF_OVERRIDE_DEFAULT_VERSIONING = "project.version.overrideDefault"; // boolean
  static final String PREF_CUSTOM_VERSION = "project.version";
  static final String PREF_ENABLE_AUTO_PROMOTE = "project.promote"; // boolean
  static final String PREF_OVERRIDE_DEFAULT_BUCKET = "project.bucket.overrideDefault"; // boolean
  static final String PREF_CUSTOM_BUCKET = "project.bucket";
  static final String PREF_STOP_PREVIOUS_VERSION = "project.previousVersion.stop";

  private IEclipsePreferences preferenceStore;
  public static final StandardDeployPreferences DEFAULT;

  static {
    DEFAULT = new StandardDeployPreferences(DeployPreferenceInitializer.getDefaultPreferences());
  }

  public StandardDeployPreferences(IProject project) {
    this(new ProjectScope(project).getNode(PREFERENCE_STORE_QUALIFIER));
  }

  @VisibleForTesting
  StandardDeployPreferences(IEclipsePreferences preferences) {
    preferenceStore = preferences;
  }

  public void save() throws BackingStoreException {
    preferenceStore.flush();
  }

  public String getProjectId() {
    return preferenceStore.get(PREF_PROJECT_ID, DeployPreferenceInitializer.DEFAULT_PROJECT_ID);
  }

  public void setProjectId(String projectId) {
    preferenceStore.put(PREF_PROJECT_ID, projectId);
  }

  public boolean isOverrideDefaultVersioning() {
    return preferenceStore.getBoolean(PREF_OVERRIDE_DEFAULT_VERSIONING,
                                      DeployPreferenceInitializer.DEFAULT_OVERRIDE_DEFAULT_VERSIONING);
  }

  public void setOverrideDefaultVersioning(boolean overrideDefaultVersioning) {
    preferenceStore.putBoolean(PREF_OVERRIDE_DEFAULT_VERSIONING, overrideDefaultVersioning);
  }

  public String getVersion() {
    return preferenceStore.get(PREF_CUSTOM_VERSION, DeployPreferenceInitializer.DEFAULT_CUSTOM_VERSION);
  }

  public void setVersion(String version) {
    preferenceStore.put(PREF_CUSTOM_VERSION, version);
  }

  public boolean isAutoPromote() {
    return preferenceStore.getBoolean(PREF_ENABLE_AUTO_PROMOTE,
                                      DeployPreferenceInitializer.DEFAULT_ENABLE_AUTO_PROMOTE);
  }

  public void setAutoPromote(boolean autoPromote) {
    preferenceStore.putBoolean(PREF_ENABLE_AUTO_PROMOTE, autoPromote);
  }

  public boolean isOverrideDefaultBucket() {
    return preferenceStore.getBoolean(PREF_OVERRIDE_DEFAULT_BUCKET,
                                      DeployPreferenceInitializer.DEFAULT_OVERRIDE_DEFAULT_BUCKET);
  }

  public void setOverrideDefaultBucket(boolean overrideDefaultBucket) {
    preferenceStore.putBoolean(PREF_OVERRIDE_DEFAULT_BUCKET, overrideDefaultBucket);
  }

  public String getBucket() {
    return preferenceStore.get(PREF_CUSTOM_BUCKET, DeployPreferenceInitializer.DEFAULT_CUSTOM_BUCKET);
  }

  public void setBucket(String bucket) {
    preferenceStore.put(PREF_CUSTOM_BUCKET, bucket);
  }

  public boolean isStopPreviousVersion() {
    return preferenceStore.getBoolean(PREF_STOP_PREVIOUS_VERSION,
                                      DeployPreferenceInitializer.DEFAULT_STOP_PREVIOUS_VERSION);
  }

  public void setStopPreviousVersion(boolean stopPreviousVersion) {
    preferenceStore.putBoolean(PREF_STOP_PREVIOUS_VERSION, stopPreviousVersion);
  }

}
