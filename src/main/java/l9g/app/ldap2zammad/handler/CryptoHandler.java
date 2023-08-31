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
import jakarta.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.HashMap;
import javax.net.ssl.SSLSocketFactory;
import l9g.app.ldap2zammad.Config;
import l9g.app.ldap2zammad.crypto.AES256;
import l9g.app.ldap2zammad.crypto.AppSecretKey;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@ToString
public class CryptoHandler
{
  final static Logger LOGGER = LoggerFactory.getLogger(CryptoHandler.class);

  public final static String AES256_PREFIX = "{AES256}";
  
  @Autowired
  private AppSecretKey appSecretKey;

  private AES256 aes256;

  private boolean initialized;

  @PostConstruct
  public synchronized void initialize()
  {
    if (!initialized)
    {
      LOGGER.debug("initialize");
      aes256 = new AES256(appSecretKey.getSecretKey());
      initialized = true;
    }
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public CryptoHandler cryptoHandlerBean()
  {
    LOGGER.debug("cryptoHandlerBean");
    return this;
  }

  public String encrypt(String text)
  {
    return AES256_PREFIX + aes256.encrypt(text);
  }
  
  public String decrypt(String encryptedText)
  {
    String text;
    
    if ( encryptedText != null && encryptedText.startsWith(AES256_PREFIX))
    {
      text = aes256.decrypt(encryptedText.substring(AES256_PREFIX.length()));
    }
    else
    {
      text = encryptedText;
    }
    
    return text;
  }
}
