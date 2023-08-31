/*
 * Copyright 2022 Thorsten Ludewig (t.ludewig@gmail.com).
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

import java.util.Random;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class PasswordGenerator
{
  private final static char[] PWCHARS =
  {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.', '!', '#', '%',
    '/', '?', '+', '*', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
    'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
    'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z', '$', '&', '<', '>'
  };

  private final static PasswordGenerator SINGLETON = new PasswordGenerator();
  private final Random random;
  
  private PasswordGenerator()
  {
    random = new Random(System.currentTimeMillis());
  }

  public static String generate(int length)
  {
    char[] pwd = new char[length];
    
    for( int i=0; i<length; i++ )
    {
      pwd[i] = PWCHARS[SINGLETON.random.nextInt(PWCHARS.length)];
    }
    
    return String.valueOf(pwd);
  }
}

