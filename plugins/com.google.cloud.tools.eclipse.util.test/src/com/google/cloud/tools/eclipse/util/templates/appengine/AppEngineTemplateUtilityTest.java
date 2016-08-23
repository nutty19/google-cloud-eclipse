package com.google.cloud.tools.eclipse.util.templates.appengine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class AppEngineTemplateUtilityTest {

  private SubMonitor monitor = SubMonitor.convert(new NullProgressMonitor());
  private IProject project;
  private IFile testFile;

  @Before
  public void setUp() throws CoreException {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    project = workspace.getRoot().getProject("foo");
    if (!project.exists()) {
      project.create(monitor);
      project.open(monitor);
    }
    testFile = project.getFile("bar");
    if (!testFile.exists()) {
      testFile.create(new ByteArrayInputStream(new byte[0]), true, monitor);
    }
  }

  @After
  public void cleanUp() throws CoreException {
    testFile.delete(true, monitor);
    project.delete(true, monitor);
  }

  @Test
  public void testCreateFileContent_appengineWebXmlWithProjectId()
      throws CoreException, IOException {
    String fileLocation = testFile.getLocation().toString();
    Map<String, String> dataMap = new HashMap<String, String>();
    dataMap.put("projectId", "myProjectId");    
    AppEngineTemplateUtility.createFileContent(
        fileLocation, AppEngineTemplateUtility.APPENGINE_WEB_XML_TEMPLATE, dataMap);

    InputStream testFileStream = testFile.getContents(true);
    InputStream expectedFileStream = getDataFile("appengineWebXmlWithProjectId.txt");
    compareFileContent(expectedFileStream, testFileStream);
  }

  @Test
  public void testCreateFileContent_appengineWebXmlWithoutProjectId()
      throws CoreException, IOException {
    String fileLocation = testFile.getLocation().toString();  
    AppEngineTemplateUtility.createFileContent(
        fileLocation, AppEngineTemplateUtility.APPENGINE_WEB_XML_TEMPLATE, new HashMap<String, String>());

    InputStream testFileStream = testFile.getContents(true);
    InputStream expectedFileStream = getDataFile("appengineWebXmlWithoutProjectId.txt");
    compareFileContent(expectedFileStream, testFileStream);
  }

  @Test
  public void testCreateFileContent_helloAppEngine()
      throws CoreException, IOException {
    String fileLocation = testFile.getLocation().toString();
    Map<String, String> dataMap = new HashMap<String, String>();
    dataMap.put("package", "com.example");
    AppEngineTemplateUtility.createFileContent(
        fileLocation, AppEngineTemplateUtility.HELLO_APPENGINE_TEMPLATE, dataMap);

    InputStream testFileStream = testFile.getContents(true);
    InputStream expectedFileStream = getDataFile("helloAppEngine.txt");
    compareFileContent(expectedFileStream, testFileStream);
  }

  @Test
  public void testCreateFileContent_index()
      throws CoreException, IOException {
    String fileLocation = testFile.getLocation().toString();  
    AppEngineTemplateUtility.createFileContent(
        fileLocation, AppEngineTemplateUtility.INDEX_HTML_TEMPLATE, new HashMap<String, String>());

    InputStream testFileStream = testFile.getContents(true);
    InputStream expectedFileStream = getDataFile("index.txt");
    compareFileContent(expectedFileStream, testFileStream);
  }

  @Test
  public void testCreateFileContent_web() throws CoreException, IOException {
    String fileLocation = testFile.getLocation().toString();
    Map<String, String> dataMap = new HashMap<String, String>();
    dataMap.put("package", "com.example.");
    AppEngineTemplateUtility.createFileContent(
        fileLocation, AppEngineTemplateUtility.WEB_XML_TEMPLATE, dataMap);

    InputStream testFileStream = testFile.getContents(true);
    InputStream expectedFileStream = getDataFile("web.txt");
    compareFileContent(expectedFileStream, testFileStream);
  }

  private InputStream getDataFile(String fileName) throws IOException {
    Bundle bundle = FrameworkUtil.getBundle(AppEngineTemplateUtilityTest.class);
    URL expectedFileUrl = bundle.getResource("/testData/templates/appengine/" + fileName);  
    return expectedFileUrl.openStream();
  }

  private void compareFileContent(InputStream expectedFileStream, InputStream actualFileStream) {
    Scanner expectedScanner = new Scanner(expectedFileStream);
    String expectedContent = expectedScanner.useDelimiter("\\Z").next();
    expectedScanner.close();

    Scanner actualScanner = new Scanner(actualFileStream);
    String actualContent = actualScanner.useDelimiter("\\Z").next();
    actualScanner.close();

    Assert.assertEquals(expectedContent, actualContent);
  }

}
