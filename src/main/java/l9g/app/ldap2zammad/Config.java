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

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;
import l9g.app.ldap2zammad.zammad.ZammadClient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
@Setter
@ToString
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class Config
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(Config.class);

  @Value("${zammad.base-url}")
  private String zammadBaseUrl;

  @Value("${zammad.token}")
  private String zammadToken;

  @Value("${zammad.trust-all-certificates}")
  private boolean zammadTrustAllCertificates;

  @Value("${ldap.host.name}")
  private String ldapHostname;

  @Value("${ldap.host.port}")
  private int ldapPort;

  @Value("${ldap.host.ssl}")
  private boolean ldapSslEnabled;

  @Value("${ldap.base-dn}")
  private String ldapBaseDn;

  @Value("${ldap.bind.dn}")
  private String ldapBindDn;

  @Value("${ldap.bind.password}")
  private String ldapBindPassword;

  @Value("${ldap.scope}")
  private String ldapScope;

  @Value("${ldap.filter}")
  private String ldapFilter;

  @Value("${ldap.user.id}")
  private String ldapUserId;

  @Value("${ldap.user.attributes}")
  private String[] ldapUserAttributeNames;


  @Value("${mail.enabled}")
  private boolean mailEnabled;

  @Value("${mail.host.name}")
  private String mailHostname;

  @Value("${mail.host.port}")
  private int mailPort;

  @Value("${mail.host.startTLS}")
  private boolean mailStartTLS;

  @Value("${mail.credentials.uid}")
  private String mailCredentialsUid;

  @Value("${mail.credentials.password}")
  private String mailCredentialsPassword;

  @Value("${mail.subject}")
  private String mailSubject;

  @Value("${mail.from}")
  private String mailFrom;
  
  @Value("${mail.receipients}")
  private String[] mailReceipients;

  private boolean dryRun;

  private boolean debug;

  @Bean
  public Config getConfig()
  {
    LOGGER.debug("getConfig");
    return this;
  }

  @Bean
  public ZammadClient createZammadClient() throws SSLException
  {
    LOGGER.debug("createZammadClient");
    var webClientBuilder = WebClient.builder();

    if (zammadTrustAllCertificates)
    {
      var sslContext = SslContextBuilder.forClient().trustManager(
        InsecureTrustManagerFactory.INSTANCE).build();

      HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(
        sslContext));

      webClientBuilder = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    WebClient webClient = webClientBuilder
      .baseUrl(zammadBaseUrl)
      .defaultHeader("Authorization", "Token token=" + zammadToken)
      .build();

    HttpServiceProxyFactory factory
      = HttpServiceProxyFactory.builder(
        WebClientAdapter.forClient(webClient)).build();

    return factory.createClient(ZammadClient.class);
  }
}
