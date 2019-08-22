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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import groovy.lang.Closure;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.GeneratedSourceRoot;
import io.spine.tools.gradle.ThirdPartyDependency;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.config.SpineDependency;
import io.spine.tools.gradle.project.SourceSuperset;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.implementation;
import static io.spine.tools.gradle.ConfigurationName.testImplementation;
import static io.spine.tools.gradle.ProtobufDependencies.protobufLite;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.java;
import static io.spine.tools.gradle.protoc.ProtocPlugin.called;
import static org.gradle.util.ConfigureUtil.configure;

/**
 * An extension which configures Java code generation.
 */
public final class JavaExtension extends CodeGenExtension {

    private final Project project;
    private final SourceSuperset directoryStructure;
    private final JavaCodegenExtension codegen;
    private final ArtifactSnapshot artifacts;

    private JavaExtension(Builder builder) {
        super(builder);
        this.project = builder.project();
        this.directoryStructure = builder.sourceSuperset();
        this.artifacts = builder.artifactSnapshot();
        this.codegen = JavaCodegenExtension.of(project, dependant(), artifacts);
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
        dependant().exclude(protobufLite());
    }

    public JavaCodegenExtension getCodegen() {
        return codegen;
    }

    public void codegen(Action<JavaCodegenExtension> config) {
        config.execute(codegen);
    }

    public void codegen(Closure config) {
        configure(config, codegen);
    }

    /**
     * Marks this project as a Java client of the system.
     *
     * <p>Adds the {@code io.spine:spine-client} and {@code io.spine:spine-testuil-client}
     * dependencies to the project.
     */
    public void client() {
        dependOn(SpineDependency.client(), implementation);
        dependOn(SpineDependency.testUtilClient(), testImplementation);
    }

    /**
     * Marks this project as a part of a Java server.
     *
     * <p>Adds the {@code io.spine:spine-server} and {@code io.spine:spine-testutil-server}
     * dependencies to the project.
     */
    public void server() {
        dependOn(SpineDependency.server(), implementation);
        dependOn(SpineDependency.testUtilServer(), testImplementation);
    }

    private void dependOn(SpineDependency module, ConfigurationName configurationName) {
        String spineVersion = artifacts.spineVersion();
        dependant().depend(configurationName, module.ofVersion(spineVersion).notation());
    }

    private void addSourceSets() {
        GeneratedSourceRoot sourceRoot = GeneratedSourceRoot.of(project);
        directoryStructure.register(sourceRoot);
    }

    @Override
    protected ImmutableMap<Dependency, String> forcedDependencies() {
        return ImmutableMap.of(ForcedDependency.PROTOBUF_JAVA.dependency(),
                               ForcedDependency.PROTOBUF_JAVA.version());
    }

    @VisibleForTesting
    enum ForcedDependency {
        PROTOBUF_JAVA("com.google.protobuf", "protobuf-java", "3.9.0");

        private final String group;
        private final String name;
        private final String version;

        ForcedDependency(String group, String name, String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }

        Dependency dependency() {
            ThirdPartyDependency dependency = new ThirdPartyDependency(group, name);
            return dependency;
        }

        String version() {
            return version;
        }
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
            super(called(java));
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
