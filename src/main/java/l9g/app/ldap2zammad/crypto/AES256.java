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
package l9g.app.ldap2zammad.crypto;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class AES256
{
  private final static Logger LOGGER = LoggerFactory.getLogger(
    AES256.class.getName());

  private final static String KEY_ALGORITHM = "AES";

  private final static String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

  private final SecretKey key;

  private final IvParameterSpec iv;

  public AES256() throws NoSuchAlgorithmException
  {
    // key
    KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
    keyGenerator.init(256);
    this.key = keyGenerator.generateKey();

    // iv
    byte[] ivBytes = new byte[16];
    new SecureRandom().nextBytes(ivBytes);
    this.iv = new IvParameterSpec(ivBytes);
  }

  public AES256(byte[] encodedSecretBytes)
  {
    byte[] encodedKeyBytes = new byte[32];
    byte[] encodedIvBytes = new byte[16];

    System.arraycopy(encodedSecretBytes, 0, encodedKeyBytes, 0, 32);
    System.arraycopy(encodedSecretBytes, 32, encodedIvBytes, 0, 16);

    this.key = new SecretKeySpec(encodedKeyBytes, 0, encodedKeyBytes.length,
      KEY_ALGORITHM);
    this.iv = new IvParameterSpec(encodedIvBytes);
  }

  public AES256(String encodedSecret)
  {
    this(Base64.getDecoder().decode(encodedSecret));
  }

  public String encrypt(String plainText)
  {
    byte[] cipherText = new byte[0];

    try
    {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, iv);
      cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    }
    catch (Exception ex)
    {
      LOGGER.error("Encryption failed ", ex);
    }

    return Base64.getEncoder().encodeToString(cipherText);
  }

  public String decrypt(String encryptedText)
  {
    byte[] plainText = new byte[0];

    try
    {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, iv);
      plainText = cipher.doFinal(Base64.getDecoder()
        .decode(encryptedText));
    }
    catch (Exception ex)
    {
      LOGGER.error("Decryption failed ", ex);
    }

    return new String(plainText);
  }

  public byte[] encrypt(byte[] plainData)
  {
    byte[] encryptedData = new byte[0];

    try
    {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, iv);
      encryptedData = cipher.doFinal(plainData);
    }
    catch (Exception ex)
    {
      LOGGER.error("Encryption failed ", ex);
    }

    return encryptedData;
  }

  public byte[] decrypt(byte[] encryptedData)
  {
    byte[] plainData = new byte[0];

    try
    {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, iv);
      plainData = cipher.doFinal(encryptedData);
    }
    catch (Exception ex)
    {
      LOGGER.error("Decryption failed ", ex);
    }

    return plainData;
  }

  public String getEncodedSecret()
  {
    byte[] secretBytes = new byte[48];
    System.arraycopy(key.getEncoded(), 0, secretBytes, 0, 32);
    System.arraycopy(iv.getIV(), 0, secretBytes, 32, 16);
    return Base64.getEncoder().encodeToString(secretBytes);
  }

  public byte[] getSecret()
  {
    byte[] secretBytes = new byte[48];
    System.arraycopy(key.getEncoded(), 0, secretBytes, 0, 32);
    System.arraycopy(iv.getIV(), 0, secretBytes, 32, 16);
    return secretBytes;
  }

}
