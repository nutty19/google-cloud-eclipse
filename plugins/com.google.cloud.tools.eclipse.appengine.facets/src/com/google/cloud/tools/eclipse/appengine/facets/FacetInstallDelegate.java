package com.google.cloud.tools.eclipse.appengine.facets;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.eclipse.util.MavenUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FacetInstallDelegate implements IDelegate {
  private final static String APPENGINE_WEB_XML = "appengine-web.xml";
  private final static String APPENGINE_WEB_XML_PATH = "src/main/webapp/WEB-INF/appengine-web.xml";
  private final static String APPENGINE_WEB_XML_DIR = "src/main/webapp/WEB-INF/";

  @Override
  public void execute(IProject project,
                      IProjectFacetVersion version,
                      Object config,
                      IProgressMonitor monitor) throws CoreException {
    if (!MavenUtils.hasMavenNature(project)) { // Maven handles classpath in maven projects.
      addAppEngineJarsToClasspath(project, monitor);
      IFacetedProject facetedProject = ProjectFacetsManager.create(project);
      installAppEngineRuntime(facetedProject, monitor);
      createConfigFiles(project, monitor);
    }

  }

  /**
   * If App Engine runtimes exist in the workspace, add them to the list of targeted runtimes
   * of <code>project</code>, otherwise create a new App Engine runtime and add it to the list
   * of targeted runtimes.
   *
   * @param project the workspace faceted project
   * @param monitor the progress monitor
   * @throws CoreException
   */
  public static void installAppEngineRuntime(IFacetedProject project, IProgressMonitor monitor)
      throws CoreException {
    // TODO what happens if the project already has App Engine runtime?
    Set<IProjectFacetVersion> facets = new HashSet<>();
    facets.add(WebFacetUtils.WEB_25);
    Set<IRuntime> runtimes = RuntimeManager.getRuntimes(facets);
    final IFacetedProjectWorkingCopy fpwc = project.createWorkingCopy();
    fpwc.setTargetedRuntimes(runtimes);
    org.eclipse.wst.server.core.IRuntime[] appEngineRuntimes = getAppEngineRuntime();

    if (appEngineRuntimes.length > 0) {
      IRuntime appEngineFacetRuntime = null;
      for(int index = 0; index < appEngineRuntimes.length; index++) {
        appEngineFacetRuntime = FacetUtil.getRuntime(appEngineRuntimes[index]);
        fpwc.addTargetedRuntime(appEngineFacetRuntime);
      }
      fpwc.setPrimaryRuntime(appEngineFacetRuntime);
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

      fpwc.addTargetedRuntime(appEngineFacetRuntime);
      fpwc.setPrimaryRuntime(appEngineFacetRuntime);
    }

    // This is to prevent the error "Cannot modify faceted project from within a facet delegate"
    Job commitChanges = new Job("Add App Engine runtime to " + fpwc.getProjectName()) {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          fpwc.commitChanges(monitor);
        } catch (CoreException e) {
          return e.getStatus();
        }
        return Status.OK_STATUS;
      }

    };
    commitChanges.schedule();
  }

  /**
   * Checks to see if <code>facetedProject</code> has the App Engine facet installed. If not, it installs
   * the App Engine facet.
   *
   * @param facetedProject the workspace faceted project
   * @param monitor the progress monitor
   * @throws CoreException
   */
  public static void installAppEngineFacet(IFacetedProject facetedProject, IProgressMonitor monitor)
      throws CoreException {
    // TODO: also install Dynamic Web Module 2.5 and Java 1.7?
    IProjectFacet appEngineFacet = ProjectFacetsManager.getProjectFacet(AppEngineStandardFacet.ID);
    IProjectFacetVersion appEngineFacetVersion = appEngineFacet.getVersion(AppEngineStandardFacet.VERSION);

    if (!facetedProject.hasProjectFacet(appEngineFacet)) {
      IFacetedProjectWorkingCopy workingCopy = facetedProject.createWorkingCopy();
      workingCopy.addProjectFacet(appEngineFacetVersion);
      workingCopy.commitChanges(monitor);
    }
  }

  /**
   * Adds jars associated with the App Engine facet if they don't already exist in
   * <code>project</code>
   */
  private void addAppEngineJarsToClasspath(IProject project, IProgressMonitor monitor)
      throws CoreException {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
    IClasspathEntry appEngineContainer = JavaCore.newContainerEntry(new Path(AppEngineSdkClasspathContainer.CONTAINER_ID),
        new IAccessRule[0],
        new IClasspathAttribute[]{
            UpdateClasspathAttributeUtil.createDependencyAttribute(true /*isWebApp */)
        },
        true /* isExported */);

    // Check if App Engine container entry already exists
    for (int i = 0, length = rawClasspath.length; i < length; i++) {
      if (rawClasspath[i].equals(appEngineContainer)) {
        return;
      }
    }

    IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length + 1];
    System.arraycopy(rawClasspath, 0, newClasspath, 0, rawClasspath.length);
    newClasspath[newClasspath.length - 1] = appEngineContainer;
    javaProject.setRawClasspath(newClasspath, monitor);
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

  /**
   * Creates an appengine-web.xml file in the WEB-INF folder if it doesn't exist
   * @throws CoreException
   */
  private static void createConfigFiles(IProject project, IProgressMonitor monitor)
      throws CoreException {
    IFile appEngineWebXml = project.getFile(APPENGINE_WEB_XML_PATH);
    if (appEngineWebXml.exists()) {
      return;
    }

    IFolder configDir = project.getFolder(APPENGINE_WEB_XML_DIR);
    if(!configDir.exists()) {
      Path configDirPath = new Path(APPENGINE_WEB_XML_DIR);
      IContainer current = project;
      for( int i = 0, n = configDirPath.segmentCount(); i < n; i++ )
      {
        final String name = configDirPath.segment( i );
        IFolder folder = current.getFolder( new Path( name ) );

        if(!folder.exists()) {
          folder.create( true, true, null );
        }
        current = folder;
      }
      configDir = (IFolder) current;
    }

    InputStream in = FacetInstallDelegate.class.getResourceAsStream("templates/" + APPENGINE_WEB_XML + ".ftl");
    if (in == null) {
      IStatus status = new Status(Status.ERROR, "todo plugin ID",
          "Could not load template for " + APPENGINE_WEB_XML, null);
      throw new CoreException(status);
    }

    IFile configFile = configDir.getFile(APPENGINE_WEB_XML);
    if (!configFile.exists()) {
      configFile.create(in, true, monitor);
    }
  }
}
