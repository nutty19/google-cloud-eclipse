package com.google.cloud.tools.eclipse.appengine.facets;

import com.google.cloud.tools.eclipse.util.MavenUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.classpathdep.UpdateClasspathAttributeUtil;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import java.io.InputStream;

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
      SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
      addAppEngineJarsToClasspath(project, subMonitor.newChild(50));
      createConfigFiles(project, subMonitor.newChild(50));
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
    IClasspathEntry appEngineContainer = JavaCore.newContainerEntry(
        new Path(AppEngineSdkClasspathContainer.CONTAINER_ID),
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

  /**
   * Creates an appengine-web.xml file in the WEB-INF folder if it doesn't exist
   */
  private static void createConfigFiles(IProject project, IProgressMonitor monitor)
      throws CoreException {
    IFile appEngineWebXml = project.getFile(APPENGINE_WEB_XML_PATH);
    if (appEngineWebXml.exists()) {
      return;
    }

    IFolder configDir = project.getFolder(APPENGINE_WEB_XML_DIR);
    if (!configDir.exists()) {
      Path configDirPath = new Path(APPENGINE_WEB_XML_DIR);
      IContainer current = project;
      for ( int i = 0, n = configDirPath.segmentCount(); i < n; i++ )
      {
        final String name = configDirPath.segment( i );
        IFolder folder = current.getFolder( new Path( name ) );

        if (!folder.exists()) {
          folder.create( true, true, monitor );
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
