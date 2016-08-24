package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.mockito.Mockito;

import org.junit.Assert;;

public class FacetInstallDelegateTest {
  private FacetInstallDelegate delegate = new FacetInstallDelegate();

  @Test
  public void testMavenNature() throws CoreException {
    IProject project = Mockito.mock(IProject.class);
    Mockito.when(project.hasNature("org.eclipse.m2e.core.maven2Nature")).thenReturn(true);
    Mockito.when(project.isAccessible()).thenReturn(true);

    delegate.execute(project, null, null, null);
  }

  @Test
  public void testSomething() {
    Dependency appEngineApiDependency = new Dependency();
    appEngineApiDependency.setGroupId("groupId");
    appEngineApiDependency.setArtifactId("artifactId");
    appEngineApiDependency.setVersion("version");
    appEngineApiDependency.setScope("scope");

    List<Dependency> aList = new ArrayList<Dependency>();
    aList.add(appEngineApiDependency);

    List<Dependency> bList = FacetInstallDelegate.updateMavenDependecies(aList);
    Assert.assertEquals(6, bList.size());
  }

  @Test
  public void testSomething_1() {
    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubsk");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");

    List<Dependency> aList = new ArrayList<Dependency>();
    aList.add(appEngineApiStubsDependency);

    List<Dependency> bList = FacetInstallDelegate.updateMavenDependecies(aList);

    System.out.println(bList.toString());
    Assert.assertEquals(5, bList.size());
  }

  @Test
  public void testSomething_2() {
    List<Dependency> aList = new ArrayList<Dependency>();
    List<Dependency> bList = FacetInstallDelegate.updateMavenDependecies(aList);

    System.out.println(bList.toString());
    Assert.assertEquals(5, bList.size());
  }
}
