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

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
@CommandScan
public class Application
{
  private final static String CONFIG_PATH = "./";

  static
  {
    System.setProperty("spring.config.location", "file:" + CONFIG_PATH);
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
  }

  @Bean
  public PromptProvider createPromptProvider()
  {
    return () -> new AttributedString("ldap2zammad:>",
      AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
  }

  public static void main(String[] args)
  {
    SpringApplication.run(Application.class, args);
  }
}
