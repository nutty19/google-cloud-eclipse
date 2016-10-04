package com.google.cloud.tools.eclipse.appengine.facets;

import static org.mockito.Mockito.when;

import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppEngineFlexFacetTest {
  @Mock private IFacetedProject facetedProject;

  @Test
  public void testFlexFacetExists() {
    Assert.assertTrue(
        ProjectFacetsManager.isProjectFacetDefined("com.google.cloud.tools.eclipse.appengine.facets.flex"));
  }

  @Test
  public void testHasAppEngineFacet_withFacet() {
    IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(AppEngineFlexFacet.ID);
    when(facetedProject.hasProjectFacet(projectFacet)).thenReturn(true);

    Assert.assertTrue(AppEngineFlexFacet.hasAppEngineFacet(facetedProject));
  }

  @Test
  public void testHasAppEngineFacet_withoutFacet() {
    IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(AppEngineFlexFacet.ID);
    when(facetedProject.hasProjectFacet(projectFacet)).thenReturn(false);

    Assert.assertFalse(AppEngineFlexFacet.hasAppEngineFacet(facetedProject));
  }
}
