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
import com.google.protobuf.gradle.ProtobufPlugin;
import groovy.lang.Closure;
import io.spine.js.gradle.ProtoJsPlugin;
import io.spine.logging.Logging;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.gradle.util.ConfigureUtil.configure;

public final class Extension {

    static final String NAME = "spine";

    private static final Logger log = Logging.get(Extension.class);

    private final Project project;

    private @MonotonicNonNull JavaExtension java;
    private @MonotonicNonNull JavaScriptExtension javaScript;

    Extension(Project project) {
        this.project = project;
    }

    public void java(Closure configuration) {
        checkNotNull(configuration);
        java();
        configure(configuration, java);
    }

    public void java(Action<JavaExtension> configuration) {
        checkNotNull(configuration);
        java();
        configuration.execute(java);
    }

    public void java() {
        applyProtobufPlugin();
        applyPlugin(ModelCompilerPlugin.class);

        if (java == null) {
            java = new JavaExtension();
        }
    }

    public void javaScript(Closure configuration) {
        checkNotNull(configuration);
        javaScript();
        configure(configuration, javaScript);
    }

    public void javaScript(Action<JavaScriptExtension> configuration) {
        checkNotNull(configuration);
        javaScript();
        configuration.execute(javaScript);
    }

    public void javaScript() {
        applyProtobufPlugin();
        applyPlugin(ProtoJsPlugin.class);

        if (javaScript == null) {
            javaScript = new JavaScriptExtension();
        }
    }

    private void applyProtobufPlugin() {
        applyPlugin(JavaPlugin.class);
        applyPlugin(ProtobufPlugin.class);
    }

    private void applyPlugin(Class<? extends Plugin<? extends Project>> pluginClass) {
        PluginContainer pluginContainer = project.getPlugins();
        if (!pluginContainer.hasPlugin(pluginClass)) {
            PluginManager pluginManager = project.getPluginManager();
            pluginManager.apply(pluginClass);
        } else {
            log.debug("Plugin {} is already applied.", pluginClass.getCanonicalName());
        }
    }

    @VisibleForTesting
    Project project() {
        return project;
    }
}
