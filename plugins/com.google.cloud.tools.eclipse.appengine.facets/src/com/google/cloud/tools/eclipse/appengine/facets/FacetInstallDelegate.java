/*******************************************************************************
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/

package com.google.cloud.tools.eclipse.appengine.facets;

import com.google.cloud.tools.eclipse.util.MavenUtils;
import com.google.cloud.tools.eclipse.util.templates.appengine.AppEngineTemplateUtility;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.classpathdep.UpdateClasspathAttributeUtil;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FacetInstallDelegate implements IDelegate {
  private final static String APPENGINE_WEB_XML = "appengine-web.xml";
  // TODO Change directory for dynamic web module.
  // Differentiate between project with web facets vs 'true' dynamic web modules?
  private final static String APPENGINE_WEB_XML_DIR = "src/main/webapp/WEB-INF/";
  private final static String APPENGINE_WEB_XML_PATH = APPENGINE_WEB_XML_DIR + APPENGINE_WEB_XML;

  @Override
  public void execute(IProject project,
                      IProjectFacetVersion version,
                      Object config,
                      IProgressMonitor monitor) throws CoreException {
    if (!MavenUtils.hasMavenNature(project)) { // Maven handles classpath in maven projects.
      addAppEngineJarsToClasspath(project, monitor);
      createConfigFiles(project, monitor);
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
    for (int i = 0; i < rawClasspath.length; i++) {
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
      for (int i = 0; i < configDirPath.segmentCount(); i++) {
        final String segment = configDirPath.segment( i );
        IFolder folder = current.getFolder(new Path(segment));

        if (!folder.exists()) {
          folder.create( true, true, monitor );
        }
        current = folder;
      }
      configDir = (IFolder) current;
    }

    appEngineWebXml.create(new ByteArrayInputStream(new byte[0]), true, monitor);
    String configFileLocation = appEngineWebXml.getLocation().toString();
    AppEngineTemplateUtility.createFileContent(
        configFileLocation, AppEngineTemplateUtility.APPENGINE_WEB_XML_TEMPLATE, new HashMap<String, String>());
  }

  private static void addAppEngineJarsToMavenProject(IProject project, IProgressMonitor monitor) throws CoreException {
    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
    Model pom = facade.getMavenProject(monitor).getModel();

    List<Dependency> dependencies = createMavenDependecies();
    for (Dependency dependency : dependencies) {
      pom.addDependency(dependency);
    }
    
    
    
  }

  private static List<Dependency> createMavenDependecies() {
    List<Dependency> dependencies = new ArrayList<Dependency>();

    Dependency appEngineApiDependency = new Dependency();
    appEngineApiDependency.setGroupId("com.google.appengine");
    appEngineApiDependency.setArtifactId("appengine-api-1.0-sdk");
    //d1.setVersion("${appengine.version}");
    appEngineApiDependency.setScope("");
    dependencies.add(appEngineApiDependency);

    Dependency servletApiDependency = new Dependency();
    servletApiDependency.setGroupId("javax.servlet");
    servletApiDependency.setArtifactId("servlet-api");
    servletApiDependency.setVersion("2.5");
    servletApiDependency.setScope("provided");
    dependencies.add(servletApiDependency);

    Dependency jstlDependecy = new Dependency();
    jstlDependecy.setGroupId("jstl");
    jstlDependecy.setArtifactId("jstl");
    jstlDependecy.setVersion("1.2");
    dependencies.add(jstlDependecy);

    Dependency appEngineTestingDependency = new Dependency();
    appEngineTestingDependency.setGroupId("com.google.appengine");
    appEngineTestingDependency.setArtifactId("appengine-testing");
    //appEngineTestingDependency.setVersion("${appengine.version}");
    appEngineTestingDependency.setScope("test");
    dependencies.add(appEngineTestingDependency);

    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubsk");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");
    dependencies.add(appEngineApiStubsDependency);

    return dependencies;
  }

}
