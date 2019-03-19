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

package io.spine.tools.bootstrap;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.tools.bootstrap.ProtobufGenerator.ProtocBuiltIn;
import io.spine.tools.gradle.Artifact;
import org.gradle.api.Project;

import java.io.File;

import static io.spine.tools.bootstrap.ProtobufGenerator.ProtocBuiltIn.Name.java;
import static org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME;

/**
 * An extension which configures Java code generation.
 */
public final class JavaExtension extends CodeGenExtension {

    private final Project project;
    private final String spineVersion;
    private final CodeLayout codeLayout;
    private final DependencyTarget dependencyTarget;

    private boolean grpc = false;

    JavaExtension(Project project,
                  ProtobufGenerator generator,
                  PluginTarget pluginTarget,
                  CodeLayout codeLayout,
                  DependencyTarget dependencyTarget) {
        super(generator, ProtocBuiltIn.called(java), pluginTarget);
        this.project = project;
        this.codeLayout = codeLayout;
        this.dependencyTarget = dependencyTarget;
        this.spineVersion = Ext.of(project)
                               .versions()
                               .spine();
    }

    /**
     * Indicates whether the gRPC stub generation is enabled or not.
     */
    public boolean getGrpc() {
        warnUnimplemented();
        return grpc;
    }

    /**
     * Enables or disables the gRPC stub generation.
     */
    public void setGrpc(boolean generateGrpc) {
        warnUnimplemented();
        this.grpc = generateGrpc;
    }

    public void client() {
        addSpineDependency(SpineModule.client);
    }

    public void server() {
        addSpineDependency(SpineModule.server);
    }

    private void warnUnimplemented() {
        _warn("gRPC configuration is not yet implemented via `spine` DSL.");
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    void enableGeneration() {
        super.enableGeneration();
        pluginTarget().applyModelCompiler();
        addSourceSets();
        addSpineDependency(SpineModule.base);
    }

    private void addSourceSets() {
        File projectDir = project.getProjectDir();
        File generatedDir = new File(projectDir, "generated");
        codeLayout.javaSourcesRoot(generatedDir.toPath());
    }

    private void addSpineDependency(SpineModule spineModule) {
        Artifact artifact = Artifact
                .newBuilder()
                .setGroup("io.spine")
                .setName(spineModule.notation())
                .setVersion(spineVersion)
                .build();
        dependencyTarget.dependOn(artifact);
    }

    private enum SpineModule {

        base, client, server;

        private static final String SPINE_PREFIX = "spine-";

        private String notation() {
            return SPINE_PREFIX + name();
        }
    }
}
