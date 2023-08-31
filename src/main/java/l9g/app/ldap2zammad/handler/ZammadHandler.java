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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l9g.app.ldap2zammad.Config;
import l9g.app.ldap2zammad.model.ZammadAnonymousUser;
import l9g.app.ldap2zammad.model.ZammadRole;
import l9g.app.ldap2zammad.model.ZammadUser;
import l9g.app.ldap2zammad.zammad.ZammadClient;
import lombok.Getter;
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
public class ZammadHandler
{
  private final static Logger LOGGER 
    = LoggerFactory.getLogger(ZammadHandler.class);

  @Autowired
  private Config config;

  @Autowired
  private ZammadClient zammadClient;

  @Bean
  public ZammadHandler zammadHandlerBean()
  {
    LOGGER.debug("getZammadHandler");
    return this;
  }

  public int getAdminGroupId()
  {
    LOGGER.debug("getAdminGroupId");
    int adminGroupId = -1;

    List<ZammadRole> roles = zammadClient.roles();

    for (ZammadRole role : roles)
    {
      LOGGER.debug(role.toString());
      if ("Admin".equals(role.getName()))
      {
        adminGroupId = role.getId();
        break;
      }
    }

    return adminGroupId;
  }

  public void readZammadUsers()
  {
    LOGGER.debug("readZammadUsers");
    zammadUsersList = zammadClient.usersSearch("active", "true");
    zammadUsersMap.clear();
    zammadUsersList.forEach(user -> zammadUsersMap.put(user.getLogin(), user));
  }

  public ZammadUser createUser(ZammadUser user)
  {
    if (config.isDryRun())
    {
      LOGGER.debug("CREATE DRY RUN: " + user);
    }
    else
    {
      LOGGER.debug("CREATE: " + user);
      try
      {
        user = zammadClient.usersCreate(user);
      }
      catch (Throwable t)
      {
        LOGGER.error("*** CREATE FAILED *** " + t.getMessage());
      }
    }

    return user;
  }

  public ZammadUser updateUser(ZammadUser user)
  {
    if (config.isDryRun())
    {
      LOGGER.debug("UPDATE DRY RUN: " + user);
    }
    else
    {
      try
      {
        LOGGER.debug("UPDATE: " + objectMapper.writeValueAsString(user));
        user = zammadClient.usersUpdate(user.getId(), user);
      }
      catch (Throwable t)
      {
        LOGGER.error("*** UPDATE FAILED *** " + t.getMessage());
      }
    }

    return user;
  }

  public void deleteUser(ZammadUser user)
  {
    if (config.isDryRun())
    {
      LOGGER.debug("DELETE DRY RUN: " + user);
    }
    else
    {
      LOGGER.debug("DELETE: " + user);
      try
      {
        // zammadClient.usersDelete(user.getId());
        zammadClient.usersAnonymize(user.getId(),
          new ZammadAnonymousUser(user.getLogin()));
      }
      catch (Throwable t)
      {
        LOGGER.error("*** DELETE (Anonymize) FAILED *** " + t.getMessage());
      }
    }
  }

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Getter
  private final Map<String, ZammadUser> zammadUsersMap = new HashMap<>();

  @Getter
  private List<ZammadUser> zammadUsersList;
}
