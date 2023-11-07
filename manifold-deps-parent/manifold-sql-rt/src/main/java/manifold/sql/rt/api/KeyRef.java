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

package manifold.sql.rt.api;

public class KeyRef
{
  private final Entity _ref;
  private final String _keyColName;

  public KeyRef( Entity ref, String keyColName )
  {
    _ref = ref;
    _keyColName = keyColName;
  }

  public Entity getRef()
  {
    return _ref;
  }

  public String getKeyColName()
  {
    return _keyColName;
  }
}
