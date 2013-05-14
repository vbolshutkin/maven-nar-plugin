package org.apache.maven.plugin.nar.gnu;

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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.nar.common.NarUtil;

/**
 * Move the GNU style output in the correct directories for nar-package
 * 
 * @goal nar-gnu-process
 * @phase process-classes
 * @requiresProject
 * @author Mark Donszelmann
 */
public class NarGnuProcess
    extends AbstractGnuMojo
{
    public final void narExecute()
        throws MojoExecutionException, MojoFailureException
    {
        File srcDir = getGnuAOLTargetDirectory();
        if ( srcDir.exists() )
        {
            getLog().info( "Running GNU process" );

            // copyResources( srcDir, getAOL().toString() );
            File destDir = getLayout().getAOLCDirectory(getTargetDirectory(), getMavenProject().getArtifactId(),
                    getMavenProject().getVersion(), getAOL().toString(), getClassifiersString(), null);
            try {
				NarUtil.copyDirectoryStructure( srcDir, destDir, null, NarUtil.DEFAULT_EXCLUDES );
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage());
			}
        }
    }
}
