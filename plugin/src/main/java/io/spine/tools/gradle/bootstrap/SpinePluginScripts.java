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

import io.spine.io.Resource;
import io.spine.tools.gradle.PluginScript;

/**
 * A factory of Spine-specific {@link PluginScript}s.
 *
 * <p>These scripts are read from the plugin resources at runtime. When building the plugin,
 * the script files are copied from the {@code config} submodule.
 */
final class SpinePluginScripts {

    /**
     * Prevents the utility class instantiation.
     */
    private SpinePluginScripts() {
    }

    /**
     * Obtains the {@code model-compiler.gradle} script.
     *
     * <p>The script configures the {@link io.spine.tools.gradle.compiler.ModelCompilerPlugin} to
     * the recommended settings.
     */
    static PluginScript modelCompilerConfig() {
        return PluginScript.declaredIn(Name.MODEL_COMPILER.resourceFile());
    }

    /**
     * The names of the existing plugin scripts.
     */
    enum Name {

        MODEL_COMPILER("model-compiler");

        private static final String SCRIPT_EXTENSION = ".gradle";

        private final String name;

        Name(String name) {
            this.name = name;
        }

        private Resource resourceFile() {
            String resourceName = name + SCRIPT_EXTENSION;
            return Resource.file(resourceName);
        }
    }
}
