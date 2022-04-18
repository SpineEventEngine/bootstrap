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

package io.spine.tools.gradle.protoc;

import com.google.protobuf.gradle.ExecutableLocator;
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.GenerateProtoTask.PluginOptions;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import groovy.lang.Closure;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ProtobufDependencies.gradlePlugin;
import static io.spine.tools.gradle.project.Projects.getProtobufConvention;
import static io.spine.tools.groovy.ConsumerClosure.closure;

/**
 * A facade for Protobuf plugin configuration.
 *
 * <p>Configures the {@code protoc} built-ins and plugins to be used for code generation.
 */
public final class ProtobufGenerator {

    private final Project project;

    public ProtobufGenerator(Project project) {
        this.project = checkNotNull(project);
    }

    /**
     * Sets the state of the specified protoc plugin.
     *
     * @param plugin
     *         the plugin to turn on or off
     * @param enabled
     *         the desired state of the plugin
     */
    public void switchPlugin(ProtocPlugin plugin, boolean enabled) {
        if (enabled) {
            enablePlugin(plugin);
        } else {
            disablePlugin(plugin);
        }
    }

    /**
     * Sets the state of the code generation of the given protoc built-in.
     *
     * @param builtIn
     *         the built-in to configure
     * @param enabled
     *         the desired state of the code generation
     */
    public void switchBuiltIn(ProtocPlugin builtIn, boolean enabled) {
        if (enabled) {
            enablePlugin(builtIn);
        } else {
            disableBuiltIn(builtIn);
        }
    }

    /**
     * Enables code generation with the given {@code protoc} built-in.
     */
    public void enableBuiltIn(ProtocPlugin builtIn) {
        enableIn(builtIn, GenerateProtoTask::getBuiltins);
    }

    /**
     * Enables code generation with the given {@code protoc} built-in.
     */
    public void enablePlugin(ProtocPlugin builtIn) {
        enableIn(builtIn, GenerateProtoTask::getPlugins);
    }

    private void enableIn(ProtocPlugin plugin, ContainerSelector selector) {
        withProtobufPlugin(() -> configureTasks(task -> {
            var plugins = selector.apply(task);
            plugin.createIn(plugins);
        }));
    }

    /**
     * Disables code generation with the given {@code protoc} built-in.
     */
    public void disableBuiltIn(ProtocPlugin builtIn) {
        disableIn(builtIn, GenerateProtoTask::getBuiltins);
    }

    /**
     * Disables code generation with the given {@code protoc} built-in.
     */
    public void disablePlugin(ProtocPlugin builtIn) {
        disableIn(builtIn, GenerateProtoTask::getPlugins);
    }

    private void disableIn(ProtocPlugin plugin, ContainerSelector selector) {
        withProtobufPlugin(() -> configureTasks(task -> {
            var plugins = selector.apply(task);
            plugin.removeFrom(plugins);
        }));
    }

    /**
     * Specifies the Protobuf compiler to use to generate code.
     *
     * @param artifactSpec
     *         Protobuf compiler artifact spec
     */
    public void useCompiler(String artifactSpec) {
        checkNotNull(artifactSpec);
        withProtobufPlugin(() -> configurator().protoc(closure(
                (ExecutableLocator locator) -> locator.setArtifact(artifactSpec))
        ));
    }

    private void configureTasks(Consumer<GenerateProtoTask> config) {
        Closure<?> forEachTask = closure(
                (GenerateProtoTaskCollection tasks) -> tasks.all()
                                                            .forEach(config)
        );
        configurator().generateProtoTasks(forEachTask);
    }

    private ProtobufConfigurator configurator() {
        var protobuf = getProtobufConvention(project).getProtobuf();
        return protobuf;
    }

    private void withProtobufPlugin(Runnable action) {
        var pluginManager = project.getPluginManager();
        if (pluginManager.hasPlugin(gradlePlugin.id)) {
            action.run();
        } else {
            pluginManager.withPlugin(gradlePlugin.id, plugin -> action.run());
        }
    }

    private interface ContainerSelector
            extends Function<GenerateProtoTask, NamedDomainObjectContainer<PluginOptions>> {
    }
}
