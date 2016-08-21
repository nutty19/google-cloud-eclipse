package com.google.cloud.tools.eclipse.util.templates.appengine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.cloud.tools.eclipse.util.status.StatusUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class AppEngineTemplateUtility {
  public static final String APPENGINE_WEB_XML_TEMPLATE = "appengine-web.xml.ftl";
  private static Configuration configuration;

  public static void createFileContent(String outputFileLocation, String templateName, Map<String, Object> dataMap)
      throws CoreException {
    try {
      if (configuration == null) {
        configuration = createConfiguration();
      }
      File outputFile = new File(outputFileLocation);
      Writer fileWriter = new FileWriter(outputFile);
      Template template = configuration.getTemplate(templateName);
      template.process(dataMap, fileWriter);
    } catch (IOException | TemplateException | URISyntaxException e) {
      throw new CoreException(StatusUtil.error(AppEngineTemplateUtility.class, e.getMessage()));
    }
  }

  private AppEngineTemplateUtility() {
  }

  private static Configuration createConfiguration() throws IOException, URISyntaxException{
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
    Bundle bundle = FrameworkUtil.getBundle(AppEngineTemplateUtility.class);
    URL templatesUrl = bundle.getEntry("/templates/appengine");
    File appengineTemplatesDir = new File(FileLocator.resolve(templatesUrl).toURI());
    cfg.setDirectoryForTemplateLoading(appengineTemplatesDir);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    return cfg;
  }

}
