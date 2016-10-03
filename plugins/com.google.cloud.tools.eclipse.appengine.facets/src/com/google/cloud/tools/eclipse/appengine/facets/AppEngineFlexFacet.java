package com.google.cloud.tools.eclipse.appengine.facets;

import org.eclipse.wst.common.project.facet.core.IFacetedProject;

import com.google.cloud.tools.eclipse.util.FacetedProjectHelper;

public class AppEngineFlexFacet {
  public static final String ID = "com.google.cloud.tools.eclipse.appengine.facets.flex";

  /**
   * Returns true if project has the App Engine Flex facet and false otherwise.
   *
   * @param project should not be null
   * @return true if project has the App Engine Flex facet and false otherwise
   */
  public static boolean hasAppEngineFacet(IFacetedProject project) {
    FacetedProjectHelper facetedProjectHelper = new FacetedProjectHelper();
    return facetedProjectHelper.projectHasFacet(project, ID);
  }

}
