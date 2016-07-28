package com.google.cloud.tools.eclipse.appengine.facets;

import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;

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
}
