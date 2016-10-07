package com.google.cloud.tools.eclipse.appengine.libraries.repository;

import org.junit.Test;

import com.google.cloud.tools.eclipse.appengine.libraries.MavenCoordinates;

public class M2RepositoryServiceTest {

  @Test
  public void testGetJarLocation() {
    new M2RepositoryService().getJarLocation(new MavenCoordinates("com.google.appengine", "appengine-api-1.0-sdk"));
  }

}
