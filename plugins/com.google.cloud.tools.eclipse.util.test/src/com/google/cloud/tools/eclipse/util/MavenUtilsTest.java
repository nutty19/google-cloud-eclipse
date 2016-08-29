package com.google.cloud.tools.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class MavenUtilsTest {
  @Test
  public void testMavenNature_mavenProject() throws CoreException {
    IProject project = Mockito.mock(IProject.class);
    Mockito.when(project.hasNature("org.eclipse.m2e.core.maven2Nature")).thenReturn(true);
    Mockito.when(project.isAccessible()).thenReturn(true);

    Assert.assertTrue(MavenUtils.hasMavenNature(project));
  }

  @Test
  public void testMavenNature_nonMavenProject() throws CoreException {
    IProject project = Mockito.mock(IProject.class);
    Mockito.when(project.isAccessible()).thenReturn(true);

    Assert.assertFalse(MavenUtils.hasMavenNature(project));
  }
}
