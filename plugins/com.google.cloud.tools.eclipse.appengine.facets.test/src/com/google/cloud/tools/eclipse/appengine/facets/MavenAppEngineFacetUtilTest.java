package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.Map;

import org.apache.maven.model.Dependency;
import org.junit.Assert;
import org.junit.Test;

public class MavenAppEngineFacetUtilTest {
  @Test
  public void testGetAppEngineDependecies() {
    Map<String, Dependency> dependecies = MavenAppEngineFacetUtil.getAppEngineDependecies();

    Assert.assertNotNull(dependecies);
    Assert.assertEquals(5, dependecies.size());
    Assert.assertTrue(dependecies.containsKey("Dependency {groupId=com.google.appengine, " 
        + "artifactId=appengine-api-1.0-sdk, version=${appengine.version}, type=jar}"));
    Assert.assertTrue(dependecies.containsKey("Dependency {groupId=com.google.appengine, "
        + "artifactId=appengine-testing, version=${appengine.version}, type=jar}"));
    Assert.assertTrue(dependecies.containsKey("Dependency {groupId=com.google.appengine, "
        + "artifactId=appengine-api-stubs, version=${appengine.version}, type=jar}"));
    Assert.assertTrue(dependecies.containsKey("Dependency {groupId=jstl, artifactId=jstl, "
        + "version=1.2, type=jar}"));
    Assert.assertTrue(dependecies.containsKey("Dependency {groupId=javax.servlet, "
        + "artifactId=servlet-api, version=2.5, type=jar}"));
  }

  @Test
  public void testGetAppEnginePomProperties() {
    Map<String, String> properties = MavenAppEngineFacetUtil.getAppEnginePomProperties();

    Assert.assertNotNull(properties);
    Assert.assertEquals(4, properties.size());
    Assert.assertTrue(properties.containsKey("app.id"));
    Assert.assertEquals("", properties.get("app.id"));
    Assert.assertTrue(properties.containsKey("app.version"));
    Assert.assertEquals(1, properties.get("app.version"));
    Assert.assertTrue(properties.containsKey("appengine.version"));
    Assert.assertEquals(AppEngineStandardFacet.DEFAULT_APPENGINE_SDK_VERSION, properties.get("appengine.version"));
    Assert.assertTrue(properties.containsKey("gcloud.plugin.version"));
    Assert.assertEquals(AppEngineStandardFacet.DEFAULT_GCLOUD_PLUGIN_VERSION, properties.get("gcloud.plugin.version"));
  }

}
