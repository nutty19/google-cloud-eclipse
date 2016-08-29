package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.junit.Assert;
import org.junit.Test;

public class FacetInstallDelegateTest {
  @Test
  public void testCreateMavenProjectDependecies_initialNonAppEngineDependency() {
    Dependency nonAppEngineDependency = new Dependency();
    nonAppEngineDependency.setGroupId("groupId");
    nonAppEngineDependency.setArtifactId("artifactId");
    nonAppEngineDependency.setVersion("version");
    nonAppEngineDependency.setScope("scope");

    List<Dependency> intialDependencies = new ArrayList<Dependency>();
    intialDependencies.add(nonAppEngineDependency);

    List<Dependency> finalDependencies = FacetInstallDelegate.createMavenProjectDependecies(intialDependencies);
    Assert.assertEquals(6, finalDependencies.size());
  }

  @Test
  public void testCreateMavenProjectDependecies_initialAppEngineDependency() {
    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubs");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");

    List<Dependency> intialDependencies = new ArrayList<Dependency>();
    intialDependencies.add(appEngineApiStubsDependency);

    List<Dependency> finalDependencies = FacetInstallDelegate.createMavenProjectDependecies(intialDependencies);
    Assert.assertEquals(5, finalDependencies.size());
  }

  @Test
  public void testCreateMavenProjectDependecies_noInitialDependency() {
    List<Dependency> intialDependencies = new ArrayList<Dependency>();
    List<Dependency> finalDependencies = FacetInstallDelegate.createMavenProjectDependecies(intialDependencies);
    Assert.assertEquals(5, finalDependencies.size());
  }
}
