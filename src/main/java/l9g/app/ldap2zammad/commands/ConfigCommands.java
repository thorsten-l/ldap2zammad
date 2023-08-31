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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import l9g.app.ldap2zammad.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.shell.command.annotation.Command;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Command(group = "Config")
public class ConfigCommands
{
  private final static Logger LOGGER 
    = LoggerFactory.getLogger(ConfigCommands.class);

  @Autowired
  private BuildProperties buildProperties;

  @Autowired
  private Config config;

  @Command(description = "show build time properties")
  public String showBuildProperties()
  {
    LOGGER.debug("showBuildProperties");
    final StringBuilder infos = new StringBuilder();
    final ArrayList<String> keys = new ArrayList();

    buildProperties.forEach(entry -> keys.add(entry.getKey()));
    Collections.sort(keys);

    keys.forEach(key ->
    {
      infos
        .append(key)
        .append(" = ")
        .append(buildProperties.get(key))
        .append('\n');
    });

    return infos.toString();
  }

  @Command(description = "show current System.properties")
  public String showSystemProperties()
  {
    LOGGER.debug("showSystemProperties");
    String[] keys = System.getProperties().keySet()
      .toArray(String[]::new); // new String[0]
    Arrays.sort(keys);
    StringBuilder sysprops = new StringBuilder();

    for (String key : keys)
    {
      sysprops
        .append(key)
        .append(" = ")
        .append(System.getProperties().get(key).toString())
        .append('\n');
    }

    return sysprops.toString();
  }

  @Command(description = "show configuration")
  public String showConfig() throws IOException
  {
    LOGGER.debug("showConfig");
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    StringWriter output = new StringWriter();
    mapper.writeValue(output, config);
    return output.toString();
  }
}
