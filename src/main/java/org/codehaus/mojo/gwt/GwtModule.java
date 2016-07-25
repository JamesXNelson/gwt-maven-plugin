package org.codehaus.mojo.gwt;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.gwt.utils.GwtModuleReaderException;
import org.codehaus.plexus.util.xml.Xpp3Dom;




/**
 * <p>GwtModule class.</p>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 * @version $Id: $Id
 */
public class GwtModule
{
    private Xpp3Dom xml;

    private String name;

    private Set<GwtModule> inherits;

    private GwtModuleReader reader;
    
    private File sourceFile;

    /**
     * <p>Constructor for GwtModule.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param xml a {@link org.codehaus.plexus.util.xml.Xpp3Dom} object.
     * @param reader a {@link org.codehaus.mojo.gwt.GwtModuleReader} object.
     */
    public GwtModule( String name, Xpp3Dom xml, GwtModuleReader reader )
    {
        this.name = name;
        this.xml = xml;
        this.reader = reader;
    }

    private String getRenameTo()
    {
        return xml.getAttribute( "rename-to" );
    }

    /**
     * <p>getPublic.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPublic()
    {
        Xpp3Dom node = xml.getChild( "public" );
        return ( node == null ? "public" : node.getAttribute( "path" ) );
    }

    /**
     * <p>getSuperSources.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getSuperSources()
    {
        Xpp3Dom nodes[] = xml.getChildren( "super-source" );
        if ( nodes == null )
        {
            return new String[0];
        }
        String[] superSources = new String[nodes.length];
        int i = 0;
        for ( Xpp3Dom node : nodes )
        {
            String path = node.getAttribute( "path" );
            if ( path == null )
            {
                path = "";
            }
            superSources[i++] = path;
        }
        return superSources;
    }

    /**
     * <p>getSources.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getSources()
    {
        Xpp3Dom nodes[] = xml.getChildren( "source" );
        if ( nodes == null || nodes.length == 0 )
        {
            return new String[] { "client" };
        }
        String[] sources = new String[nodes.length];
        int i = 0;
        for ( Xpp3Dom node : nodes )
        {
            sources[i++] = node.getAttribute( "path" );
        }
        return sources;
    }

    /**
     * <p>getEntryPoints.</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.codehaus.mojo.gwt.utils.GwtModuleReaderException if any.
     */
    public List<String> getEntryPoints()
        throws GwtModuleReaderException
    {
        List<String> entryPoints = new ArrayList<String>();
        entryPoints.addAll( getLocalEntryPoints() );
        for ( GwtModule module : getInherits() )
        {
            entryPoints.addAll( module.getLocalEntryPoints() );
        }
        return entryPoints;
    }

    private List<String> getLocalEntryPoints()
    {
        Xpp3Dom nodes[] = xml.getChildren( "entry-point" );
        if ( nodes == null )
        {
            return Collections.emptyList();
        }
        List<String> entryPoints = new ArrayList<String>( nodes.length );
        for ( Xpp3Dom node : nodes )
        {
            entryPoints.add( node.getAttribute( "class" ) );
        }
        return entryPoints;
    }

    /**
     * Build the set of inhertied modules. Due to xml inheritence mecanism, there may be cicles in the inheritence
     * graph, so we build a set of inherited modules
     *
     * @return a {@link java.util.Set} object.
     * @throws org.codehaus.mojo.gwt.utils.GwtModuleReaderException if any.
     */
    public Set<GwtModule> getInherits()
		throws GwtModuleReaderException
    {
        if ( inherits != null )
        {
            return inherits;
        }

        inherits = new HashSet<GwtModule>();
        addInheritedModules( inherits, getLocalInherits() );

        return inherits;
    }

    /**
     * 
     * @param set
     * @param modules
     * @throws MojoExecutionException
     */
    private void addInheritedModules( Set<GwtModule> set, Set<GwtModule> modules )
        throws GwtModuleReaderException
    {
        for ( GwtModule module : modules )
        {
            if ( set.add( module ) )
            {
                // if module is allready in the set, don't re-parse it's inherits
                addInheritedModules( set, module.getLocalInherits() );
            }
        }

    }

    private Set<GwtModule> getLocalInherits()
        throws GwtModuleReaderException
    {
        Xpp3Dom nodes[] = xml.getChildren( "inherits" );
        if ( nodes == null )
        {
            return Collections.emptySet();
        }
        Set<GwtModule> modules = new HashSet<GwtModule>();
        for ( Xpp3Dom node : nodes )
        {
            String moduleName = node.getAttribute( "name" );
            // exclude modules from gwt-dev/gwt-user
            if ( !moduleName.startsWith( "com.google.gwt." ) )
            {
                modules.add( reader.readModule( moduleName ) );
            }
        }
        return modules;
    }

    /**
     * <p>getServlets.</p>
     *
     * @return a {@link java.util.Map} object.
     * @throws org.codehaus.mojo.gwt.utils.GwtModuleReaderException if any.
     */
    public Map<String, String> getServlets()
        throws GwtModuleReaderException
    {
        return getServlets( getPath() );
    }

    /**
     * <p>getServlets.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     * @throws org.codehaus.mojo.gwt.utils.GwtModuleReaderException if any.
     */
    public Map<String, String> getServlets( String path )
        throws GwtModuleReaderException
    {
        Map<String, String> servlets = getLocalServlets( path );
        for ( GwtModule module : getInherits() )
        {
            servlets.putAll( module.getLocalServlets( path ) );
        }
        return servlets;
    }

    private Map<String, String> getLocalServlets( String path )
    {
        Map<String, String> servlets = new HashMap<String, String>();
        Xpp3Dom nodes[] = xml.getChildren( "servlet" );
        if ( nodes != null )
        {
            for ( Xpp3Dom node : nodes )
            {
                servlets.put( StringUtils.isBlank( path ) ? node.getAttribute( "path" ) : path + node.getAttribute( "path" ),
                              node.getAttribute( "class" ) );
            }
        }
        return servlets;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName()
    {
        return name;
    }

    /**
     * <p>getPackage.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackage()
    {
        int index = name.lastIndexOf( '.' );
        return ( index < 0 ) ? "" : name.substring( 0, index );
    }

    /**
     * <p>getPath.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPath()
    {
        if ( getRenameTo() != null )
        {
            return getRenameTo();
        }
        return name;
    }

    /**
     * <p>Getter for the field <code>sourceFile</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getSourceFile() {
    	return sourceFile;
    }
    
    /**
     * <p>Setter for the field <code>sourceFile</code>.</p>
     *
     * @param file a {@link java.io.File} object.
     */
    public void setSourceFile(File file) {
		this.sourceFile = file;
	}
	
    /** {@inheritDoc} */
    @Override
    public boolean equals( Object obj )
    {
        return name.equals( ( (GwtModule) obj ).name );
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

}
