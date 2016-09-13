package com.google.cloud.tools.eclipse.appengine.facets;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import java.util.ArrayList;
import java.util.List;

public final class AppEngineSdkClasspathContainer implements IClasspathContainer {

  public static final String CONTAINER_ID = "AppEngineSDK";

  private static final String[] INCLUDED_JARS = {};
  private static final String APPENGINE_API_JAVADOC_URL =
      "https://cloud.google.com/appengine/docs/java/javadoc/";

  @Override
  public IPath getPath() {
    return new Path(AppEngineSdkClasspathContainer.CONTAINER_ID);
  }

  @Override
  public int getKind() {
    return IClasspathContainer.K_DEFAULT_SYSTEM;
  }

  @Override
  public String getDescription() {
    return "App Engine SDKs";
  }

  @Override
  public IClasspathEntry[] getClasspathEntries() {
    try {
      CloudSdk cloudSdk = new CloudSdk.Builder().build();
      if (cloudSdk != null) {
        List<IClasspathEntry> entries = new ArrayList<>(INCLUDED_JARS.length);
        for (String jarLocation : INCLUDED_JARS) {
          java.nio.file.Path jarFile = cloudSdk.getJavaAppEngineSdkPath().resolve(jarLocation);
          if (jarFile != null) {
            IClasspathAttribute javadocAttribute = JavaCore.newClasspathAttribute(
                IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, APPENGINE_API_JAVADOC_URL);
            IClasspathEntry jarEntry = JavaCore.newLibraryEntry(
                new Path(jarFile.toString()),
                null /* sourceAttachmentPath */,
                null /* sourceAttachmentRootPath */,
                null /* accessRules */,
                new IClasspathAttribute[] { javadocAttribute },
                false /* isExported */);
            entries.add(jarEntry);
          }
        }
        return entries.toArray(new IClasspathEntry[entries.size()]);
      }
    } catch (AppEngineException ex) {
      /* fall through */
    }
    return new IClasspathEntry[0];
  }

}
