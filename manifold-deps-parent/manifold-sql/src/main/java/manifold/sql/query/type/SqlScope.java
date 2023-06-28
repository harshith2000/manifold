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

package manifold.sql.query.type;

import manifold.api.fs.IFile;
import manifold.api.fs.IFileFragment;
import manifold.api.type.ITypeManifold;
import manifold.internal.javac.IIssue;
import manifold.sql.rt.api.DbConfig;
import manifold.sql.rt.connection.DbConfigImpl;
import manifold.sql.schema.api.Schema;
import manifold.sql.schema.api.SchemaProvider;
import manifold.sql.schema.type.SchemaManifold;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SqlScope
{
  private final SqlManifold _sqlManifold;
  private final List<IIssue> _issues;
  private final Schema _schema;

  SqlScope( SqlManifold sqlManifold, IFile dbConfigFile )
  {
    _sqlManifold = sqlManifold;
    _issues = new ArrayList<>();
    _schema = findSchema( dbConfigFile );
  }

  private Schema findSchema( IFile dbConfigFile )
  {
    // share the schema from the corresponding SchemaManifold

    Set<ITypeManifold> tms = _sqlManifold.getModule().findTypeManifoldsFor( dbConfigFile );
    SchemaManifold schemaManifold = (SchemaManifold)tms.stream()
      .filter( m -> m instanceof SchemaManifold )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "Could not find schema manifold for: " + dbConfigFile.getName() ) );
    return schemaManifold.getSchema( dbConfigFile );

// don't make a separate schema
//    return SchemaProvider.PROVIDERS.get().stream().map( sp -> sp.getSchema( _dbConfig ) ).filter( schema -> schema != null ).findFirst().orElse( null );
  }

  /**
   * Errant config.
   */
  private SqlScope( SqlManifold sqlManifold )
  {
    _sqlManifold = sqlManifold;
    _issues = new ArrayList<>();
    _schema = null;
  }

  public static SqlScope makeErrantScope( SqlManifold sqlManifold, String fqn, IFile file )
  {
    SqlScope errantScope = new SqlScope( sqlManifold );
    errantScope._issues.add( new SqlIssue( IIssue.Kind.Error, "SQL type '" + fqn + "' from file '" + file.getName() + "' is not covered in any .dbconfig files" ) );
    return errantScope;
  }

  boolean hasConfigErrors()
  {
    return _issues.stream().anyMatch( issue -> issue.getKind() == IIssue.Kind.Error );
  }

  public DbConfig getDbconfig()
  {
    Schema schema = getSchema();
    return schema == null ? DbConfigImpl.EMPTY : getSchema().getDbConfig();
  }

  public Schema getSchema()
  {
    return _schema;
  }

  List<IIssue> getIssues()
  {
    return _issues;
  }

  boolean appliesTo( IFile file )
  {
    if( hasConfigErrors() || getDbconfig().getName() == null )
    {
      return false;
    }

    String dbConfigName = findDbConfigName( file );
    if( dbConfigName != null )
    {
      return getDbconfig().getName().equals( dbConfigName );
    }

    return false;
  }

  public static boolean isDefaultScopeApplicable( IFile file )
  {
    String dbConfigName = findDbConfigName( file );
    return dbConfigName == null || dbConfigName.isEmpty();
  }

  static String findDbConfigName( IFile file )
  {
    if( file instanceof IFileFragment )
    {
      return ((IFileFragment)file).getScope();
    }

    // look for name like: MyQuery.MyDbConfigName.sql

    String fileBaseName = file.getBaseName();
    int dbconfigName = fileBaseName.lastIndexOf( '.' );
    if( dbconfigName < 0 )
    {
      // No secondary extension in name
      return null;
    }
    return fileBaseName.substring( dbconfigName + 1 );
  }

  public boolean isErrant()
  {
    return getDbconfig().getBuildUrlOtherwiseRuntimeUrl() == null;
  }
}