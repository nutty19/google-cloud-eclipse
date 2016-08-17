package com.google.cloud.tools.eclipse.ui.util.templates.appengine;

import java.io.File;
import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class AppEngineTemplateConfiguration {
  private static Configuration configuration;

  private AppEngineTemplateConfiguration() {

  }

  private static Configuration createConfiguration(){
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);

    // TODO replace this string
    String tmp = "/usr/local/google/home/nbashirbello/src/gcloud-eclipse-tools/plugins/com.google.cloud.tools.eclipse.ui.util/templates/appengine";

    // Check that file exists
    File someFile = new File(tmp);
    try {
      cfg.setDirectoryForTemplateLoading(someFile);
    } catch (IOException e) {
      // Log and do something
      return null;
    }
    
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    return cfg;
  }
  
  public static Configuration getConfiguration() {
    if (configuration == null) {
      configuration = createConfiguration();
    }
    return configuration;
  }
  
}
