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
    Map<String, Dependency> dependecies = MavenAppEngineFacetUtil.getAppEngineDependecies();
    Assert.assertEquals(5, dependecies.size());
    Assert.assertTrue(dependecies.containsKey(""));
    Assert.assertTrue(dependecies.containsKey(""));
    Assert.assertTrue(dependecies.containsKey(""));
    Assert.assertTrue(dependecies.containsKey(""));
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

    List<Dependency> bList = FacetInstallDelegate.createMavenProjectDependecies(aList);
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

    List<Dependency> bList = FacetInstallDelegate.createMavenProjectDependecies(aList);

    System.out.println(bList.toString());
    Assert.assertEquals(5, bList.size());
  }

  @Test
  public void testSomething_2() {
    List<Dependency> aList = new ArrayList<Dependency>();
    List<Dependency> bList = FacetInstallDelegate.createMavenProjectDependecies(aList);

    System.out.println(bList.toString());
    Assert.assertEquals(5, bList.size());
  }
}
