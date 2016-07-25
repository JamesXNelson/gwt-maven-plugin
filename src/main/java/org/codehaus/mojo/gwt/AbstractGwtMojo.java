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

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.plexus.util.StringUtils;

/**
 * Abstract Support class for all GWT-related operations.
 * <p>
 * Provide methods to build classpath for GWT SDK tools.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 * @version $Id$
 */
public abstract class AbstractGwtMojo
    extends AbstractMojo
{
    private static final String GWT_USER = ":gwt-user";

    private static final String GWT_DEV = ":gwt-dev";

    /** GWT artifacts groupId */
    public static final String GWT_GROUP_ID = "net.wetheinter";

    // --- Some Maven tools ----------------------------------------------------

    @Parameter(defaultValue = "${plugin.artifactMap}", required = true, readonly = true)
    private Map<String, Artifact> pluginArtifactMap;

    @Component
    private MavenProjectBuilder projectBuilder;

    @Component
    protected ArtifactResolver resolver;

    @Component
    protected ArtifactFactory artifactFactory;

    @Component
    protected ClasspathBuilder classpathBuilder;

    // --- Some MavenSession related structures --------------------------------

    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    protected ArtifactRepository localRepository;

    @Parameter(defaultValue = "${project.pluginArtifactRepositories}", required = true, readonly = true)
    protected List<ArtifactRepository> remoteRepositories;

    @Component
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * The maven project descriptor
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    // --- Plugin parameters ---------------------------------------------------

    /**
     * Folder where generated-source will be created (automatically added to compile classpath).
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/gwt", required = true)
    private File generateDirectory;

    /**
     * Location on filesystem where GWT will write output files (-out option to GWTCompiler).
     */
    @Parameter(property = "gwt.war", defaultValue="${project.build.directory}/${project.build.finalName}", alias = "outputDirectory")
    private File webappDirectory;

    /**
     * Prefix to prepend to module names inside {@code webappDirectory} or in URLs in DevMode.
     * <p>
     * Could also be seen as a suffix to {@code webappDirectory}.
     */
    @Parameter(property = "gwt.modulePathPrefix")
    protected String modulePathPrefix;

    /**
     * Location of the web application static resources (same as maven-war-plugin parameter)
     */
    @Parameter(defaultValue="${basedir}/src/main/webapp")
    protected File warSourceDirectory;

    /**
     * Select the place where GWT application is built. In <code>inplace</code> mode, the warSourceDirectory is used to
     * match the same use case of the {@link war:inplace
     * http://maven.apache.org/plugins/maven-war-plugin/inplace-mojo.html} goal.
     */
    @Parameter(defaultValue = "false", property = "gwt.inplace")
    private boolean inplace;

    /**
     * Locations on the filesystem of additional source code to be included for
     * Gwt compilation
     */
    @Parameter(property = "gwt.extraSource")
    private List<File> extraSources;

    /**
     * For users who maintain forked copies of Gwt, allow swapping out the
     * standard com.google.gwt groupId for a custom value.
     */
    @Parameter(property = "gwt.groupId", defaultValue="net.wetheinter")
    private String groupId;

    /**
     * The forked command line will use gwt sdk jars first in classpath.
     * see issue http://code.google.com/p/google-web-toolkit/issues/detail?id=5290
     *
     * @since 2.1.0-1
     * @deprecated tweak your dependencies and/or split your project with a client-only module
     */
    @Deprecated
    @Parameter(defaultValue = "false", property = "gwt.gwtSdkFirstInClasspath")
    protected boolean gwtSdkFirstInClasspath;

    /**
     * <p>getOutputDirectory.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getOutputDirectory()
    {
        File out = inplace ? warSourceDirectory : webappDirectory;
        if ( !StringUtils.isBlank( modulePathPrefix ) )
        {
            out = new File(out, modulePathPrefix);
        }
        return out;
    }

    /**
     * Add classpath elements to a classpath URL set
     *
     * @param elements the initial URL set
     * @param urls the urls to add
     * @param startPosition the position to insert URLS
     * @return full classpath URL set
     * @throws org.apache.maven.plugin.MojoExecutionException some error occured
     */
    protected int addClasspathElements( final Collection<?> elements, final URL[] urls, int startPosition )
        throws MojoExecutionException
    {
        for ( final Object object : elements )
        {
            try
            {
                if ( object instanceof Artifact )
                {
                    urls[startPosition] = ( (Artifact) object ).getFile().toURI().toURL();
                }
                else if ( object instanceof Resource )
                {
                    urls[startPosition] = new File( ( (Resource) object ).getDirectory() ).toURI().toURL();
                }
                else
                {
                    urls[startPosition] = new File( (String) object ).toURI().toURL();
                }
            }
            catch ( final MalformedURLException e )
            {
                throw new MojoExecutionException(
                                                  "Failed to convert original classpath element " + object + " to URL.",
                                                  e );
            }
            startPosition++;
        }
        return startPosition;
    }


    /**
     * Build the GWT classpath for the specified scope
     *
     * @param scope Artifact.SCOPE_COMPILE or Artifact.SCOPE_TEST
     * @return a collection of dependencies as Files for the specified scope.
     * @throws org.apache.maven.plugin.MojoExecutionException if classPath building failed
     */
    public Collection<File> getClasspath( final String scope )
        throws MojoExecutionException
    {
        try
        {
            final Collection<File> files = classpathBuilder.buildClasspathList( getProject(), scope, getProjectArtifacts(), isGenerator() );

            if ( getLog().isDebugEnabled() )
            {
                getLog().debug( "GWT SDK execution classpath :" );
                for ( final File f : files )
                {
                    getLog().debug( "   " + f.getAbsolutePath() );
                }
            }
            return files;
        }
        catch ( final ClasspathBuilderException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }


    /**
     * Whether to use processed resources and compiled classes ({@code false}), or raw resources ({@code true }).
     *
     * @return a boolean.
     */
    protected boolean isGenerator() {
        return false;
    }

    /**
     * <p>getGwtDevJar.</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     */
    protected Collection<File> getGwtDevJar() throws MojoExecutionException
    {
        return getJarFiles( groupId + GWT_DEV );
    }

    /**
     * <p>getGwtUserJar.</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     */
    protected Collection<File> getGwtUserJar() throws MojoExecutionException
    {
        return getJarFiles( groupId + GWT_USER );
    }

    private Collection<File> getJarFiles(final String artifactId) throws MojoExecutionException
    {
        checkGwtUserVersion();
        final Artifact rootArtifact = pluginArtifactMap.get( artifactId );

        if (rootArtifact == null) {
          getLog().warn("Unable to find " + artifactId+" in artifactMap."
              + "You should explicitly include this dependency");
          throw new MojoExecutionException( "Failed to resolve artifact "+artifactId+" from \n"+pluginArtifactMap);
        }

        ArtifactResolutionResult result;
        try
        {
            // Code shamelessly copied from exec-maven-plugin.
            final MavenProject rootProject =
                            this.projectBuilder.buildFromRepository( rootArtifact, this.remoteRepositories,
                                                                     this.localRepository );
            final List<Dependency> dependencies = rootProject.getDependencies();
            final Set<Artifact> dependencyArtifacts =
                            MavenMetadataSource.createArtifacts( artifactFactory, dependencies, null, null, null );
            dependencyArtifacts.add( rootProject.getArtifact() );
            result = resolver.resolveTransitively( dependencyArtifacts, rootArtifact,
                                                   Collections.EMPTY_MAP, localRepository,
                                                   remoteRepositories, artifactMetadataSource,
                                                   null, Collections.EMPTY_LIST);
        }
        catch (final Exception e)
        {
            throw new MojoExecutionException( "Failed to resolve artifact "+artifactId+" from \n"+pluginArtifactMap, e);
        }

        final Collection<Artifact> resolved = result.getArtifacts();
        final Collection<File> files = new ArrayList<File>(resolved.size() + 1 );
        files.add( rootArtifact.getFile() );
        for ( final Artifact artifact : resolved )
        {
            files.add( artifact.getFile() );
        }

        return files;
    }

    /**
     * Check gwt-user dependency matches plugin version
     */
    private void checkGwtUserVersion() throws MojoExecutionException
    {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream( "org/codehaus/mojo/gwt/mojoGwtVersion.properties" );
        final Properties properties = new Properties();
        try
        {
            properties.load( inputStream );

        }
        catch (final IOException e)
        {
            throw new MojoExecutionException( "Failed to load plugin properties", e );
        }
        finally
        {
            IOUtils.closeQuietly( inputStream );
        }

        final Artifact gwtUser = project.getArtifactMap().get( groupId + GWT_USER );
        if (gwtUser != null)
        {
            final String mojoGwtVersion = properties.getProperty( "gwt.version" );
            //ComparableVersion with an up2date maven version
            final ArtifactVersion mojoGwtArtifactVersion = new DefaultArtifactVersion( mojoGwtVersion );
            final ArtifactVersion userGwtArtifactVersion = new DefaultArtifactVersion( gwtUser.getVersion() );
            if ( userGwtArtifactVersion.compareTo( mojoGwtArtifactVersion ) < 0 )
            {
                getLog().warn( "Your project declares dependency on gwt-user " + gwtUser.getVersion()
                                   + ". This plugin is designed for at least gwt version " + mojoGwtVersion );
            }
        }
    }

    /**
     * <p>resolve.</p>
     *
     * @param groupId a {@link java.lang.String} object.
     * @param artifactId a {@link java.lang.String} object.
     * @param version a {@link java.lang.String} object.
     * @param type a {@link java.lang.String} object.
     * @param classifier a {@link java.lang.String} object.
     * @return a {@link org.apache.maven.artifact.Artifact} object.
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     */
    protected Artifact resolve( final String groupId, final String artifactId, final String version, final String type, final String classifier )
        throws MojoExecutionException
    {
        // return project.getArtifactMap().get( groupId + ":" + artifactId );

        final Artifact artifact = artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        try
        {
            resolver.resolve(artifact, remoteRepositories, localRepository);
        }
        catch ( final ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "artifact not found - " + e.getMessage(), e );
        }
        catch ( final ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "artifact resolver problem - " + e.getMessage(), e );
        }
        return artifact;
    }

    /**
     * <p>addCompileSourceRoot.</p>
     *
     * @param path file to add to the project compile directories
     */
    protected void addCompileSourceRoot( final File path )
    {
        getProject().addCompileSourceRoot( path.getAbsolutePath() );
    }

    /**
     * <p>addExtraSources.</p>
     */
    protected void addExtraSources() {
      for (final File extraSource : extraSources) {
        addCompileSourceRoot(extraSource);
      }
    }
    /**
     * <p>Getter for the field <code>project</code>.</p>
     *
     * @return the project
     */
    public MavenProject getProject()
    {
        return project;
    }


    /**
     * <p>Getter for the field <code>localRepository</code>.</p>
     *
     * @return a {@link org.apache.maven.artifact.repository.ArtifactRepository} object.
     */
    public ArtifactRepository getLocalRepository()
    {
        return this.localRepository;
    }

    /**
     * <p>Getter for the field <code>remoteRepositories</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ArtifactRepository> getRemoteRepositories()
    {
        return this.remoteRepositories;
    }

    /**
     * <p>setupGenerateDirectory.</p>
     *
     * @return a {@link java.io.File} object.
     */
    protected File setupGenerateDirectory() {
        if ( !generateDirectory.exists() )
        {
            getLog().debug( "Creating target directory " + generateDirectory.getAbsolutePath() );
            generateDirectory.mkdirs();
        }
        getLog().debug( "Add compile source root " + generateDirectory.getAbsolutePath() );
        addCompileSourceRoot( generateDirectory );
        return generateDirectory;
    }

    /**
     * <p>Getter for the field <code>generateDirectory</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getGenerateDirectory()
    {
        if ( !generateDirectory.exists() )
        {
            getLog().debug( "Creating target directory " + generateDirectory.getAbsolutePath() );
            generateDirectory.mkdirs();
        }
        return generateDirectory;
    }

    /**
     * <p>getProjectArtifacts.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Artifact> getProjectArtifacts()
    {
        return project.getArtifacts();
    }

    /**
     * <p>getProjectRuntimeArtifacts.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Artifact> getProjectRuntimeArtifacts()
    {
        final Set<Artifact> artifacts = new HashSet<Artifact>();
        for (final Artifact projectArtifact : project.getArtifacts() )
        {
            final String scope = projectArtifact.getScope();
            if ( SCOPE_RUNTIME.equals( scope )
              || SCOPE_COMPILE.equals( scope ) )
            {
                artifacts.add( projectArtifact );
            }

        }
        return artifacts;
    }


}
