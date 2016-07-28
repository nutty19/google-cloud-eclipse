package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.List;

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
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;


public class AppEngineRuntimeChangeListener implements IFacetedProjectListener {

  @Override
  public void handleEvent(IFacetedProjectEvent event) {
    if (event.getType() != IFacetedProjectEvent.Type.PRIMARY_RUNTIME_CHANGED) {
      return;
    }
    
    // If the App Engine runtime has been added as the primary runtime
    // add the App Engine facet to the project
    final IPrimaryRuntimeChangedEvent runtimeChangeEvent = (IPrimaryRuntimeChangedEvent)event;
    IRuntime newRuntime = runtimeChangeEvent.getNewPrimaryRuntime();
    if (newRuntime == null) {
      return;
    }

    List<IRuntimeComponent> runtimeComponents = newRuntime.getRuntimeComponents();
    for (IRuntimeComponent comp : runtimeComponents) {
      if (comp.getRuntimeComponentType().getId().equals("com.google.appengine.runtime")) {        
        // Add the App Engine facet
        Job addFacetJob = new Job("Add App Engine facet...") {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            IFacetedProject project = runtimeChangeEvent.getProject();
            IStatus installStatus = Status.OK_STATUS;
            
            try {
              FacetInstallDelegate.installAppEngineFacet(project, monitor);
              return installStatus;
            } catch (CoreException e) {
              // Displays missing constraints that prevented facet installation
              installStatus = e.getStatus();
            }
            
            // Remove App Engine as primary runtime
            try {
              project.removeTargetedRuntime(runtimeChangeEvent.getNewPrimaryRuntime(), monitor);
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
    
  }

}
