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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;

import com.google.cloud.tools.eclipse.util.MavenUtils;

public class FacetUninstallDelegate implements IDelegate {

  @Override
  public void execute(IProject project, IProjectFacetVersion version, Object config,
      IProgressMonitor monitor) throws CoreException {
    if (MavenUtils.hasMavenNature(project)) {
      removeAppEngineJarsFromMavenProject(project, monitor);
    } else {
      removeAppEngineJarsFromClasspath(project, monitor);
    }
    uninstallAppEngineRuntime(project, monitor);
  }

  private void removeAppEngineJarsFromClasspath(IProject project, IProgressMonitor monitor) throws CoreException {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
    IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length - 1];

    int appEngineContainerIndex = 0;
    boolean isAppEngineSdkPresent = false;
    for (IClasspathEntry entry : rawClasspath) {
      if (AppEngineSdkClasspathContainer.CONTAINER_ID.equals(entry.getPath().toString())) {
        isAppEngineSdkPresent = true;
      } else {
        newClasspath[appEngineContainerIndex++] = entry;
      }
    }

    if(isAppEngineSdkPresent) {
      javaProject.setRawClasspath(newClasspath, monitor);
    }
  }

  /**
   * Removes all the App Engine server runtimes from the list of targeted runtimes for
   * <code>project</code>.
   */
  private void uninstallAppEngineRuntime(final IProject project, IProgressMonitor monitor) {
    Job uninstallJob = new Job("Uninstall App Engine runtimes in " + project.getName()) {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          IFacetedProject facetedProject = ProjectFacetsManager.create(project);
          Set<IRuntime> targetedRuntimes = facetedProject.getTargetedRuntimes();

          for (IRuntime targetedRuntime : targetedRuntimes) {
            if (AppEngineStandardFacet.isAppEngineStandardRuntime(targetedRuntime)) {
              facetedProject.removeTargetedRuntime(targetedRuntime, monitor);
            }
          }
          return Status.OK_STATUS;
        } catch (CoreException ex) {
          return ex.getStatus();
        }
      }
    };
    uninstallJob.schedule();

  }

  private void removeAppEngineJarsFromMavenProject(IProject project, IProgressMonitor monitor) throws CoreException {
    IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
    Model pom = facade.getMavenProject(monitor).getModel();

    List<Dependency> currentDependecies = pom.getDependencies();
    List<Dependency> dependencies = updateMavenDependecies(currentDependecies);
    pom.setDependencies(dependencies);

    Properties properties = pom.getProperties();
    updatePomProperties(properties, pom.getArtifactId());

    DefaultModelWriter writer = new DefaultModelWriter();
    try {
      writer.write(pom.getPomFile(), null, pom);
    } catch (IOException e) {
      // throw new CoreException(e.getMessage());
    }
  }

  private List<Dependency> updateMavenDependecies(List<Dependency> currentDependecies) {
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
      dep.remove(appEngineApiString);
    }

    Dependency servletApiDependency = new Dependency();
    servletApiDependency.setGroupId("javax.servlet");
    servletApiDependency.setArtifactId("servlet-api");
    servletApiDependency.setVersion("2.5");
    servletApiDependency.setScope("provided");

    String servletApiString = servletApiDependency.toString();
    if (!dep.containsKey(servletApiString)) {
      dep.remove(servletApiString);
    }

    Dependency jstlDependecy = new Dependency();
    jstlDependecy.setGroupId("jstl");
    jstlDependecy.setArtifactId("jstl");
    jstlDependecy.setVersion("1.2");

    String jstlString = jstlDependecy.toString();
    if (!dep.containsKey(jstlString)) {
      dep.remove(jstlString);
    }

    Dependency appEngineTestingDependency = new Dependency();
    appEngineTestingDependency.setGroupId("com.google.appengine");
    appEngineTestingDependency.setArtifactId("appengine-testing");
    appEngineTestingDependency.setVersion("${appengine.version}");
    appEngineTestingDependency.setScope("test");

    String appEngineTestingString = appEngineTestingDependency.toString();
    if (!dep.containsKey(appEngineTestingString)) {
      dep.remove(appEngineTestingString);
    }

    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubs");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");
    String appEngineApiStubsString = appEngineApiStubsDependency.toString();
    if (!dep.containsKey(appEngineApiStubsString)) {
      dep.remove(appEngineApiStubsString);
    }

    List<Dependency> finalDepList = new ArrayList<Dependency>();
    finalDepList.addAll(dep.values());
    return finalDepList;
  }

  //visible for testing
  public static void updatePomProperties(Properties properties, String artifactId) {
    if (!properties.containsKey("app.id")) {
      properties.remove("app.id");
    }

    if (!properties.containsKey("app.version")) {
      properties.remove("app.version");
    }

    if (!properties.containsKey("appengine.version")) {
      properties.remove("appengine.version");
    }

    if (!properties.containsKey("gcloud.plugin.version")) {
      properties.remove("gcloud.plugin.version");
    }
  }
}
