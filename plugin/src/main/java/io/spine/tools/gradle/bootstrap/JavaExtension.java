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

import io.spine.tools.gradle.DependencyTarget;
import io.spine.tools.gradle.DirectoryStructure;
import io.spine.tools.gradle.Ext;
import io.spine.tools.gradle.GeneratedSourceRoot;
import io.spine.tools.gradle.SpineModule;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ProtobufArtifacts.protobufLite;
import static io.spine.tools.gradle.SpineModule.client;
import static io.spine.tools.gradle.SpineModule.server;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.grpc;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.java;
import static io.spine.tools.gradle.protoc.ProtocPlugin.called;

/**
 * An extension which configures Java code generation.
 */
public final class JavaExtension extends CodeGenExtension {

    private final Project project;
    private final DirectoryStructure directoryStructure;

    private boolean generateGrpc = false;

    private JavaExtension(Builder builder) {
        super(builder);
        this.project = builder.project();
        this.directoryStructure = builder.directoryStructure();
    }

    /**
     * Indicates whether the gRPC stub generation is enabled or not.
     */
    public boolean getGrpc() {
        return generateGrpc;
    }

    /**
     * Enables or disables the gRPC stub generation.
     */
    public void setGrpc(boolean generateGrpc) {
        this.generateGrpc = generateGrpc;
        if (generateGrpc) {
            protobufGenerator().enablePlugin(called(grpc));
            addGrpcDependencies();
        } else {
            protobufGenerator().disablePlugin(called(grpc));
        }
    }

    /**
     * Marks this project as a Java client of the system.
     *
     * <p>Adds the {@code io.spine:spine-client} dependency to the project.
     */
    public void client() {
        dependOn(client);
        fixRuntimeConfigurations();
    }

    /**
     * Marks this project as a part of a Java server.
     *
     * <p>Adds the {@code io.spine:spine-server} dependency to the project.
     */
    public void server() {
        dependOn(server);
        fixRuntimeConfigurations();
    }

    private void dependOn(SpineModule module) {
        dependencyTarget().compile(module.withVersion(spineVersion()));
    }

    private void fixRuntimeConfigurations() {
        dependencyTarget().exclude(protobufLite());
    }

    @Override
    void enableGeneration() {
        super.enableGeneration();
        pluginTarget().applyModelCompiler();
        addSourceSets();
    }

    private void addSourceSets() {
        GeneratedSourceRoot sourceRoot = GeneratedSourceRoot.of(project);
        directoryStructure.markCodeGenRoot(sourceRoot);
    }

    private void addGrpcDependencies() {
        DependencyTarget dependencyTarget = dependencyTarget();
        Ext.of(project)
           .artifacts()
           .grpc()
           .forEach(dependencyTarget::implementation);
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder extends CodeGenExtension.Builder<JavaExtension, Builder> {

        private DirectoryStructure directoryStructure;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
            super(called(java));
        }

        private DirectoryStructure directoryStructure() {
            return directoryStructure;
        }

        Builder setDirectoryStructure(DirectoryStructure directoryStructure) {
            this.directoryStructure = directoryStructure;
            return this;
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        JavaExtension build() {
            checkNotNull(directoryStructure);
            return super.build();
        }

        @Override
        JavaExtension doBuild() {
            return new JavaExtension(this);
        }
    }
}
