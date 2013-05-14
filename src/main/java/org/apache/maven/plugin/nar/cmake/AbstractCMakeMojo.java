package org.apache.maven.plugin.nar.cmake;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.nar.AbstractResourcesMojo;
import org.sonatype.aether.util.artifact.ArtifacIdUtils;

/**
 * Abstract CMake keeps configuration
 * 
 * @author Mark Donszelmann
 */
public abstract class AbstractCMakeMojo
    extends AbstractResourcesMojo
{
    
    /**
     * Source directory for CMake style project
     * 
     * @parameter expression="${basedir}/src/cmake"
     * @required
     */
    private File cmakeSourceDirectory;

    /**
     * @return
     * @throws MojoFailureException
     * @throws MojoExecutionException 
     */
    protected final File getCMakeAOLTargetDirectory()
        throws MojoExecutionException
    {
    	return getLayout().getAOLCDirectory(getTargetDirectory(), getMavenProject().getArtifactId(),
                getMavenProject().getVersion(), getAOL().toString(), getClassifiersString(), null);
    }
    
    protected final File getCMakeSourceDirectory() {
        return cmakeSourceDirectory;
    }
    
    /**
     * Returns true if we do not want to use GNU on Windows
     * 
     * @return
     */
    protected final boolean useCMake() {
        return "cmake".equals(builder);
    }


}
