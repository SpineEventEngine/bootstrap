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
import io.spine.logging.Logging;
import io.spine.tools.bootstrap.protobuf.ProtobufGenerator;
import io.spine.tools.bootstrap.protobuf.ProtocPlugin;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.bootstrap.SpineModule.base;

/**
 * A part of the {@link Extension spine} extension which configures certain code generation tasks.
 *
 * <p>One extension is responsible for one programming language to generate code in.
 */
abstract class CodeGenExtension implements Logging {

    private final ProtobufGenerator protobufGenerator;
    private final ProtocPlugin codeGenJob;
    private final PluginTarget pluginTarget;
    private final DependencyTarget dependencyTarget;
    private final String spineVersion;

    CodeGenExtension(Builder<?, ?> builder) {
        this.protobufGenerator = builder.getProtobufGenerator();
        this.codeGenJob = builder.getCodeGenJob();
        this.pluginTarget = builder.getPluginTarget();
        this.dependencyTarget = builder.getDependencyTarget();
        this.spineVersion = Ext.of(builder.getProject())
                               .versions()
                               .spine();
    }

    /**
     * Enables code generation in the associated language.
     */
    @OverridingMethodsMustInvokeSuper
    void enableGeneration() {
        pluginTarget.applyProtobufPlugin();
        protobufGenerator.enableBuiltIn(codeGenJob);
        dependencyTarget.compile(base.withVersion(spineVersion));
    }

    /**
     * Disables code generation in the associated language.
     */
    @OverridingMethodsMustInvokeSuper
    void disableGeneration() {
        protobufGenerator.disableBuiltIn(codeGenJob);
    }

    /**
     * Obtains the container of plugins associated with this extension.
     */
    final PluginTarget pluginTarget() {
        return pluginTarget;
    }

    /**
     * Obtains the dependency container associated with this extension.
     */
    final DependencyTarget dependencyTarget() {
        return dependencyTarget;
    }

    /**
     * Obtains the version of Spine framework used in this project.
     *
     * <p>This is also the version of the Bootstrap plugin itself.
     */
    final String spineVersion() {
        return spineVersion;
    }

    /**
     * Obtains the configurator of the Protobuf code generation.
     */
    final ProtobufGenerator protobufGenerator() {
        return protobufGenerator;
    }

    /**
     * An abstract builder for the {@code CodeGenExtension} subtypes.
     */
    abstract static class Builder<E extends CodeGenExtension, B extends Builder<E, B>> {

        private final ProtocPlugin codeGenJob;

        private ProtobufGenerator protobufGenerator;
        private PluginTarget pluginTarget;
        private DependencyTarget dependencyTarget;
        private Project project;

        Builder(ProtocPlugin codeGenJob) {
            this.codeGenJob = codeGenJob;
        }

        ProtobufGenerator getProtobufGenerator() {
            return protobufGenerator;
        }

        B setProtobufGenerator(ProtobufGenerator protobufGenerator) {
            this.protobufGenerator = protobufGenerator;
            return self();
        }

        ProtocPlugin getCodeGenJob() {
            return codeGenJob;
        }

        PluginTarget getPluginTarget() {
            return pluginTarget;
        }

        B setPluginTarget(PluginTarget pluginTarget) {
            this.pluginTarget = pluginTarget;
            return self();
        }

        DependencyTarget getDependencyTarget() {
            return dependencyTarget;
        }

        B setDependencyTarget(DependencyTarget dependencyTarget) {
            this.dependencyTarget = dependencyTarget;
            return self();
        }

        Project getProject() {
            return project;
        }

        B setProject(Project project) {
            this.project = project;
            return self();
        }

        E build() {
            checkNotNull(protobufGenerator);
            checkNotNull(codeGenJob);
            checkNotNull(pluginTarget);
            checkNotNull(dependencyTarget);
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
