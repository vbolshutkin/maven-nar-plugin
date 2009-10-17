package org.apache.maven.plugin.nar;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * Copies any resources, including AOL specific distributions, to the target area for packaging
 * 
 * @goal nar-resources
 * @phase process-resources
 * @requiresProject
 * @author Mark Donszelmann
 */
public class NarResourcesMojo
    extends AbstractResourcesMojo
{

    /**
     * Directory for nar resources. Defaults to src/nar/resources
     * 
     * @parameter expression="${basedir}/src/nar/resources"
     * @required
     */
    private File resourceDirectory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( shouldSkip() )
            return;

        // scan for AOLs
        File aolDir = new File( resourceDirectory, "aol" );
        if ( aolDir.exists() )
        {
            String[] aols = aolDir.list();
            for ( int i = 0; i < aols.length; i++ )
            {
                boolean ignore = false;
                for ( Iterator j = FileUtils.getDefaultExcludesAsList().iterator(); j.hasNext(); )
                {
                    String exclude = (String) j.next();
                    if ( SelectorUtils.matchPath( exclude.replace( '/', File.separatorChar ), aols[i] ) )
                    {
                        ignore = true;
                        break;
                    }
                }
                if ( !ignore )
                {
                    File aol = new File( aolDir, aols[i] );
                    copyResources( aol, aol.getName() );
                }
            }
        }
    }
}