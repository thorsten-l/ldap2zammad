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
package l9g.app.ldap2zammad.engine;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class JavaScriptEngine implements Closeable
{
  final static Logger LOGGER = LoggerFactory.
    getLogger(JavaScriptEngine.class);

  private final static String SCRIPT_TYPE = "js";

  private final static String SCRIPT_FILENAME = "ldap2zammad.js";

  public JavaScriptEngine() throws IOException
  {
    LOGGER.debug("JavaScriptEngine()");

    context = Context.newBuilder(SCRIPT_TYPE)
      .allowHostAccess(HostAccess.ALL)
      .allowHostClassLookup(className -> true)
      .allowHostClassLoading(true)
      .build();

    value = context.eval(Source.newBuilder(
      SCRIPT_TYPE, new File(SCRIPT_FILENAME)).build());
  }

  @Override
  public void close()
  {
    if (context != null)
    {
      context.close();
    }
  }

  private final Context context;

  @Getter
  private final Value value;
}
