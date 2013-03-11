package fi.mikah.log.web;
/*
Copyright (c) 2013 Mika Hämäläinen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fi.mikah.log.web.json.AppenderJson;
import fi.mikah.log.web.json.LoggerJson;
import fi.mikah.log.web.json.ResponseJson;

@Controller
public class LogController {
  private static final Logger log = Logger.getLogger(LogController.class);
  private static final String LOG4J_CONFIG = "/log4j.properties";

  @RequestMapping(value = "/appenders", method = RequestMethod.GET)
  public @ResponseBody List<AppenderJson> appenders() {
    Enumeration<?> allAppenders = Logger.getRootLogger().getAllAppenders();
    List<AppenderJson> appenders = new ArrayList<AppenderJson>();
    while(allAppenders.hasMoreElements()) {
      final Appender appender = (Appender) allAppenders.nextElement();
      AppenderJson json = new AppenderJson();
      json.setName(appender.getName());
      if (appender instanceof AppenderSkeleton) {
        Priority threshold = ((AppenderSkeleton) appender).getThreshold();
        if (threshold != null) {
          json.setThreshold(threshold.toString());
        } else {
          json.setThreshold("-");
        }
      }
      appenders.add(json);
    }

    log.debug("Found " + appenders.size() + " appenders");

    return appenders;
  }

  @RequestMapping(value = "/loggers", method = RequestMethod.GET)
  public @ResponseBody List<LoggerJson> loggers() {
    Enumeration<?> currentLoggers = LogManager.getCurrentLoggers();
    List<LoggerJson> loggers = new ArrayList<LoggerJson>();

    while(currentLoggers.hasMoreElements()) {
      Category category = (Category) currentLoggers.nextElement();
      LoggerJson json = new LoggerJson();
      json.setName(category.getName());
      json.setLevel(category.getEffectiveLevel().toString());
      loggers.add(json);
    }

    Collections.sort(loggers, new Comparator<LoggerJson>() {
      @Override
      public int compare(LoggerJson o1, LoggerJson o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });


    LoggerJson rootJson = new LoggerJson();
    rootJson.setName(Logger.getRootLogger().getName());
    rootJson.setLevel(Logger.getRootLogger().getEffectiveLevel().toString());
    loggers.add(0, rootJson);

    log.debug("Found " + loggers.size() + " loggers");

    return loggers;
  }

  @RequestMapping(value = "/loggers", method = RequestMethod.POST)
  public @ResponseBody ResponseJson setLogLevel(@RequestBody LoggerJson request) {
    log.info("Setting logger '" + request.getName() + "' to level " + request.getLevel());

    final ResponseJson response = new ResponseJson();
    final Level level = Level.toLevel(request.getLevel());

    if (request.getName().equals(Logger.getRootLogger().getName())) {
      Logger.getRootLogger().setLevel(level);
      response.setStatus("root");
    } else {
      Logger logger = Logger.getLogger(request.getName());
      logger.setLevel(level);
      response.setStatus("ok");
    }

    return response;
  }

  @RequestMapping(value = "reload", method = RequestMethod.GET)
  public @ResponseBody ResponseJson reloadConfiguration() {
    log.info("Reloading log4j configuration");
    InputStream log4jConfig = getClass().getResourceAsStream(LOG4J_CONFIG);
    try {
      Properties log4jProperties = new Properties();
      log4jProperties.load(log4jConfig);
      log4jConfig.close();
      new PropertyConfigurator().doConfigure(log4jProperties, LogManager.getLoggerRepository());
    } catch (IOException e) {
      log.error("Failed to load log4j.properties", e);
      throw new IllegalStateException("Failed to load log4j.properties", e);
    }

    final ResponseJson response = new ResponseJson();
    response.setStatus("ok");
    return response;
  }
}
