/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.protobuf.gradle.ProtobufPlugin;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.mc.dart.gradle.McDartPlugin;
import io.spine.tools.mc.java.gradle.plugins.McJavaPlugin;
import io.spine.tools.mc.js.gradle.McJsPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link PluginTarget} which applies Spine Gradle plugins.
 *
 * <p>Provides convenience methods for the Model Compiler plugin, Proto JS plugin, and the Protobuf
 * Gradle plugin.
 */
public final class SpinePluginTarget implements PluginTarget {

    private final PluginTarget delegate;

    public SpinePluginTarget(PluginTarget delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public <P extends Plugin<Project>> void with(GradlePlugin<P> plugin, Consumer<P> action) {
        delegate.with(plugin, action);
    }

    @Override
    public void apply(GradlePlugin<?> plugin) {
        delegate.apply(plugin);
    }

    @Override
    public void apply(PluginScript pluginScript) {
        delegate.apply(pluginScript);
    }

    @Override
    public boolean isApplied(GradlePlugin<?> plugin) {
        return delegate.isApplied(plugin);
    }

    /**
     * Applies the standard {@link JavaPlugin}.
     */
    public void applyJavaPlugin() {
        GradlePlugin<?> javaPlugin = GradlePlugin.implementedIn(JavaPlugin.class);
        apply(javaPlugin);
    }

    /**
     * Applies the {@link ProtobufPlugin} and the {@link JavaPlugin}.
     *
     * <p>The Protobuf plugin requires the Java plugin. Thus, the Java plugin is applied first.
     */
    public void applyProtobufPlugin() {
        applyJavaPlugin();
        GradlePlugin<?> protoPlugin = GradlePlugin.implementedIn(ProtobufPlugin.class);
        apply(protoPlugin);
    }

    /**
     * Applies the {@link McJavaPlugin}.
     */
    public void applyMcJavaPlugin() {
        GradlePlugin<?> plugin = GradlePlugin.implementedIn(McJavaPlugin.class);
        apply(plugin);
    }

    /**
     * Applies the {@link McJsPlugin}.
     */
    public void applyMcJsPlugin() {
        GradlePlugin<?> plugin = GradlePlugin.implementedIn(McJsPlugin.class);
        apply(plugin);
    }

    /**
     * Applies the {@link McDartPlugin}.
     */
    public void applyMcDartPlugin() {
        GradlePlugin<?> plugin = GradlePlugin.implementedIn(McDartPlugin.class);
        apply(plugin);
    }
}
