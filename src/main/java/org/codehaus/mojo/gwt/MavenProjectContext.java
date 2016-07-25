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
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

/**
 * <p>MavenProjectContext class.</p>
 *
 * @author ndeloof
 * @version $Id$
 */
public class MavenProjectContext
{

    private MavenProject project;

    private ArtifactRepository localRepository;

    private List<ArtifactRepository> remoteRepositories;

    /**
     * <p>Constructor for MavenProjectContext.</p>
     *
     * @param project a {@link org.apache.maven.project.MavenProject} object.
     * @param localRepository a {@link org.apache.maven.artifact.repository.ArtifactRepository} object.
     * @param remoteRepositories a {@link java.util.List} object.
     */
    public MavenProjectContext( MavenProject project, ArtifactRepository localRepository,
                                List<ArtifactRepository> remoteRepositories )
    {
        super();
        this.project = project;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }

    /**
     * <p>Getter for the field <code>project</code>.</p>
     *
     * @return a {@link org.apache.maven.project.MavenProject} object.
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
        return localRepository;
    }

    /**
     * <p>getBuildDir.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getBuildDir()
    {
        return new File( project.getBuild().getDirectory() );
    }

    /**
     * <p>Getter for the field <code>remoteRepositories</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ArtifactRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }
}
