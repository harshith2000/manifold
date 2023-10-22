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

import manifold.sql.rt.api.ValueAccessor;
import manifold.sql.rt.api.ValueAccessorProvider;
import manifold.util.concurrent.LocklessLazyVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultValueAccessorProvider implements ValueAccessorProvider
{
  private static final Logger LOGGER = LoggerFactory.getLogger( DefaultValueAccessorProvider.class );

  private final LocklessLazyVar<Map<Integer, ValueAccessor>> _byJdbcType =
    LocklessLazyVar.make( () -> {
      LinkedHashMap<Integer, ValueAccessor> map = new LinkedHashMap<>();
      for( Class<? extends ValueAccessor> accClass : getAll() )
      {
        try
        {
          ValueAccessor acc = accClass.newInstance();
          int jdbcType = acc.getJdbcType();
          map.put( jdbcType, acc );
        }
        catch( Exception e )
        {
          throw new RuntimeException( e );
        }
      }
      return map;
    } );

  public Class<? extends ValueAccessor>[] getAll()
  {
    //noinspection unchecked
    return new Class[]{
      ArrayValueAccessor.class,
      BinaryValueAccessor.class,
      BitValueAccessor.class,
      BlobValueAccessor.class,
      BooleanValueAccessor.class,
      CharValueAccessor.class,
      ClobValueAccessor.class,
      DataLinkValueAccessor.class,
      DateValueAccessor.class,
      DecimalValueAccessor.class,
      DoubleValueAccessor.class,
      FloatValueAccessor.class,
      IntegerValueAccessor.class,
      JavaObjectValueAccessor.class,
      DistinctValueAccessor.class,
      LongNvarcharValueAccessor.class,
      BigIntValueAccessor.class,
      LongVarBinaryValueAccessor.class,
      LongVarcharValueAccessor.class,
      NcharValueAccessor.class,
      NclobValueAccessor.class,
      NumericValueAccessor.class,
      NvarcharValueAccessor.class,
      OtherValueAccessor.class,
      RealValueAccessor.class,
      RowIdValueAccessor.class,
      SmallIntValueAccessor.class,
      SqlXmlValueAccessor.class,
      TimestampValueAccessor.class,
      TimestampWithTimeZoneValueAccessor.class,
      TimeValueAccessor.class,
      TimeWithTimeZoneValueAccessor.class,
      TinyIntValueAccessor.class,
      VarBinaryValueAccessor.class,
      VarcharValueAccessor.class,

      Oracle_IntervalYmValueAccessor.class,
      Oracle_IntervalDsValueAccessor.class,
      Oracle_TimestampLtzValueAccessor.class,
      Oracle_TimestampTzValueAccessor.class
    };
  }

  @Override
  public ValueAccessor get( int jdbcType )
  {
    ValueAccessor valueAccessor = _byJdbcType.get().get( jdbcType );
    if( valueAccessor == null )
    {
      // for example, Sql Server has a sql type 'datetimeoffset' with jdbc type: -155, which is internal to sql server
      LOGGER.warn( "No direct ValueAccessor implementation found for JDBC type: " + jdbcType + ".\n" +
        "Using default '" + DistinctValueAccessor.class.getSimpleName() + "'." );
      valueAccessor = _byJdbcType.get().get( Types.DISTINCT );
    }
    return valueAccessor;
  }
}
