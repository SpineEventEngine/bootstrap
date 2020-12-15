/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.logging.Logging;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.project.Dependant;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.protoc.ProtobufGenerator;
import io.spine.tools.gradle.protoc.ProtocPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.config.SpineDependency.base;
import static io.spine.tools.gradle.config.SpineDependency.time;

/**
 * A part of the {@link Extension spine} extension which configures certain code generation tasks.
 *
 * <p>One extension is responsible for one programming language to generate code in.
 */
abstract class CodeGenExtension implements Logging {

    private final ProtobufGenerator protobufGenerator;

    /**
     * The {@code protoc} plugin to be enabled if the extension is applied.
     *
     * <p>The value may be {@code null} to indicate that no code generation is required. In such
     * cases, Protobuf code generation should not be enabled unless specified otherwise by other
     * extensions.
     */
    private final @Nullable ProtocPlugin codeGenJob;
    private final SpinePluginTarget pluginTarget;
    private final Dependant dependant;
    private final ArtifactSnapshot artifactSnapshot;

    CodeGenExtension(Builder<?, ?> builder) {
        this.protobufGenerator = builder.protobufGenerator();
        this.codeGenJob = builder.codeGenJob();
        this.pluginTarget = new SpinePluginTarget(builder.pluginTarget());
        this.dependant = builder.dependant();
        this.artifactSnapshot = builder.artifactSnapshot();
    }

    /**
     * Enables code generation in the associated language.
     */
    @OverridingMethodsMustInvokeSuper
    void enableGeneration() {
        pluginTarget.applyJavaPlugin();
        dependant.compile(base().ofVersion(artifactSnapshot.spineBaseVersion()));
        dependant.compile(time().ofVersion(artifactSnapshot.spineTimeVersion()));
        if (codeGenJob != null) {
            pluginTarget.applyProtobufPlugin();
            protobufGenerator.enableBuiltIn(codeGenJob);
        }
    }

    /**
     * Disables code generation in the associated language.
     */
    @OverridingMethodsMustInvokeSuper
    void disableGeneration() {
        if (codeGenJob != null) {
            protobufGenerator.disableBuiltIn(codeGenJob);
        }
    }

    /**
     * Obtains the container of plugins associated with this extension.
     */
    final SpinePluginTarget pluginTarget() {
        return pluginTarget;
    }

    /**
     * Obtains the {@code ProtobufGenerator} which configures the code generation for the associated
     * project.
     */
    final ProtobufGenerator protobufGenerator() {
        return protobufGenerator;
    }

    /**
     * Obtains the dependency container associated with this extension.
     */
    final Dependant dependant() {
        return dependant;
    }

    /**
     * Forces the dependencies required by this extension.
     *
     * @see Extension#setForceDependencies(boolean)
     */
    final void forceDependencies() {
        forcedDependencies().forEach(dependant::force);
    }

    /**
     * Disables the dependencies enforcement.
     *
     * @see Extension#setForceDependencies(boolean)
     */
    final void disableDependencyEnforcement() {
        forcedDependencies().forEach(dependant::removeForcedDependency);
    }

    /**
     * Returns a set of dependencies whose versions should be forced.
     *
     * <p>Each set entry is a dependency spec, for example
     * {@code com.google.protobuf:protobuf-java:3.9.0}.
     *
     * <p>The implementors may override this method to specify the dependencies that are critical
     * for their work and should always be resolved to some particular versions.
     */
    protected ImmutableSet<String> forcedDependencies() {
        return ImmutableSet.of();
    }

    /**
     * An abstract builder for the {@code CodeGenExtension} subtypes.
     */
    abstract static class Builder<E extends CodeGenExtension, B extends Builder<E, B>> {

        private final @Nullable ProtocPlugin codeGenJob;

        private ProtobufGenerator protobufGenerator;
        private PluginTarget pluginTarget;
        private Dependant dependant;
        private Project project;
        private ArtifactSnapshot artifactSnapshot;

        Builder(@Nullable ProtocPlugin codeGenJob) {
            this.codeGenJob = codeGenJob;
        }

        Builder() {
            this(null);
        }

        private @Nullable ProtocPlugin codeGenJob() {
            return codeGenJob;
        }

        private ProtobufGenerator protobufGenerator() {
            return protobufGenerator;
        }

        B setProtobufGenerator(ProtobufGenerator protobufGenerator) {
            this.protobufGenerator = protobufGenerator;
            return self();
        }

        private PluginTarget pluginTarget() {
            return pluginTarget;
        }

        B setPluginTarget(PluginTarget pluginTarget) {
            this.pluginTarget = pluginTarget;
            return self();
        }

        private Dependant dependant() {
            return dependant;
        }

        B setDependant(Dependant dependant) {
            this.dependant = dependant;
            return self();
        }

        Project project() {
            return project;
        }

        B setProject(Project project) {
            this.project = project;
            return self();
        }

        ArtifactSnapshot artifactSnapshot() {
            return artifactSnapshot;
        }

        B setArtifactSnapshot(ArtifactSnapshot artifactSnapshot) {
            this.artifactSnapshot = artifactSnapshot;
            return self();
        }

        E build() {
            checkNotNull(protobufGenerator);
            checkNotNull(pluginTarget);
            checkNotNull(dependant);
            checkNotNull(artifactSnapshot);
            return doBuild();
        }

        /**
         * Obtains a reference to {@code this} which has the required compile-time type.
         */
        abstract B self();

        /**
         * Instantiates a new extension based on the data of this builder.
         *
         * <p>This method assumes that the data is valid and all the required fields are set.
         */
        abstract E doBuild();
    }
}
