package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.common.project.facet.core.JavaFacetInstallConfig;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetInstallDataModelProvider;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.common.base.Preconditions;

// TODO: add tests
public class AppEngineStandardFacet {

  public static final String ID = "com.google.cloud.tools.eclipse.appengine.facet";

  static final String VERSION = "1";
  static final String DEFAULT_RUNTIME_ID = "com.google.appengine.standard.runtime";
  static final String DEFAULT_RUNTIME_NAME = "App Engine Standard";
  static final String INSTALL_ACTION_ID = "com.google.cloud.tools.eclipse.appengine.facet.install.action";
  static final String UNINSTALL_ACTION_ID = "com.google.cloud.tools.eclipse.appengine.facet.uninstall.action";

  /**
   * Returns true if project has the App Engine Standard facet and false otherwise.
   *
   * @param project the project; project should not be null
   * @return true if project has the App Engine Standard facet and false otherwise
   */
  public static boolean hasAppEngineFacet(IFacetedProject project) {
    Preconditions.checkNotNull(project, "project is null");

    IProjectFacet appEngineFacet = ProjectFacetsManager.getProjectFacet(ID);
    return project.hasProjectFacet(appEngineFacet);
  }

  /**
   * Returns true is <code>runtime</code> is an App Engine Standard runtime and false otherwise
   *
   * @param runtime the facet runtime; runtime should not be null
   * @return true is <code>runtime</code> is an App Engine Standard runtime and false otherwise
   */
  public static boolean isAppEngineRunime(IRuntime runtime) {
    Preconditions.checkNotNull(runtime, "runtime is null");

    org.eclipse.wst.server.core.IRuntime serverRuntime = FacetUtil.getRuntime(runtime);
    if (serverRuntime != null) {
      IRuntimeType runtimeType = serverRuntime.getRuntimeType();
      return runtimeType.getId().equals(DEFAULT_RUNTIME_ID);
    } else {
      return false;
    }
  }

  /**
   * Checks to see if <code>facetedProject</code> has the App Engine facet installed. If not, it installs
   * the App Engine facet.
   *
   * @param facetedProject the workspace faceted project
   * @param monitor the progress monitor
   * @throws CoreException
   */
  public static void installAppEngineFacet(IFacetedProject facetedProject, boolean installDependentFacets, IProgressMonitor monitor)
      throws CoreException {
    // Install required App Engine facets Java 1.7 and Dynamic Web Module 2.5
    if (installDependentFacets) {
      installJavaFacet(facetedProject, monitor);
      installWebFacet(facetedProject, monitor);
    }

    IProjectFacet appEngineFacet = ProjectFacetsManager.getProjectFacet(AppEngineStandardFacet.ID);
    IProjectFacetVersion appEngineFacetVersion = appEngineFacet.getVersion(AppEngineStandardFacet.VERSION);

    if (!facetedProject.hasProjectFacet(appEngineFacet)) {
      facetedProject.installProjectFacet(appEngineFacetVersion, null, monitor);
    }
  }

  /**
   * If App Engine runtimes exist in the workspace, add them to the list of targeted runtimes
   * of <code>project</code>, otherwise create a new App Engine runtime and add it to the list
   * of targeted runtimes.
   *
   * @param project the workspace faceted project
   * @param force true if all runtime instances should be added to the <code>project</code> even if targeted list of
   *  <code>project</code> already includes App Engine runtime instances and false otherwise
   * @param monitor the progress monitor
   * @throws CoreException if method fails for any reason
   */
  public static void installAllAppEngineRuntimes(IFacetedProject project, boolean force, IProgressMonitor monitor)
      throws CoreException {
    // If the project already has an App Engine runtime instance and force is false
    // do not add any other App Engine runtime instance to the list of targeted runtimes
    Set<IRuntime> existingTargetedRuntimes = project.getTargetedRuntimes();
    if (!existingTargetedRuntimes.isEmpty()) {
      for (IRuntime existingTargetedRuntime : existingTargetedRuntimes) {
        if (AppEngineStandardFacet.isAppEngineRunime(existingTargetedRuntime) && !force) {
          return;
        }
      }
    }

    org.eclipse.wst.server.core.IRuntime[] appEngineRuntimes = getAppEngineRuntime();
    if (appEngineRuntimes.length > 0) {
      IRuntime appEngineFacetRuntime = null;
      for(int index = 0; index < appEngineRuntimes.length; index++) {
        appEngineFacetRuntime = FacetUtil.getRuntime(appEngineRuntimes[index]);
        project.addTargetedRuntime(appEngineFacetRuntime, monitor);
      }
      project.setPrimaryRuntime(appEngineFacetRuntime, monitor);
    } else { // Create a new App Engine runtime
      IRuntimeType appEngineRuntimeType =
          ServerCore.findRuntimeType(AppEngineStandardFacet.DEFAULT_RUNTIME_ID);
      if (appEngineRuntimeType == null) {
        throw new NullPointerException("Could not find " + AppEngineStandardFacet.DEFAULT_RUNTIME_NAME + " runtime type");
      }

      IRuntimeWorkingCopy appEngineRuntimeWorkingCopy
          = appEngineRuntimeType.createRuntime(null, monitor);
      CloudSdk cloudSdk = new CloudSdk.Builder().build();
      if (cloudSdk != null) {
        java.nio.file.Path sdkLocation = cloudSdk.getJavaAppEngineSdkPath();
        if (sdkLocation != null) {
          IPath sdkPath = Path.fromOSString(sdkLocation.toAbsolutePath().toString());
          appEngineRuntimeWorkingCopy.setLocation(sdkPath);
        }
      }

      org.eclipse.wst.server.core.IRuntime appEngineServerRuntime =
          appEngineRuntimeWorkingCopy.save(true, monitor);
      IRuntime appEngineFacetRuntime = FacetUtil.getRuntime(appEngineServerRuntime);
      if (appEngineFacetRuntime == null) {
        throw new NullPointerException("Could not locate App Engine facet runtime");
      }

      project.addTargetedRuntime(appEngineFacetRuntime, monitor);
      project.setPrimaryRuntime(appEngineFacetRuntime, monitor);
    }
  }

  /**
   * Installs Java 1.7 facet if it doesn't already exits in <code>factedProject</code>
   */
  private static void installJavaFacet(IFacetedProject facetedProject, IProgressMonitor monitor)
      throws CoreException {
    if (facetedProject.hasProjectFacet(JavaFacet.VERSION_1_7)) {
      return;
    }

    JavaFacetInstallConfig javaConfig = new JavaFacetInstallConfig();
    List<IPath> sourcePaths = new ArrayList<>();
    sourcePaths.add(new Path("src/main/java"));
    sourcePaths.add(new Path("src/test/java"));
    javaConfig.setSourceFolders(sourcePaths);
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_7, javaConfig, monitor);
  }

  /**
   * Installs Dynamic Web Module 2.5 facet if it doesn't already exits in <code>factedProject</code>
   */
  private static void installWebFacet(IFacetedProject facetedProject, IProgressMonitor monitor)
      throws CoreException {
    if (facetedProject.hasProjectFacet(WebFacetUtils.WEB_25)) {
      return;
    }

    IDataModel webModel = DataModelFactory.createDataModel(new WebFacetInstallDataModelProvider());
    webModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR, false);
    webModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, false);
    webModel.setBooleanProperty(IWebFacetInstallDataModelProperties.INSTALL_WEB_LIBRARY, false);
    webModel.setStringProperty(IWebFacetInstallDataModelProperties.CONFIG_FOLDER, "src/main/webapp");
    facetedProject.installProjectFacet(WebFacetUtils.WEB_25, webModel, monitor);
  }

  // TODO: find a more general form of this method
  private static org.eclipse.wst.server.core.IRuntime[] getAppEngineRuntime() {
    org.eclipse.wst.server.core.IRuntime[] allRuntimes = ServerCore.getRuntimes();
    List<org.eclipse.wst.server.core.IRuntime> appEngineRuntimes = new ArrayList<>();

    for (int i = 0; i < allRuntimes.length; i++) {
      if (allRuntimes[i].getRuntimeType().getId().equals(AppEngineStandardFacet.DEFAULT_RUNTIME_ID)) {
        appEngineRuntimes.add(allRuntimes[i]);
      }
    }

    org.eclipse.wst.server.core.IRuntime[] appEngineRuntimesArray =
        new org.eclipse.wst.server.core.IRuntime[appEngineRuntimes.size()];
    return appEngineRuntimes.toArray(appEngineRuntimesArray);
  }

}
