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

import com.google.common.annotations.VisibleForTesting;
import io.spine.js.gradle.ProtoJsPlugin;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;

public final class Extension {

    static final String NAME = "spine";

    private static final Class<? extends Plugin<Project>> SPINE_JAVA_PLUGIN =
            ModelCompilerPlugin.class;

    private static final Class<? extends Plugin<Project>> SPINE_JS_PLUGIN =
            ProtoJsPlugin.class;

    private final Project project;

    Extension(Project project) {
        this.project = project;
    }

    public void javaProject() {
        applyPlugin(SPINE_JAVA_PLUGIN);
    }

    public void jsProject() {
        applyPlugin(SPINE_JS_PLUGIN);
    }

    private void applyPlugin(Class<? extends Plugin<Project>> pluginClass) {
        PluginContainer pluginContainer = project.getPlugins();
        if (!pluginContainer.hasPlugin(pluginClass)) {
            PluginManager pluginManager = project.getPluginManager();
            pluginManager.apply(pluginClass);
        }
    }

    @VisibleForTesting
    Project project() {
        return project;
    }
}
