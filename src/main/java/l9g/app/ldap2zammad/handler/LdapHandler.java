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
package l9g.app.ldap2zammad.handler;

import com.unboundid.asn1.ASN1GeneralizedTime;
import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.HashMap;
import javax.net.ssl.SSLSocketFactory;
import l9g.app.ldap2zammad.Config;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Component
@ToString
public class LdapHandler
{
  final static Logger LOGGER = LoggerFactory.getLogger(LdapHandler.class);

  @Autowired
  private Config config;

  @Autowired
  private CryptoHandler cryptoHandler;

  @Bean
  public LdapHandler ldapHandlerBean()
  {
    LOGGER.debug("getLdapHandler");
    return this;
  }

  private LDAPConnection getConnection() throws Exception
  {
    LOGGER.trace(config.toString());
    LOGGER.debug("host = " + config.getLdapHostname());
    LOGGER.debug("port = " + config.getLdapPort());
    LOGGER.debug("ssl = " + config.isLdapSslEnabled());
    LOGGER.debug("bind dn = " + config.getLdapBindDn());
    LOGGER.trace("bind pw = " + 
      cryptoHandler.decrypt(config.getLdapBindPassword()));

    LDAPConnection ldapConnection;

    LDAPConnectionOptions options = new LDAPConnectionOptions();
    if (config.isLdapSslEnabled())
    {
      ldapConnection = new LDAPConnection(createSSLSocketFactory(), options,
        config.getLdapHostname(), config.getLdapPort(),
        config.getLdapBindDn(),
        cryptoHandler.decrypt(config.getLdapBindPassword()));
    }
    else
    {
      ldapConnection = new LDAPConnection(options,
        config.getLdapHostname(), config.getLdapPort(),
        config.getLdapBindDn(),
        cryptoHandler.decrypt(config.getLdapBindPassword()));
    }
    ldapConnection.setConnectionName(config.getLdapHostname());
    return ldapConnection;
  }

  private SSLSocketFactory createSSLSocketFactory() throws
    GeneralSecurityException
  {
    SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
    return sslUtil.createSSLSocketFactory();
  }

  private void printLdapEntriesMap()
  {
    ldapEntryMap.forEach((k, v) ->
    {
      System.out.println(k + " = " + v);
    });
  }

  public void readLdapEntries(
    ASN1GeneralizedTime lastSyncTimestamp, boolean withAttributes)
    throws Throwable
  {
    ldapEntryMap.clear();

    String filter = new MessageFormat(
      config.getLdapFilter()).format(new Object[]
    {
      lastSyncTimestamp.toString()
    });

    LOGGER.debug("filter={}", filter);

    try (LDAPConnection connection = getConnection())
    {
      SearchRequest searchRequest;

      if (withAttributes)
      {
        searchRequest = new SearchRequest(
          config.getLdapBaseDn(), SearchScope.SUB, filter,
          config.getLdapUserAttributeNames());
      }
      else
      {
        searchRequest = new SearchRequest(
          config.getLdapBaseDn(), SearchScope.SUB, filter,
          config.getLdapUserId());
      }

      int totalSourceEntries = 0;
      ASN1OctetString resumeCookie = null;
      SimplePagedResultsControl responseControl = null;

      // int pagedResultSize = ldapConfig.getPagedResultSize() > 0
      //   ? ldapConfig.getPagedResultSize() : 1000;
      int pagedResultSize = 1000;

      do
      {
        searchRequest.setControls(
          new SimplePagedResultsControl(pagedResultSize, resumeCookie));

        SearchResult sourceSearchResult = connection.search(searchRequest);

        int sourceEntries = sourceSearchResult.getEntryCount();
        totalSourceEntries += sourceEntries;

        if (sourceEntries > 0)
        {
          for (Entry entry : sourceSearchResult.getSearchEntries())
          {
            ldapEntryMap.put(
              entry.getAttributeValue(
                config.getLdapUserId()).trim().toLowerCase(), entry);
          }

          responseControl = SimplePagedResultsControl.get(sourceSearchResult);

          if (responseControl != null)
          {
            resumeCookie = responseControl.getCookie();
          }
        }
      }
      while (responseControl != null && responseControl.moreResultsToReturn());

      if (totalSourceEntries == 0)
      {
        LOGGER.info("No entries to synchronize found");
      }
      else
      {
        LOGGER.
          info("build list from source DNs, {} entries", totalSourceEntries);
      }
    }
  }

  public void readAllLdapEntryUIDs() throws Throwable
  {
    readLdapEntries(new ASN1GeneralizedTime(0), false);
  }

  public void test() throws Throwable
  {
    LOGGER.debug("basedn=" + config.getLdapBaseDn());
    LOGGER.debug("scope=" + config.getLdapScope());
    LOGGER.debug("user id=" + config.getLdapUserId());

    readAllLdapEntryUIDs();
    printLdapEntriesMap();
    readLdapEntries(new ASN1GeneralizedTime(0), true);
    printLdapEntriesMap();
  }

  @Getter
  private final HashMap<String, Entry> ldapEntryMap = new HashMap<>();
}
