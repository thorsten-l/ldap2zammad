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

import com.unboundid.asn1.ASN1GeneralizedTime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class TimestampUtil
{
  private final static Logger LOGGER
    = LoggerFactory.getLogger(TimestampUtil.class.getName());

  private static final String TIMESTAMP_FILENAME = "lastsync.timestamp";

  private static final String VAR_DIRECTORY_NAME 
    = Application.CONFIG_PATH + "var";

  public TimestampUtil(String prefix)
  {
    currentTimestamp = new ASN1GeneralizedTime();
    this.prefix = prefix;
    
    if (System.getProperty("app.home") != null)
    {
      varDirectory = new File(System.getProperty("app.home")
        + File.separator + VAR_DIRECTORY_NAME);
    }
    else
    {
      varDirectory = new File(VAR_DIRECTORY_NAME);
    }

    LOGGER.debug("varDirectory={}", varDirectory.getAbsolutePath());

    if (!varDirectory.exists())
    {
      varDirectory.mkdirs();
    }

    timestampFile = new File(varDirectory, prefix + "-" + TIMESTAMP_FILENAME);

    LOGGER.debug("timestampFile={}", timestampFile.getAbsolutePath());

    ASN1GeneralizedTime timestamp = null;

    try
    {
      timestamp = readLastSyncTimestamp();
    }
    catch (Throwable t)
    {
      LOGGER.error("ERROR: Reading last timesync file", t);
      System.exit(-1);
    }

    lastSyncTimestamp = timestamp;
  }

  private final ASN1GeneralizedTime readLastSyncTimestamp() throws Throwable
  {
    ASN1GeneralizedTime timestamp = new ASN1GeneralizedTime(0l);

    if (timestampFile.exists() && timestampFile.canRead())
    {
      String timestampString;

      try (BufferedReader reader = new BufferedReader(new FileReader(
        timestampFile)))
      {
        timestampString = reader.readLine().trim();
      }

      if (timestampString != null && timestampString.length() > 0)
      {
        timestamp = new ASN1GeneralizedTime(timestampString);
      }
    }

    LOGGER.debug("last sync {} timestamp = {}", prefix, timestamp);
    return timestamp;
  }

  public void writeCurrentTimestamp() throws IOException
  {
    LOGGER.info("Writing timestamp to {}", timestampFile.getAbsolutePath());
    try (PrintWriter out = new PrintWriter(timestampFile))
    {
      out.println(currentTimestamp.toString());
    }
  }

  private final ASN1GeneralizedTime currentTimestamp;

  @Getter
  private final ASN1GeneralizedTime lastSyncTimestamp;

  private final File varDirectory;

  private final File timestampFile;

  private final String prefix;
}
