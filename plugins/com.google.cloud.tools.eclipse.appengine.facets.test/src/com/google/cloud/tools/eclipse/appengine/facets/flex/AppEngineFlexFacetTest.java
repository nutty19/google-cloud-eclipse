package com.google.cloud.tools.eclipse.appengine.facets.flex;

import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Assert;
import org.junit.Test;

import com.google.cloud.tools.eclipse.appengine.facets.AppEngineFlexFacet;

public class AppEngineFlexFacetTest {
  @Test
  public void testFlexFacetExists() {
    Assert.assertTrue(ProjectFacetsManager.isProjectFacetDefined(AppEngineFlexFacet.ID));
  }
}
