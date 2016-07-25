package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.classpathdep.UpdateClasspathAttributeUtil;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.eclipse.sdk.CloudSdkProvider;
import com.google.cloud.tools.eclipse.util.MavenUtils;

public class FacetInstallDelegate implements IDelegate {
  @Override
  public void execute(IProject project,
                      IProjectFacetVersion version,
                      Object config,
                      IProgressMonitor monitor) throws CoreException {
    if (!MavenUtils.hasMavenNature(project)) { // Maven handles classpath in maven projects.
      updateClasspath(project, monitor);
    }

  }

  public static void installAppEngineRuntime(IFacetedProject project, IProgressMonitor monitor)
      throws CoreException {
    Set<IProjectFacetVersion> facets = new HashSet<>();
    facets.add(WebFacetUtils.WEB_25);
    Set<IRuntime> runtimes = RuntimeManager.getRuntimes(facets);
    project.setTargetedRuntimes(runtimes, monitor);

    if (RuntimeManager.isRuntimeDefined(AppEngineStandardFacet.DEFAULT_RUNTIME_NAME)) {
      IRuntime appEngineRuntime = RuntimeManager.getRuntime(AppEngineStandardFacet.DEFAULT_RUNTIME_NAME);
      project.setPrimaryRuntime(appEngineRuntime, monitor);
    } else { // Create a new App Engine runtime
      IRuntimeType appEngineRuntimeType = ServerCore.findRuntimeType(AppEngineStandardFacet.DEFAULT_RUNTIME_ID);
      if (appEngineRuntimeType == null) {
        throw new NullPointerException("Could not find " + AppEngineStandardFacet.DEFAULT_RUNTIME_NAME + " runtime type");
      }

      IRuntimeWorkingCopy appEngineRuntimeWorkingCopy
          = appEngineRuntimeType.createRuntime(null, monitor);
      CloudSdk cloudSdk = new CloudSdkProvider().getCloudSdk();
      if (cloudSdk != null) {
        java.nio.file.Path sdkLocation = cloudSdk.getJavaAppEngineSdkPath();
        if (sdkLocation != null) {
          IPath sdkPath = Path.fromOSString(sdkLocation.toAbsolutePath().toString());
          appEngineRuntimeWorkingCopy.setLocation(sdkPath);
        }
      }

      org.eclipse.wst.server.core.IRuntime appEngineServerRuntime
          = appEngineRuntimeWorkingCopy.save(true, monitor);
      IRuntime appEngineFacetRuntime = FacetUtil.getRuntime(appEngineServerRuntime);
      if (appEngineFacetRuntime == null) {
        throw new NullPointerException("Could not locate App Engine facet runtime");
      }

      project.addTargetedRuntime(appEngineFacetRuntime, monitor);
      project.setPrimaryRuntime(appEngineFacetRuntime, monitor);
    }
  }

  /**
   * Checks to see if <code>facetedProject</code> has the App Engine facet installed. If not, it installs
   * the App Engine facet.
   *
   * @param facetedProject the workspace faceted project????????/
   * @param monitor the progress monitor
   * @throws CoreException
   */
  public static void installAppEngineFacet(IFacetedProject facetedProject, IProgressMonitor monitor)
      throws CoreException {
    IProjectFacet appEngineFacet = ProjectFacetsManager.getProjectFacet(AppEngineStandardFacet.ID);
    IProjectFacetVersion appEngineFacetVersion = appEngineFacet.getVersion(AppEngineStandardFacet.VERSION);

    // TODO: fix - this something cause the following error-------------------------------
    if (!facetedProject.hasProjectFacet(appEngineFacet)) {
      IFacetedProjectWorkingCopy workingCopy = facetedProject.createWorkingCopy();
      workingCopy.addProjectFacet(appEngineFacetVersion);
      workingCopy.commitChanges(monitor);
    }
  }

  /**
   * Adds jars associated with the App Engine facet
   */
  private void updateClasspath(IProject project, IProgressMonitor monitor) throws CoreException {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
    IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length + 1];
    System.arraycopy(rawClasspath, 0, newClasspath, 0, rawClasspath.length);
    newClasspath[newClasspath.length - 1] =
        JavaCore.newContainerEntry(new Path(AppEngineSdkClasspathContainer.CONTAINER_ID),
                                   new IAccessRule[0],
                                   new IClasspathAttribute[]{
                                       UpdateClasspathAttributeUtil.createDependencyAttribute(true /*isWebApp */)
                                   },
                                   true /* isExported */);
    javaProject.setRawClasspath(newClasspath, monitor);
  }

}
