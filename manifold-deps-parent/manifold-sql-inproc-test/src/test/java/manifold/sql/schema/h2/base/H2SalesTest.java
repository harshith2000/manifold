/*
 * Copyright (c) 2023 - Manifold Systems LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package manifold.sql.schema.h2.base;

import manifold.sql.DbResourceFileTest;
import org.junit.After;
import org.junit.Before;

public abstract class H2SalesTest extends DbResourceFileTest
{
  private static final String DB_RESOURCE = "/samples/db/h2-sales.mv.db";

  @Before
  public void setup()
  {
    _setup( DB_RESOURCE );
  }

  @After
  public void cleanup()
  {
    _cleanup( DB_RESOURCE );
  }
}
