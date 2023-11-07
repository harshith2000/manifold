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

package manifold.sql.rt.impl.accessors;

import manifold.sql.rt.api.BaseElement;
import manifold.sql.rt.api.ValueAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class SmallIntValueAccessor extends IntegerValueAccessor
{
  @Override
  public int getJdbcType()
  {
    return Types.SMALLINT;
  }

  // Note, we treat SMALLINT as INTEGER because Short type requires casting of int values including integral literals.
  // As a consequence, short doesn't really provide much in terms of a statically enforceable constraint. Casting causes
  // confusion and just sucks, so we favor the int type here via extending IntegerValueAccessor.

//  @Override
//  public Class<?> getJavaType( BaseElement elem )
//  {
//    return elem.canBeNull() ? Short.class : short.class;
//  }
//
//  @Override
//  public Short getRowValue( ResultSet rs, BaseElement elem ) throws SQLException
//  {
//    short value = rs.getShort( elem.getPosition() );
//    return value == 0 && rs.wasNull() ? null : value;
//  }
//
//  @Override
//  public void setParameter( PreparedStatement ps, int pos, Object value ) throws SQLException
//  {
//    if( value == null )
//    {
//      ps.setNull( pos, getJdbcType() );
//    }
//    else if( value instanceof Number )
//    {
//      ps.setShort( pos, ((Number)value).shortValue() );
//    }
//    else
//    {
//      ps.setObject( pos, value, getJdbcType() );
//    }
//  }
}
