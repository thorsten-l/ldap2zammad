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
package l9g.app.ldap2zammad.commands;

import l9g.app.ldap2zammad.crypto.PasswordGenerator;
import l9g.app.ldap2zammad.handler.CryptoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Command(group = "Crypto")
public class CryptoCommands
{
  private final static Logger LOGGER 
    = LoggerFactory.getLogger(CryptoCommands.class);

  @Autowired
  private CryptoHandler cryptoHandler;

  @Command(description = "encrypt clear text for passwords")
  public void encrypt(
    @Option(description = "clear text", required = true) String text)
    throws Throwable
  {
    LOGGER.debug("encrypt");
    System.out.println("text = \"" + text + "\"");
    System.out.println("encrypted text = \"" + cryptoHandler.encrypt(text) + "\"");
  }

  @Command(description = "decrypt encrypted text")
  public void decrypt(
    @Option(description = "encrypted text", required = true) String encrypted)
    throws Throwable
  {
    LOGGER.debug("decrypt");
    System.out.println("encrypted text = \"" + encrypted + "\"");
    System.out.println("text = \""
      + cryptoHandler.decrypt(encrypted) + "\"");
  }

  
  @Command(alias="pwgen", description = "create random passwords")
  public void passwordGenerator(
    @Option(description = "number of chars", required = true) int length)
    throws Throwable
  {
    LOGGER.debug("passwordGenerator");
    System.out.println( "random password");
    encrypt(PasswordGenerator.generate(length));
  }
}
