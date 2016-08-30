package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Dependency;

public class MavenAppEngineFacetUtil {
  /**
   * Returns a list of all the App Engine dependencies that should exist in the pom.xml
   * of a maven project that has the App Engine facet installed
   */
  public static List<Dependency> getAppEngineDependecies() {
    List<Dependency> dependencies = new ArrayList<Dependency>();

    Dependency appEngineApiDependency = new Dependency();
    appEngineApiDependency.setGroupId("com.google.appengine");
    appEngineApiDependency.setArtifactId("appengine-api-1.0-sdk");
    appEngineApiDependency.setVersion("${appengine.version}");
    appEngineApiDependency.setScope("");
    dependencies.add(appEngineApiDependency);

    Dependency servletApiDependency = new Dependency();
    servletApiDependency.setGroupId("javax.servlet");
    servletApiDependency.setArtifactId("servlet-api");
    servletApiDependency.setVersion("2.5");
    servletApiDependency.setScope("provided");
    dependencies.add(servletApiDependency);

    Dependency jstlDependecy = new Dependency();
    jstlDependecy.setGroupId("jstl");
    jstlDependecy.setArtifactId("jstl");
    jstlDependecy.setVersion("1.2");
    dependencies.add(jstlDependecy);

    Dependency appEngineTestingDependency = new Dependency();
    appEngineTestingDependency.setGroupId("com.google.appengine");
    appEngineTestingDependency.setArtifactId("appengine-testing");
    appEngineTestingDependency.setVersion("${appengine.version}");
    appEngineTestingDependency.setScope("test");
    dependencies.add(appEngineTestingDependency);

    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubs");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");
    dependencies.add(appEngineApiStubsDependency);

    return dependencies;
  }

  /**
   * Returns a map of all the App Engine properties that should exist in the pom.xml
   * of a maven project that has the App Engine facet installed
   *
   * @return a map where the keys and values are the property fields and values respectively
   */
  public static Map<String, String> getAppEnginePomProperties() {
    Map<String, String> allProperties = new HashMap<String, String>();
    allProperties.put("app.id", "");
    allProperties.put("app.version", "1");
    allProperties.put("appengine.version", AppEngineStandardFacet.DEFAULT_APPENGINE_SDK_VERSION);
    allProperties.put("gcloud.plugin.version", AppEngineStandardFacet.DEFAULT_GCLOUD_PLUGIN_VERSION);
    return allProperties;
  }

  /**
   * Returns true if the group IDs and artifact IDs of <code>dependency1</code> and
   * <@code>dependency1</@code> are equal. Returns false otherwise.
   */
  // visible for testing
  public static boolean areDependenciesEqual(Dependency dependency1, Dependency dependency2) {
    if ((dependency1 == null) || (dependency2 == null)) {
      return false;
    }

    if (dependency1.getGroupId() == null) {
      if (dependency2.getGroupId() != null) {
        return false;
      }
    } else if (!dependency1.getGroupId().equals(dependency2.getGroupId())) {
      return false;
    }

    if (dependency1.getArtifactId() == null) {
      if (dependency2.getArtifactId() != null) {
        return false;
      }
    } else if (!dependency1.getArtifactId().equals(dependency2.getArtifactId())) {
      return false;
    }

    return true;
  }

  /**
   * Returns true if a dependency with the same group ID and artifact ID as <code>targetDependency</code>
   * exists in <code>dependencies</code>. Returns false otherwise.
   */
  public static boolean doesListContainDependency(List<Dependency> dependencies, Dependency targetDependency) {
    if((dependencies == null) || (targetDependency == null)) {
      return false;
    }

    for (Dependency dependency : dependencies) {
      if (areDependenciesEqual(dependency, targetDependency)) {
        return true;
      }
    }

    return false;
  }

}
