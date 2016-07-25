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

/**
 * <p>ClasspathBuilderException class.</p>
 *
 * @author <a href="mailto:olamy@apache.org">Olivier Lamy</a>
 * @since 2.1.0-1
 * @version $Id: $Id
 */
public class ClasspathBuilderException
    extends Exception
{
    /**
     * <p>Constructor for ClasspathBuilderException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public ClasspathBuilderException( String message, Throwable t )
    {
        super( message, t );
    }

    /**
     * <p>Constructor for ClasspathBuilderException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ClasspathBuilderException( String message )
    {
        super( message );
    }
}
