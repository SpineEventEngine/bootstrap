/*
 * Copyright 2019, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.gradle.bootstrap;

import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.project.PlugableProject;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.project.ProjectSourceSuperset;
import io.spine.tools.gradle.project.SourceSuperset;
import io.spine.tools.gradle.protoc.ProtobufGenerator;
import org.gradle.api.Project;

/**
 * Spine Bootstrap plugin entrance point.
 *
 * <p>This plugin configures the project in order to make it possible to develop Spine-based
 * applications. Many configurations are implicit. For a more fine-grain configuration, use
 * {@code spine-model-compiler} plugin, {@code spine-proto-js-plugin},
 * {@code java}/{@code java-library} plugin, and {@code com.google.protobuf} plugin directly.
 *
 * <p>A typical usage of the bootstrap plugin is as follows:
 * <pre>
 *     {@code
 *     // -- build.gradle --
 *
 *     plugins {
 *         id 'io.spine.tools.gradle.bootstrap'
 *     }
 *
 *     spine {
 *         enableJava {
 *             grpc = true
 *         }
 *         enableJavaScript()
 *     }
 *     }
 * </pre>
 *
 * <p>The example above configures the project to generate both Java and JS code from Protobuf.
 * Also, gRPC stubs and implementation bases are generated for the Protobuf services in Java.
 * Also, {@code java}, {@code com.google.protobuf}, {@code io.spine.tools.spine-model.compiler},
 * and {@code io.spine.tools.spine-proto-js-plugin} Gradle plugins are added to the project
 * automatically.
 */
public final class BootstrapPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        ArtifactSnapshot artifacts = ArtifactSnapshot.fromResources();
        applyExtension(project, artifacts);
        configureProtocArtifact(project, artifacts);
    }

    private static void applyExtension(Project project, ArtifactSnapshot artifacts) {
        PluginTarget plugableProject = new PlugableProject(project);
        SourceSuperset layout = ProjectSourceSuperset.of(project);
        SpineBasedProject dependant = SpineBasedProject.from(project);
        dependant.prepareRepositories(artifacts);
        Extension extension = Extension
                .newBuilder()
                .setProject(project)
                .setDependencyTarget(dependant)
                .setPluginTarget(plugableProject)
                .setLayout(layout)
                .setArtifactSnapshot(artifacts)
                .build();
        project.getExtensions()
               .add(Extension.NAME, extension);
        extension.disableJavaGeneration();
    }

    private static void configureProtocArtifact(Project project, ArtifactSnapshot artifacts) {
        ProtobufGenerator generator = new ProtobufGenerator(project);
        generator.useCompiler(artifacts.protoc());
    }
}
