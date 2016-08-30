package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.junit.Assert;
import org.junit.Test;

public class MavenAppEngineFacetUtilTest {
  @Test
  public void testGetAppEngineDependecies() {
    List<Dependency> dependecies = MavenAppEngineFacetUtil.getAppEngineDependecies();

    Assert.assertNotNull(dependecies);
    Assert.assertEquals(5, dependecies.size());
  }

  @Test
  public void testGetAppEnginePomProperties() {
    Map<String, String> properties = MavenAppEngineFacetUtil.getAppEnginePomProperties(null /* monitor */);

    Assert.assertNotNull(properties);
    Assert.assertEquals(4, properties.size());
    Assert.assertTrue(properties.containsKey("app.id"));
    Assert.assertEquals("", properties.get("app.id"));
    Assert.assertTrue(properties.containsKey("app.version"));
    Assert.assertEquals("1", properties.get("app.version"));
    Assert.assertTrue(properties.containsKey("appengine.version"));
    Assert.assertTrue(properties.containsKey("gcloud.plugin.version"));
  }

  @Test
  public void testAreDependenciesEqual_nullDependecies() {
    Assert.assertFalse(MavenAppEngineFacetUtil.areDependenciesEqual(null, null));
    Assert.assertFalse(MavenAppEngineFacetUtil.areDependenciesEqual(new Dependency(), null));
    Assert.assertFalse(MavenAppEngineFacetUtil.areDependenciesEqual(null, new Dependency()));
  }

  @Test
  public void testAreDependenciesEqual_equalDependencies() {
    Dependency dependency1 = new Dependency();
    Dependency dependency2 = new Dependency();
    Dependency dependency3 = new Dependency();
    dependency3.setGroupId("groupId");
    dependency3.setArtifactId("artifactId");
    Dependency dependency4 = new Dependency();
    dependency4.setGroupId("groupId");
    dependency4.setArtifactId("artifactId");

    Assert.assertTrue(MavenAppEngineFacetUtil.areDependenciesEqual(dependency1, dependency2));
    Assert.assertTrue(MavenAppEngineFacetUtil.areDependenciesEqual(dependency3, dependency4));
  }

  @Test
  public void testAreDependenciesEqual_unEqualDependecies() {
    Dependency dependency1 = new Dependency();
    Dependency dependency2 = new Dependency();
    dependency2.setGroupId("groupId1");
    dependency2.setArtifactId("artifactId1");
    Dependency dependency3 = new Dependency();
    dependency3.setGroupId("groupId2");
    dependency3.setArtifactId("artifactId2");

    Assert.assertFalse(MavenAppEngineFacetUtil.areDependenciesEqual(dependency1, dependency2));
    Assert.assertFalse(MavenAppEngineFacetUtil.areDependenciesEqual(dependency2, dependency3));
  }

  @Test
  public void testDoesListContainDependency_existingDependency() {
    Dependency dependency1 = new Dependency();
    dependency1.setGroupId("groupId");
    dependency1.setArtifactId("artifactId");
    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(dependency1);

    Dependency dependency2 = new Dependency();
    dependency2.setGroupId("groupId");
    dependency2.setArtifactId("artifactId");

    Assert.assertTrue(MavenAppEngineFacetUtil.doesListContainDependency(dependencies, dependency2));
  }

  @Test
  public void testDoesListContainDependency_nonExistingDependency() {
    List<Dependency> dependencies1 = new ArrayList<Dependency>();
    List<Dependency> dependencies2 = new ArrayList<Dependency>();
    Dependency dependency1 = new Dependency();
    Dependency dependency2 = new Dependency();
    dependency2.setGroupId("groupId2");
    dependency2.setArtifactId("artifactId2");
    Dependency dependency3 = new Dependency();
    dependency3.setGroupId("groupId3");
    dependency3.setArtifactId("artifactId3");
    dependencies2.add(dependency2);

    Assert.assertFalse(MavenAppEngineFacetUtil.doesListContainDependency(null, null));
    Assert.assertFalse(MavenAppEngineFacetUtil.doesListContainDependency(dependencies1, null));
    Assert.assertFalse(MavenAppEngineFacetUtil.doesListContainDependency(dependencies1, dependency1));
    Assert.assertFalse(MavenAppEngineFacetUtil.doesListContainDependency(dependencies1, dependency2));
    Assert.assertFalse(MavenAppEngineFacetUtil.doesListContainDependency(dependencies2, dependency3));
  }

}
