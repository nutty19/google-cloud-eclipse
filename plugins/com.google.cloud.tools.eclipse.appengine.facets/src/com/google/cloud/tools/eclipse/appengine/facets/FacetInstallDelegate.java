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
import org.apache.maven.model.io.DefaultModelWriter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    if (MavenUtils.hasMavenNature(project)) { 
      addAppEngineJarsToMavenProject(project, monitor);
    } else {
      addAppEngineJarsToClasspath(project, monitor);
    }
    createConfigFiles(project, monitor);
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

    List<Dependency> currentDependecies = pom.getDependencies();
    List<Dependency> dependencies = createMavenDependecies(currentDependecies);
    pom.setDependencies(dependencies);

    Properties properties = pom.getProperties();
    updatePomProperties(properties, pom.getArtifactId());

    DefaultModelWriter writer = new DefaultModelWriter();
    try {
      writer.write(pom.getPomFile(), null, pom);
    } catch (IOException e) {
      //throw new CoreException(e.getMessage());
    }
  }

  // TODO: move to maven util
  public static List<Dependency> createMavenDependecies(List<Dependency> currentDependecies) {
    Map<String, Dependency> dep = new HashMap<String, Dependency>();
    for (Dependency dependency : currentDependecies) {
      dep.put(dependency.toString(), dependency);
    }

    Dependency appEngineApiDependency = new Dependency();
    appEngineApiDependency.setGroupId("com.google.appengine");
    appEngineApiDependency.setArtifactId("appengine-api-1.0-sdk");
    appEngineApiDependency.setVersion("${appengine.version}");
    appEngineApiDependency.setScope("");

    String appEngineApiString = appEngineApiDependency.toString();
    if (!dep.containsKey(appEngineApiString)) {
      dep.put(appEngineApiString, appEngineApiDependency);
    }

    Dependency servletApiDependency = new Dependency();
    servletApiDependency.setGroupId("javax.servlet");
    servletApiDependency.setArtifactId("servlet-api");
    servletApiDependency.setVersion("2.5");
    servletApiDependency.setScope("provided");

    String servletApiString = servletApiDependency.toString();
    if (!dep.containsKey(servletApiString)) {
      dep.put(servletApiString, servletApiDependency);
    }

    Dependency jstlDependecy = new Dependency();
    jstlDependecy.setGroupId("jstl");
    jstlDependecy.setArtifactId("jstl");
    jstlDependecy.setVersion("1.2");

    String jstlString = jstlDependecy.toString();
    if (!dep.containsKey(jstlString)) {
      dep.put(jstlString, jstlDependecy);
    }

    Dependency appEngineTestingDependency = new Dependency();
    appEngineTestingDependency.setGroupId("com.google.appengine");
    appEngineTestingDependency.setArtifactId("appengine-testing");
    appEngineTestingDependency.setVersion("${appengine.version}");
    appEngineTestingDependency.setScope("test");

    String appEngineTestingString = appEngineTestingDependency.toString();
    if (!dep.containsKey(appEngineTestingString)) {
      dep.put(appEngineTestingString, appEngineTestingDependency);
    }

    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubs");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");

    String appEngineApiStubsString = appEngineApiStubsDependency.toString();
    if (!dep.containsKey(appEngineApiStubsString)) {
      dep.put(appEngineApiStubsString, appEngineApiStubsDependency);
    }

    List<Dependency> finalDepList = new ArrayList<Dependency>();
    finalDepList.addAll(dep.values());
    return finalDepList;
  }

  private static void updatePomProperties(Properties properties, String artifactId) {
    if(!properties.containsKey("app.id")) {
      properties.setProperty("app.id", artifactId);
    }

    if(!properties.containsKey("app.version")) {
      properties.setProperty("app.version", "1");
    }

    if(!properties.containsKey("appengine.version")) {
      properties.setProperty("appengine.version", "1.9.38");
    }

    if(!properties.containsKey("gcloud.plugin.version")) {
      properties.setProperty("gcloud.plugin.version", "2.0.9.111.v20160527");
    }
  }

}
