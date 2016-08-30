package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.junit.Assert;
import org.junit.Test;

public class FacetInstallDelegateTest {
  @Test
  public void testUpdateMavenProjectDependencies_nonAppEngineInitialDependency() {
    Dependency nonAppEngineDependency = new Dependency();
    nonAppEngineDependency.setGroupId("groupId");
    nonAppEngineDependency.setArtifactId("artifactId");
    nonAppEngineDependency.setVersion("version");
    nonAppEngineDependency.setScope("scope");

    List<Dependency> intialDependencies = new ArrayList<Dependency>();
    intialDependencies.add(nonAppEngineDependency);

    List<Dependency> finalDependencies = FacetInstallDelegate.updateMavenProjectDependencies(intialDependencies);
    Assert.assertEquals(6, finalDependencies.size());
  }

  @Test
  public void testUpdateMavenProjectDependencies_appEngineInitialDependency() {
    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubs");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");

    List<Dependency> intialDependencies = new ArrayList<Dependency>();
    intialDependencies.add(appEngineApiStubsDependency);

    List<Dependency> finalDependencies = FacetInstallDelegate.updateMavenProjectDependencies(intialDependencies);
    Assert.assertEquals(5, finalDependencies.size());
  }

  @Test
  public void testUpdateMavenProjectDependencies_noInitialDependency() {
    List<Dependency> intialDependencies = new ArrayList<Dependency>();
    List<Dependency> finalDependencies = FacetInstallDelegate.updateMavenProjectDependencies(intialDependencies);
    Assert.assertEquals(5, finalDependencies.size());
  }

  @Test
  public void testUpdatePomProperties_nonAppEngineInitialPropertery() {
    Properties properties = new Properties();
    properties.setProperty("a", "b");

    FacetInstallDelegate.updatePomProperties(properties, null /* monitor */);
    Assert.assertEquals(5, properties.size());
    Assert.assertTrue(properties.containsKey("a"));
  }

  @Test
  public void testUpdatePomProperties_appEngineInitialPropertery() {
    Properties properties = new Properties();
    properties.setProperty("app.version", "1");

    FacetInstallDelegate.updatePomProperties(properties, null /* monitor */);
    Assert.assertEquals(4, properties.size());
    Assert.assertTrue(properties.containsKey("app.version"));
  }
 
  @Test
  public void testUpdatePomProperties_noInitialPropertery() {
    Properties properties = new Properties();

    FacetInstallDelegate.updatePomProperties(properties, null /* monitor */);
    Assert.assertEquals(4, properties.size());
  }
}
