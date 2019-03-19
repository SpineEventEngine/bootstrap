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
import org.gradle.api.Project;

import static io.spine.tools.bootstrap.SpineModule.base;

/**
 * A part of the {@link Extension spine} extension which configures certain code generation tasks.
 *
 * <p>One extension is responsible for one programming language to generate code in.
 */
abstract class CodeGenExtension implements Logging {

    private final ProtobufGenerator protobufGenerator;
    private final ProtobufGenerator.ProtocBuiltIn codeGenJob;
    private final PluginTarget pluginTarget;
    private final DependencyTarget dependencyTarget;
    private final String spineVersion;

    CodeGenExtension(ProtobufGenerator protobufGenerator,
                     ProtobufGenerator.ProtocBuiltIn job,
                     PluginTarget pluginTarget,
                     DependencyTarget dependencyTarget,
                     Project project) {
        this.protobufGenerator = protobufGenerator;
        this.codeGenJob = job;
        this.pluginTarget = pluginTarget;
        this.dependencyTarget = dependencyTarget;
        this.spineVersion = Ext.of(project)
                               .versions()
                               .spine();
    }

    /**
     * Enables code generation in the associated language.
     */
    @OverridingMethodsMustInvokeSuper
    void enableGeneration() {
        pluginTarget.applyProtobufPlugin();
        protobufGenerator.enable(codeGenJob);
        dependencyTarget.compile(base.withVersion(spineVersion));
    }

    /**
     * Disables code generation in the associated language.
     */
    @OverridingMethodsMustInvokeSuper
    void disableGeneration() {
        protobufGenerator.disable(codeGenJob);
    }

    final PluginTarget pluginTarget() {
        return pluginTarget;
    }

    final DependencyTarget dependencyTarget() {
        return dependencyTarget;
    }

    final String spineVersion() {
        return spineVersion;
    }
}
