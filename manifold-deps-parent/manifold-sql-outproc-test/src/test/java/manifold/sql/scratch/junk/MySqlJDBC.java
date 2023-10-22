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

package manifold.sql.scratch.junk;

import manifold.util.ManExceptionUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

public class MySqlJDBC extends CommonJDBC
{

  public static void main( String[] args )
  {
//    scratchStuff();
    printAllTypes();
  }

  private static void printAllTypes()
  {

    try
    {
      Connection c = DriverManager.getConnection( "jdbc:mysql://localhost:3306/MysqlSakila?user=root&password=password" );
      System.out.println( "Opened database successfully" );

      DatabaseMetaData metaData = c.getMetaData();

      printAllDataTypes( metaData );
    }
    catch( Exception e )
    {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      throw ManExceptionUtil.unchecked( e );
    }
  }
}