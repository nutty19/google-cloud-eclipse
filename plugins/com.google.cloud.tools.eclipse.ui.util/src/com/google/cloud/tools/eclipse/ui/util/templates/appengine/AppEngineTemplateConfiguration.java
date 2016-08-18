package com.google.cloud.tools.eclipse.ui.util.templates.appengine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class AppEngineTemplateConfiguration {
  private static Configuration configuration;

  public static Configuration getConfiguration() {
    if (configuration == null) {
      configuration = createConfiguration();
    }
    return configuration;
  }

  private AppEngineTemplateConfiguration() {

  }

  private static Configuration createConfiguration(){
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
    Bundle bundle = FrameworkUtil.getBundle(AppEngineTemplateConfiguration.class);
    URL templatesUrl = bundle.getEntry("/templates/appengine");

    try {
      File appengineTemplatesDir = new File(FileLocator.resolve(templatesUrl).toURI());
      cfg.setDirectoryForTemplateLoading(appengineTemplatesDir);
      cfg.setDefaultEncoding("UTF-8");
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      cfg.setLogTemplateExceptions(false);
      return cfg;
    } catch (IOException | URISyntaxException e) {
      // TODO: Log error
    }
    // TODO: is it better to return the cfg without the template dir?
    return null; 
  }
  
}
