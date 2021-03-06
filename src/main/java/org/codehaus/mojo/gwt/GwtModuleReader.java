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

 import java.util.List;

import org.codehaus.mojo.gwt.utils.GwtModuleReaderException;


/**
 * <p>GwtModuleReader interface.</p>
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 * @version $Id: $Id
 */
public interface GwtModuleReader
{
    /**
     * <p>readModule.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.codehaus.mojo.gwt.GwtModule} object.
     * @throws org.codehaus.mojo.gwt.utils.GwtModuleReaderException if any.
     */
    GwtModule readModule( String name )
		throws GwtModuleReaderException;
    
    /**
     * <p>getGwtModules.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getGwtModules();
}
