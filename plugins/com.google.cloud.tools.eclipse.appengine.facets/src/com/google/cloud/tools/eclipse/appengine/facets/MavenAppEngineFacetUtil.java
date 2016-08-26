package com.google.cloud.tools.eclipse.appengine.facets;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.model.Dependency;

public class MavenAppEngineFacetUtil {
  /**
   * Returns a map of all the App Engine dependencies that should exist in the pom.xml
   * of a maven project that has the App Engine facet installed
   *
   * @return a map where the key entries are the {@link Dependency#toString()} result of
   *   the Dependency values
   */
  public static Map<String, Dependency> getAppEngineDependecies() {
    // TODO: should map be immutable
    Map<String, Dependency> dependencies = new HashMap<String, Dependency>();

    Dependency appEngineApiDependency = new Dependency();
    appEngineApiDependency.setGroupId("com.google.appengine");
    appEngineApiDependency.setArtifactId("appengine-api-1.0-sdk");
    appEngineApiDependency.setVersion("${appengine.version}");
    appEngineApiDependency.setScope("");
    dependencies.put(appEngineApiDependency.toString(), appEngineApiDependency);

    Dependency servletApiDependency = new Dependency();
    servletApiDependency.setGroupId("javax.servlet");
    servletApiDependency.setArtifactId("servlet-api");
    servletApiDependency.setVersion("2.5");
    servletApiDependency.setScope("provided");
    dependencies.put(servletApiDependency.toString(), servletApiDependency);

    Dependency jstlDependecy = new Dependency();
    jstlDependecy.setGroupId("jstl");
    jstlDependecy.setArtifactId("jstl");
    jstlDependecy.setVersion("1.2");
    dependencies.put(jstlDependecy.toString(), jstlDependecy);

    Dependency appEngineTestingDependency = new Dependency();
    appEngineTestingDependency.setGroupId("com.google.appengine");
    appEngineTestingDependency.setArtifactId("appengine-testing");
    appEngineTestingDependency.setVersion("${appengine.version}");
    appEngineTestingDependency.setScope("test");
    dependencies.put(appEngineTestingDependency.toString(), appEngineTestingDependency);

    Dependency appEngineApiStubsDependency = new Dependency();
    appEngineApiStubsDependency.setGroupId("com.google.appengine");
    appEngineApiStubsDependency.setArtifactId("appengine-api-stubs");
    appEngineApiStubsDependency.setVersion("${appengine.version}");
    appEngineApiStubsDependency.setScope("test");
    dependencies.put(appEngineApiStubsDependency.toString(), appEngineApiStubsDependency);

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
    allProperties.put("appengine.version", "1.9.38");
    allProperties.put("gcloud.plugin.version", "2.0.9.111.v20160527");
    return allProperties;
  }
}
