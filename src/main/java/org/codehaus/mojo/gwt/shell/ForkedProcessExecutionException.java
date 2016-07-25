package org.codehaus.mojo.gwt.shell;

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


import org.apache.maven.plugin.MojoExecutionException;

/**
 * <p>ForkedProcessExecutionException class.</p>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 * @version $Id: $Id
 */
public class ForkedProcessExecutionException
    extends MojoExecutionException
{

    /**
     * <p>Constructor for ForkedProcessExecutionException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ForkedProcessExecutionException( String message )
    {
        super( message );
    }

    /**
     * <p>Constructor for ForkedProcessExecutionException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Exception} object.
     */
    public ForkedProcessExecutionException( String message, Exception cause )
    {
        super( message, cause );
    }

    /**
     * <p>Constructor for ForkedProcessExecutionException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public ForkedProcessExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * <p>Constructor for ForkedProcessExecutionException.</p>
     *
     * @param source a {@link java.lang.Object} object.
     * @param shortMessage a {@link java.lang.String} object.
     * @param longMessage a {@link java.lang.String} object.
     */
    public ForkedProcessExecutionException( Object source, String shortMessage, String longMessage )
    {
        super( source, shortMessage, longMessage );
    }

}
