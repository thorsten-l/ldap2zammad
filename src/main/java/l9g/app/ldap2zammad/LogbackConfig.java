/*
 * Copyright 2023 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.app.ldap2zammad;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.core.spi.CyclicBufferTracker;
import l9g.app.ldap2zammad.handler.CryptoHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Component
public class LogbackConfig
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(LogbackConfig.class);

  public final static String SMTP_NOTIFICATION = "SMTP_NOTIFICATION";

  private final static String PATTERN
    = "%date{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger:%line - %msg %n";

  @Autowired
  public LogbackConfig(Config config, CryptoHandler cryptoHandler)
  {
    this.config = config;
    this.cryptoHandler = cryptoHandler;

    LOGGER.debug("initialize - post construct - mail is enabled = {}",
      config.isMailEnabled());
    loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    rootLogger = loggerContext.getLogger(
      ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    l9gLogger = loggerContext.getLogger("l9g");

    notificationMarker = MarkerFactory.getMarker(SMTP_NOTIFICATION);

    if (config.isMailEnabled())
    {
      PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
      layoutEncoder.setContext(loggerContext);
      layoutEncoder.setPattern(PATTERN);
      layoutEncoder.start();
      //
      smtpAppender = buildSmtpAppender("SMTP", layoutEncoder, null);
      smtpMarkerAppender
        = buildSmtpAppender(SMTP_NOTIFICATION, layoutEncoder,
          SMTP_NOTIFICATION);
      rootLogger.addAppender(smtpAppender);
      rootLogger.addAppender(smtpMarkerAppender);
    }
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public LogbackConfig logbackConfigBean()
  {
    LOGGER.debug("logbackConfigBean");
    return this;
  }

  private SMTPAppender buildSmtpAppender(String name,
    PatternLayoutEncoder layoutEncoder, String markerName)
  {
    SMTPAppender appender = new SMTPAppender();

    CyclicBufferTracker bufferTracker = new CyclicBufferTracker();
    bufferTracker.setBufferSize(2);

    appender.setContext(loggerContext);
    appender.setName(name);
    appender.setFrom(config.getMailFrom());

    for (String to : config.getMailReceipients())
    {
      appender.addTo(to);
    }

    appender.setSmtpHost(config.getMailHostname());
    appender.setSmtpPort(config.getMailPort());
    appender.setSTARTTLS(config.isMailStartTLS());
    appender.setSubject(config.getMailSubject());
    appender.setUsername(config.getMailCredentialsUid());
    appender.setPassword(
      cryptoHandler.decrypt(config.getMailCredentialsPassword()));
    appender.setLayout(layoutEncoder.getLayout());
    appender.setAsynchronousSending(false);
    appender.setCyclicBufferTracker(bufferTracker);

    if (markerName != null)
    {
      OnMarkerEvaluator evaluator = new OnMarkerEvaluator();
      evaluator.addMarker(markerName);
      appender.setEvaluator(evaluator);
    }

    appender.start();

    return appender;
  }

  private SMTPAppender smtpAppender;

  private SMTPAppender smtpMarkerAppender;

  private final Config config;

  private final CryptoHandler cryptoHandler;

  private final LoggerContext loggerContext;

  @Getter
  private final ch.qos.logback.classic.Logger rootLogger;

  @Getter
  private final ch.qos.logback.classic.Logger l9gLogger;

  @Getter
  private final Marker notificationMarker;
}
