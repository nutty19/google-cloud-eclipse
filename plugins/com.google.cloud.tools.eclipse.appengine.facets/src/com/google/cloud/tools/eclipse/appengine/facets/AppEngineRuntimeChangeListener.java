package com.google.cloud.tools.eclipse.appengine.facets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
import org.eclipse.wst.common.project.facet.core.events.IPrimaryRuntimeChangedEvent;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;

/**
 * Parses for events where the App Engine runtime has been made the primary
 * runtime for a project. If this project does not have the App Engine facet
 * installed the App Engine facet will be installed.
 */
public class AppEngineRuntimeChangeListener implements IFacetedProjectListener {

  @Override
  public void handleEvent(IFacetedProjectEvent event) {
    // PRIMARY_RUNTIME_CHANGED occurs in scenarios including when you select runtimes on the
    // "New Faceted Project" wizard and the "New Dynamic Web Project" wizard.
    // IFacetedProjectEvent.Type.TARGETED_RUNTIMES_CHANGED does not happen
    if (event.getType() != IFacetedProjectEvent.Type.PRIMARY_RUNTIME_CHANGED) {
      return;
    }
    
    IPrimaryRuntimeChangedEvent runtimeChangeEvent = (IPrimaryRuntimeChangedEvent)event;
    final IRuntime newRuntime = runtimeChangeEvent.getNewPrimaryRuntime();
    if (newRuntime == null) {
      return;
    }

    if (!AppEngineStandardFacet.isAppEngineRunime(newRuntime)) {
      return;
    }

    // Check if the App Engine facet has been installed in the project
    final IFacetedProject project = runtimeChangeEvent.getProject();
    if (AppEngineStandardFacet.hasAppEngineFacet(project)) {
      return;
    }

    // TODO: do we need to run this as a job?
    // Add the App Engine facet
    Job addFacetJob = new Job("Add App Engine facet to " + project.getProject().getName()) {

      @Override
      protected IStatus run(IProgressMonitor monitor) {

        IStatus installStatus = Status.OK_STATUS;

        try {
          AppEngineStandardFacet.installAppEngineFacet(project, false /* installDependentFacets */, monitor);
          return installStatus;
        } catch (CoreException e) {
          // Displays missing constraints that prevented facet installation
          installStatus = e.getStatus();
        }

        // Remove App Engine as primary runtime
        try {
          project.removeTargetedRuntime(newRuntime, monitor);
          return installStatus;
        } catch (CoreException e) {
          MultiStatus multi = (MultiStatus) installStatus;
          multi.merge(e.getStatus());
          return multi;
        }
      }

    };
    addFacetJob.schedule();
  }

}
