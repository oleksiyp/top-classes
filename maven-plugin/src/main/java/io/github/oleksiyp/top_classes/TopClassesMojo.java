package io.github.oleksiyp.top_classes;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.PluginArtifact;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Goal which calculates top used classes.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo( name = "top-classes", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, threadSafe = true )
public class TopClassesMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true )
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException
    {
        Artifact artifact = project.getArtifact();
        if (!"jar".equals(artifact.getClassifier())) {
            return;
        }

//        project.addAttachedArtifact(new DefaultArtifact(
//                artifact.getGroupId(),
//                artifact.getArtifactId(),
//                artifact.getVersion(),
//                artifact.getScope(),
//                artifact.getType(),
//                "top",
//                new DefaultArtifactHandler("top")
//        ));

        File jarFile = artifact.getFile();
        getLog().info(jarFile.toString());
    }
}
