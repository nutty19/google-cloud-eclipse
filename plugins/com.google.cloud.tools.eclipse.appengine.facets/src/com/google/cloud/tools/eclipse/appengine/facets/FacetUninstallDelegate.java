package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.FacetUtil;
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
    if (!MavenUtils.hasMavenNature(project)) { // Maven handles classpath in maven projects.
      SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
      updateClasspath(project, subMonitor.newChild(50));
      uninstallAppEngineRuntime(project, subMonitor.newChild(50));
    }

  }

  private void updateClasspath(IProject project, IProgressMonitor monitor) throws CoreException {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
    IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length - 1];

    int appEngineContainerIndex = 0;
    boolean isAppEngineSdkPresent = false;
    for (int i = 0; i < rawClasspath.length; i++) {
      if (AppEngineSdkClasspathContainer.CONTAINER_ID.equals(rawClasspath[i].getPath().toString())) {
        isAppEngineSdkPresent = true;
      } else {
        newClasspath[appEngineContainerIndex++] = rawClasspath[i];
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
    Job uninstallJob = new Job("") {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        IFacetedProject facetedProject;
        try {
          facetedProject = ProjectFacetsManager.create(project);
          Set<IRuntime> targetedRuntimes = facetedProject.getTargetedRuntimes();

          for (IRuntime aRuntime : targetedRuntimes) {
            org.eclipse.wst.server.core.IRuntime runtime2 = FacetUtil.getRuntime(aRuntime);
            if (runtime2.getRuntimeType().getId().equals(AppEngineStandardFacet.DEFAULT_RUNTIME_ID)) {
              facetedProject.removeTargetedRuntime(aRuntime, monitor);
            }
          }
        } catch (CoreException e) {
          return e.getStatus();
        }
        return Status.OK_STATUS;
      }
    };
    uninstallJob.schedule();

  }
}
