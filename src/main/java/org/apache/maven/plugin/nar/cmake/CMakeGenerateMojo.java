package org.apache.maven.plugin.nar.cmake;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.nar.util.MojoUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Goal which generates project files.
 * 
 * @goal nar-cmake-generate
 * @phase process-sources
 * 
 * @author Gili Tzabari
 * @author Vladimir Bolshutkin
 */
public class CMakeGenerateMojo extends AbstractCMakeMojo {
	/**
	 * The release platform.
	 * 
	 * @parameter expression="${classifier}"
	 * @readonly
	 */
	private String classifier;
	/**
	 * The directory containing CMakeLists.txt
	 * 
	 * @parameter default-value="${basedir}/src/cmake"
	 */
	private File sourcePath;
	/**
	 * The output directory.
	 * 
	 * @parameter default-value="${project.build.directory}"
	 */
	private File targetPath;
	
	/**
	 * The output directory.
	 * 
	 * @parameter default-value="build"
	 */
	private String tmpBuildPath;
	
	/**
	 * Arguments to pass to GNU configure.
	 * 
	 * @parameter expression="${nar.cmake.generate.args}" default-value=""
	 */
	private String cmakeGenerateArgs;
	
	/**
	 * The makefile generator to use.
	 * 
	 * @parameter default-value=""
	 */
	private String generator;
	/**
	 * The environment variables.
	 * 
	 * @parameter
	 */
	private Map<String, String> environmentVariables;
	/**
	 * @component
	 */
	private BuildPluginManager pluginManager;
	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;
	/**
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession session;
	private final boolean isPosix = !System.getProperty("os.name")
			.toLowerCase().startsWith("windows");

	@Override
	public void narExecute() throws MojoExecutionException {
		
		if (!useCMake()) {
			return;
		}
		
		FileUtils.mkdir(targetPath.getAbsolutePath());
		
		List<String> command = new ArrayList<String>();
		command.add("cmake");
		command.add(sourcePath.getAbsolutePath());
		
		if (!StringUtils.isEmpty(generator)) {
			command.add("-G");
			command.add(generator);
		}
		command.add("-D" + "TARGET_PATH" + "=" + getCMakeAOLTargetDirectory().getAbsolutePath());
		command.add("-D" + "BUILD_PATH" + "=" + tmpBuildPath);
	    command.add(cmakeGenerateArgs);

		ProcessBuilder processBuilder = new ProcessBuilder(command)
				.directory(targetPath);

		Map<String, String> env = processBuilder.environment();

		if (environmentVariables != null)
			env.putAll(environmentVariables);
		Log log = getLog();
		if (log.isDebugEnabled()) {
			log.debug("sourcePath: " + sourcePath);
			log.debug("targetPath: " + targetPath);
			log.debug("environment: " + processBuilder.environment());
			log.debug("command-line: " + processBuilder.command());
		}

		try {
			int returnCode = MojoUtils.waitFor(processBuilder);
			if (returnCode != 0)
				throw new MojoExecutionException("Return code: " + returnCode);
		} catch (Exception e) {
			throw new MojoExecutionException("", e);
		}

	}

}
