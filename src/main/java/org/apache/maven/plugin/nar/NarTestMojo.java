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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Tests NAR files. Runs Native Tests and executables if produced.
 * 
 * @goal nar-test
 * @phase test
 * @requiresProject
 * @author Mark Donszelmann
 */
public class NarTestMojo
    extends AbstractCompileMojo
{
    /**
     * The classpath elements of the project being tested.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List classpathElements;

    public final void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( shouldSkip() )
        {
            return;
        }

        // run all tests
        for ( Iterator i = getTests().iterator(); i.hasNext(); )
        {
            runTest( (Test) i.next() );
        }

        for ( Iterator i = getLibraries().iterator(); i.hasNext(); )
        {
            runExecutable( (Library) i.next() );
        }
    }

    private void runTest( Test test )
        throws MojoExecutionException, MojoFailureException
    {
        // run if requested
        if ( test.shouldRun() )
        {
            String name = getTestTargetDirectory().getPath() + "/bin/" + getAOL() + "/" + test.getName();
            getLog().info( "Running test " + name );

            File workingDir = getMavenProject().getBasedir();
            getLog().info( "  in " + workingDir );
            List args = test.getArgs();
            int result =
                NarUtil.runCommand( name, (String[]) args.toArray( new String[args.size()] ), workingDir,
                                    generateEnvironment(), getLog() );
            if ( result != 0 )
            {
                throw new MojoFailureException( "Test " + name + " failed with exit code: " + result + " 0x"
                    + Integer.toHexString( result ) );
            }
        }
    }

    private void runExecutable( Library library )
        throws MojoExecutionException, MojoFailureException
    {
        if ( library.getType().equals( Library.EXECUTABLE ) && library.shouldRun() )
        {
            MavenProject project = getMavenProject();
            // FIXME NAR-90, we could make dure we get the final name from layout
            File executable =
                new File( getLayout().getBinDirectory( getTargetDirectory(), getAOL().toString() ),
                          project.getArtifactId() );
            getLog().info( "Running executable " + executable );
            List args = library.getArgs();
            int result =
                NarUtil.runCommand( executable.getPath(), (String[]) args.toArray( new String[args.size()] ), null,
                                    generateEnvironment(), getLog() );
            if ( result != 0 )
            {
                throw new MojoFailureException( "Test " + executable + " failed with exit code: " + result + " 0x"
                    + Integer.toHexString( result ) );
            }
        }
    }

    protected final File getTestTargetDirectory()
    {
        return new File( getMavenProject().getBuild().getDirectory(), "test-nar" );
    }

    private String[] generateEnvironment()
        throws MojoExecutionException, MojoFailureException
    {
        List env = new ArrayList();

        Set/* <File> */sharedPaths = new HashSet();

        // add all shared libraries of this package
        for ( Iterator i = getLibraries().iterator(); i.hasNext(); )
        {
            Library lib = (Library) i.next();
            if ( lib.getType().equals( Library.SHARED ) )
            {
                File path =
                    getLayout().getLibDirectory( getTargetDirectory(), getAOL().toString(), lib.getType() );
                getLog().debug( "Adding path to shared library: " + path );
                sharedPaths.add( path );
            }
        }

        // add dependent shared libraries
        String classifier = getAOL() + "-shared";
        List narArtifacts = getNarManager().getNarDependencies( "compile" );
        List dependencies = getNarManager().getAttachedNarDependencies( narArtifacts, classifier );
        for ( Iterator d = dependencies.iterator(); d.hasNext(); )
        {
            Artifact dependency = (Artifact) d.next();
            getLog().debug( "Looking for dependency " + dependency );

            // FIXME reported to maven developer list, isSnapshot
            // changes behaviour
            // of getBaseVersion, called in pathOf.
            dependency.isSnapshot();

            // FIXME NAR-90
            File libDir = new File( getLocalRepository().pathOf( dependency ) );
            libDir = new File( getLocalRepository().getBasedir(), libDir.getParent() );
            libDir = new File( libDir, "nar/lib/" + getAOL() + "/shared" );
            sharedPaths.add( libDir );
        }

        // set environment
        if ( sharedPaths.size() > 0 )
        {
            String sharedPath = "";
            for ( Iterator i = sharedPaths.iterator(); i.hasNext(); )
            {
                sharedPath += ( (File) i.next() ).getPath();
                if ( i.hasNext() )
                {
                    sharedPath += File.pathSeparator;
                }
            }

            String sharedEnv = NarUtil.addLibraryPathToEnv( sharedPath, null, getOS() );
            env.add( sharedEnv );
        }

        // necessary to find WinSxS
        if ( getOS().equals( OS.WINDOWS ) )
        {
            env.add( "SystemRoot=" + NarUtil.getEnv( "SystemRoot", "SystemRoot", "C:\\Windows" ) );
        }

        // add CLASSPATH
        env.add( "CLASSPATH=" + StringUtils.join( classpathElements.iterator(), File.pathSeparator ) );

        return env.size() > 0 ? (String[]) env.toArray( new String[env.size()] ) : null;
    }
}
