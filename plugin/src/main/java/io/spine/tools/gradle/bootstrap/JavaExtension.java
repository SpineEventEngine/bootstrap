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

import io.spine.tools.gradle.GeneratedSourceRoot;
import io.spine.tools.gradle.compiler.Extension;
import io.spine.tools.gradle.config.Ext;
import io.spine.tools.gradle.config.SpineDependency;
import io.spine.tools.gradle.project.Dependant;
import io.spine.tools.gradle.project.SourceSuperset;
import io.spine.tools.gradle.protoc.ProtocPlugin;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ProtobufDependencies.protobufLite;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.grpc;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.java;
import static io.spine.tools.gradle.protoc.ProtocPlugin.called;

/**
 * An extension which configures Java code generation.
 */
public final class JavaExtension extends CodeGenExtension {

    private static final ProtocPlugin JAVA_PLUGIN = called(java);
    private static final ProtocPlugin GRPC_PLUGIN = called(grpc);

    private final Project project;
    private final SourceSuperset directoryStructure;

    private boolean grpcGen = false;
    private boolean codegen = true;

    private JavaExtension(Builder builder) {
        super(builder);
        this.project = builder.project();
        this.directoryStructure = builder.sourceSuperset();
    }

    @Override
    void enableGeneration() {
        super.enableGeneration();
        pluginTarget().applyModelCompiler();
        pluginTarget().apply(SpinePluginScripts.modelCompilerConfig());
        addSourceSets();
        excludeProtobufLite();
    }

    private void excludeProtobufLite() {
        dependencyTarget().exclude(protobufLite());
    }

    /**
     * Indicates whether the gRPC stub generation is enabled or not.
     *
     * @see #withGrpcGeneration()
     */
    public boolean getGrpc() {
        return grpcGen;
    }

    /**
     * Indicates whether the Java code generation is enabled or not.
     *
     * @see #withoutCodeGeneration()
     */
    public boolean getCodegen() {
        return codegen;
    }

    /**
     * Enables the gRPC stub generation.
     */
    public void withGrpcGeneration() {
        this.grpcGen = true;
        checkGrpcRequestValid();
        protobufGenerator().enablePlugin(GRPC_PLUGIN);
        addGrpcDependencies();
    }

    /**
     * Disables any kind of Java code generation, including Protobuf messages, gRPC stubs,
     * and validating builders.
     */
    public void withoutCodeGeneration() {
        this.codegen = false;
        checkGrpcRequestValid();
        protobufGenerator().disableBuiltIn(JAVA_PLUGIN);
        Extension modelCompilerExtension = project.getExtensions().getByType(Extension.class);
        modelCompilerExtension.generateValidatingBuilders = false;
        if (grpcGen) {
            protobufGenerator().disablePlugin(GRPC_PLUGIN);
        }
    }

    private void checkGrpcRequestValid() {
        if (!codegen && grpcGen) {
            _warn("Requested gRPC code generation. " +
                          "However, Java code generation is disabled for this project. " +
                          "No Java code will be generated.");
        }
    }

    /**
     * Marks this project as a Java client of the system.
     *
     * <p>Adds the {@code io.spine:spine-client} dependency to the project.
     */
    public void client() {
        dependOn(SpineDependency.client());
    }

    /**
     * Marks this project as a part of a Java server.
     *
     * <p>Adds the {@code io.spine:spine-server} dependency to the project.
     */
    public void server() {
        dependOn(SpineDependency.server());
    }

    private void dependOn(SpineDependency module) {
        dependencyTarget().compile(module.ofVersion(spineVersion()));
    }

    private void addSourceSets() {
        GeneratedSourceRoot sourceRoot = GeneratedSourceRoot.of(project);
        directoryStructure.register(sourceRoot);
    }

    private void addGrpcDependencies() {
        Dependant dependencyTarget = dependencyTarget();
        Ext.of(project)
           .artifacts()
           .grpc()
           .forEach(dependencyTarget::implementation);
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder extends CodeGenExtension.Builder<JavaExtension, Builder> {

        private SourceSuperset sourceSuperset;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
            super(JAVA_PLUGIN);
        }

        private SourceSuperset sourceSuperset() {
            return sourceSuperset;
        }

        Builder setSourceSuperset(SourceSuperset sourceSuperset) {
            this.sourceSuperset = sourceSuperset;
            return this;
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        JavaExtension build() {
            checkNotNull(sourceSuperset);
            return super.build();
        }

        @Override
        JavaExtension doBuild() {
            return new JavaExtension(this);
        }
    }
}
