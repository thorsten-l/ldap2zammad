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

import ch.qos.logback.classic.Level;
import com.unboundid.asn1.ASN1GeneralizedTime;
import com.unboundid.ldap.sdk.Entry;
import java.util.ArrayList;
import java.util.List;
import l9g.app.ldap2zammad.Config;
import l9g.app.ldap2zammad.LogbackConfig;
import l9g.app.ldap2zammad.TimestampUtil;
import l9g.app.ldap2zammad.engine.JavaScriptEngine;
import l9g.app.ldap2zammad.handler.LdapHandler;
import l9g.app.ldap2zammad.model.ZammadUser;
import l9g.app.ldap2zammad.handler.ZammadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Command(group = "Application")
public class ApplicationCommands
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(ApplicationCommands.class);

  @Autowired
  private Config config;

  @Autowired
  private LdapHandler ldapHandler;

  @Autowired
  private ZammadHandler zammadHandler;

  @Autowired
  private LogbackConfig logbackConfig;

  @Command(description = "sync users from LDAP to Zammad")
  public void sync(
    @Option(longNames = "full-sync", defaultValue = "false") boolean fullSync,
    @Option(longNames = "dry-run", defaultValue = "false") boolean dryRun,
    @Option(longNames = "debug", defaultValue = "false") boolean debug,
    @Option(longNames = "trace", defaultValue = "false") boolean trace
  ) throws Throwable
  {
    logbackConfig.getRootLogger().setLevel(Level.INFO);
    logbackConfig.getL9gLogger().setLevel(Level.INFO);

    if (debug)
    {
      logbackConfig.getL9gLogger().setLevel(Level.DEBUG);
    }

    if (trace)
    {
      debug = true;
      logbackConfig.getRootLogger().setLevel(Level.TRACE);
      logbackConfig.getL9gLogger().setLevel(Level.TRACE);
    }

    LOGGER.info("dryRun = '{}', debug = '{}', trace = '{}'",
      dryRun, debug, trace);
    config.setDebug(debug);
    config.setDryRun(dryRun);

    LOGGER.debug("Los gehts!");
    TimestampUtil timestampUtil = new TimestampUtil("zammad-users");

    zammadHandler.readZammadRolesAndUsers();

    Integer adminGroupId = zammadHandler.getAdminGroupId();
    LOGGER.debug("adminGroupId=" + adminGroupId);

    ///////////////////////////////////////////////////////////////////////////
    // DELETE
    ldapHandler.readAllLdapEntryUIDs();
    for (ZammadUser user : zammadHandler.getZammadUsersList())
    {
      if (!ldapHandler.getLdapEntryMap().containsKey(user.getLogin()))
      {
        List<Integer> roleIds = user.getRole_ids();

        if (user.getId() == 1 || roleIds.contains(adminGroupId))
        {
          // IGNORE Admin Users
          LOGGER.warn("IGNORE DELETE ADMIN: {}, {} {} ({})",
            user.getLogin(), user.getFirstname(),
            user.getLastname(), user.getEmail());
        }
        else
        {
          // DELETE
          zammadHandler.deleteUser(user);
        }
      }
    }

    ///////////////////////////////////////////////////////////////////////////
    ASN1GeneralizedTime timestamp;

    if (fullSync)
    {
      timestamp = new ASN1GeneralizedTime(0l); // 01.01.1970, unix time 0
    }
    else
    {
      timestamp = timestampUtil.getLastSyncTimestamp();
    }

    ldapHandler.readLdapEntries(timestamp, true);

    try (JavaScriptEngine js = new JavaScriptEngine())
    {
      int noEntries = ldapHandler.getLdapEntryMap().size();
      int entryCounter = 0;

      for (Entry entry : ldapHandler.getLdapEntryMap().values())
      {
        entryCounter++;
        LOGGER.debug("{}/{}", entryCounter, noEntries);
        String login = entry.getAttributeValue(config.getLdapUserId());
        ZammadUser zammadUser = zammadHandler.getZammadUsersMap().get(login);
        ZammadUser updateUser = new ZammadUser();
        updateUser.setLogin(login);

        ArrayList<String> roles = new ArrayList<>();
        if (config.getSyncDefaultRole() != null
          && config.getSyncDefaultRole().length() > 0)
        {
          roles.add(config.getSyncDefaultRole());
        }

        if (zammadUser != null)
        {
          updateUser.setId(zammadUser.getId());
          List<Integer> roleIds
            = zammadHandler.getZammadUsersMap().get(login).getRole_ids();

          if (roleIds.contains(adminGroupId))
          {
            // IGNORE Admin Users
            LOGGER.warn("IGNORE UPDATE ADMIN: {}, {} {} ({})",
              zammadUser.getLogin(),
              zammadUser.getFirstname(), zammadUser.getLastname(),
              zammadUser.getEmail());
          }
          else
          {
            // UPDATE

            if (config.isSyncTagSyncerRolesEnabled()
              && config.isSyncRemoveTaggedRolesBeforUpdateUser())
            {
              zammadUser.getRole_ids().forEach(roleId ->
              {
                String roleName
                  = zammadHandler.getZammadRoleMap().get(roleId).getName();
                if (!roleName.equalsIgnoreCase("Agent")
                  && !roleName.equalsIgnoreCase(config.getSyncDefaultRole())
                  && !roleName.startsWith(config.getSyncRolesTag()))
                {
                  roles.add(roleName);
                }
              });
            }

            js.getValue().executeVoid("update", updateUser, entry, roles);
            zammadHandler.updateUser(updateUser);
          }
        }
        else
        {
          // CREATE
          js.getValue().executeVoid("create", updateUser, entry, roles);
          zammadHandler.createUser(updateUser);
        }
      }
    }
    ///////////////////////////////////////////////////////////////////////////
    if (!dryRun)
    {
      timestampUtil.writeCurrentTimestamp();
    }

    logbackConfig.getRootLogger().setLevel(Level.INFO);
    logbackConfig.getL9gLogger().setLevel(Level.INFO);
  }
}
