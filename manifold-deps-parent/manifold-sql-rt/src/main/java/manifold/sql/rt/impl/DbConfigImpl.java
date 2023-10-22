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

package manifold.sql.rt.impl;

import manifold.api.fs.IFile;
import manifold.api.util.cache.FqnCache;
import manifold.json.rt.api.DataBindings;
import manifold.rt.api.Bindings;
import manifold.rt.api.util.StreamUtil;
import manifold.sql.rt.api.DbConfig;
import manifold.sql.rt.api.DbLocationProvider.Mode;
import manifold.sql.rt.util.PropertyExpressionProcessor;
import manifold.sql.rt.util.SqlScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static manifold.sql.rt.api.DbLocationProvider.Mode.Unknown;

public class DbConfigImpl implements DbConfig
{
  private static final Logger LOGGER = LoggerFactory.getLogger( DbConfigImpl.class );

  public static final DbConfig EMPTY = new DbConfigImpl( null, DataBindings.EMPTY_BINDINGS, Unknown );
  private static final Set<String> DDL = new LinkedHashSet<>();

  private final Bindings _bindings;
  private final Map<String, List<Consumer<Connection>>> _initializers;
  private transient Function<String, FqnCache<IFile>> _resByExt;

  public DbConfigImpl( Function<String, FqnCache<IFile>> resByExt, Bindings bindings, Mode mode )
  {
    this( resByExt, bindings, mode, null );
  }

  /**
   * Type-safe access to configuration from .dbconfig files.
   *
   * @param bindings JSON bindings from a .dbconfig file
   * @param exprHandler An optional handler to evaluate expressions in URL fields
   */
  public DbConfigImpl( Function<String, FqnCache<IFile>> resByExt, Bindings bindings, Mode mode, Function<String, String> exprHandler )
  {
    _initializers = new HashMap<>();
    processUrl( resByExt, bindings, mode, "url", exprHandler );
    processUrl( resByExt, bindings, mode, "buildUrl", exprHandler );
    _bindings = bindings;
    _resByExt = resByExt;
    assignDefaults();
  }

  /** For testing only!! */
  public DbConfigImpl( Bindings bindings, Mode mode )
  {
    this( null, bindings, mode );
  }

  private void assignDefaults()
  {
    if( _bindings.isEmpty() )
    {
      // empty bindings indicates EMPTY bindings, such as DataBindings.EMPTY_BINDINGS,
      // which are immutable
      return;
    }

    String schemaPackage = getSchemaPackage();
    if( schemaPackage == null || schemaPackage.isEmpty() )
    {
      _bindings.put( "schemaPackage", DbConfig.DEFAULT_SCHEMA_PKG );
      LOGGER.info( "No 'schemaPackage' defined in DbConfig: '" + getName() + "'. Using default: '" + schemaPackage + "'." );
    }
  }

  private void processUrl( Function<String, FqnCache<IFile>> resByExt, Bindings bindings, Mode mode, String key, Function<String, String> exprHandler )
  {
    String url = (String)bindings.get( key );
    if( url == null )
    {
      return;
    }
    PropertyExpressionProcessor.Result result = PropertyExpressionProcessor.process( resByExt, url, mode, exprHandler );
    bindings.put( key, result.url );
    _initializers.put( result.url, result.initializers ); // for testing purposes
  }

  @Override
  public void init( Connection connection, String url, String schemaName, String ddl ) throws SQLException
  {
    List<Consumer<Connection>> consumers = _initializers.get( url );
    for( Consumer<Connection> consumer : consumers )
    {
      // this is for testing purposes e.g., dynamically creating a db from a ddl script before the db is accessed
      consumer.accept( connection );
    }

    if( schemaName != null && !schemaName.isEmpty() )
    {
      // for drivers like oracle that don't provide a way to set the schema in the url
      connection.setSchema( schemaName );
    }

    // for testing, only relevant during compilation: need to create db.
    // Note, test execution framework handles DDL loading itself
    execDdl( connection, ddl );
  }

  private void execDdl( Connection connection, String ddl ) throws SQLException
  {
    if( ddl == null || ddl.isEmpty() || DDL.contains( ddl ) )
    {
      return;
    }
    DDL.add( ddl );

    if( !ddl.startsWith( "/" ) && !ddl.startsWith( "\\" ) )
    {
      ddl = "/" + ddl;
    }

    IFile ddlFile = null;
    if( _resByExt != null )
    {
      // at compile-time we must find the ddl resource file
      ddlFile = ResourceDbLocationProvider.maybeGetCompileTimeResource( _resByExt, Mode.CompileTime, ddl );
      //_resByExt = null;
    }

    try( InputStream stream = ddlFile == null ? getClass().getResourceAsStream( ddl ) : ddlFile.openInputStream() )
    {
      if( stream == null )
      {
        throw new RuntimeException( "No resource file found matching: " + ddl );
      }

      boolean isOracle = connection.getMetaData().getDatabaseProductName().toLowerCase().contains( "oracle" );
      String script = StreamUtil.getContent( new InputStreamReader( stream ) );
      SqlScriptRunner.runScript( connection, script,
        // this is the only way to let drop user fail and continue running the script with oracle :\
        isOracle ? (s, e) -> s.toLowerCase().contains( "drop user " ) : null );
    }
    catch( Exception e )
    {
      throw new SQLException( e );
    }
  }

  @Override
  public String getName()
  {
    return (String)_bindings.get( "name" );
  }

  @Override
  public String getCatalogName()
  {
    return (String)_bindings.get( "catalogName" );
  }

  @Override
  public String getSchemaName()
  {
    return (String)_bindings.get( "schemaName" );
  }

  @Override
  public String getPath()
  {
    return (String)_bindings.get( "path" );
  }

  @Override
  public String getUrl()
  {
    return (String)_bindings.get( "url" );
  }

  @Override
  public String getBuildUrl()
  {
    return (String)_bindings.get( "buildUrl" );
  }

  @Override
  public String getUser()
  {
    return (String)_bindings.get( "user" );
  }

  @Override
  public String getPassword()
  {
    return (String)_bindings.get( "password" );
  }

  @Override
  public boolean isDefault()
  {
    Boolean isDefault = (Boolean)_bindings.get( "isDefault" );
    return isDefault != null && isDefault;
  }

  @Override
  public String getSchemaPackage()
  {
    return (String)_bindings.get( "schemaPackage" );
  }

  @Override
  public Bindings getProperties()
  {
    return (Bindings)_bindings.get( "properties" );
  }

  @Override
  public String getDbDdl()
  {
    return (String)_bindings.get( "dbDdl" );
  }

  @Override
  public boolean equals( Object o )
  {
    if( this == o ) return true;
    if( !(o instanceof DbConfigImpl) ) return false;
    DbConfigImpl dbConfig = (DbConfigImpl)o;
    return _bindings.equals( dbConfig._bindings );
  }

  @Override
  public int hashCode()
  {
    return _bindings.hashCode();
  }
}
