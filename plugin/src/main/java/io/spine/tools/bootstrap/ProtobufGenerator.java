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

import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.GenerateProtoTask.PluginOptions;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import groovy.lang.Closure;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginManager;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.groovy.ConsumerClosure.closure;

/**
 * A facade for Protobuf plugin configuration.
 *
 * <p>Configures the {@code protoc} built-ins and plugins to be used for code generation.
 */
final class ProtobufGenerator {

    /**
     * Identifier of the {@link com.google.protobuf.gradle.ProtobufPlugin}.
     */
    private static final String PROTOBUF_GRADLE_PLUGIN = "com.google.protobuf";

    private final Project project;

    ProtobufGenerator(Project project) {
        this.project = checkNotNull(project);
    }

    /**
     * Enables code generation with the given {@code protoc} built-in.
     */
    void enable(ProtocBuiltIn builtIn) {
        withProtobufPlugin(
                () -> configureTasks(
                        task -> builtIn.createIn(task.getBuiltins())
                )
        );
    }

    /**
     * Disables code generation with the given {@code protoc} built-in.
     */
    void disable(ProtocBuiltIn builtIn) {
        withProtobufPlugin(
                () -> configureTasks(task -> deleteBuiltIn(task, builtIn))
        );
    }

    private static void deleteBuiltIn(GenerateProtoTask task, ProtocBuiltIn builtIn) {
        builtIn.removeFrom(task.getBuiltins());
    }

    private void configureTasks(Consumer<GenerateProtoTask> config) {
        Closure forEachTask = closure(
                (GenerateProtoTaskCollection tasks) -> tasks.all()
                                                            .forEach(config)
        );
        protobufConfigurator().generateProtoTasks(forEachTask);
    }

    private ProtobufConfigurator protobufConfigurator() {
        ProtobufConfigurator protobuf = project.getConvention()
                                               .getPlugin(ProtobufConvention.class)
                                               .getProtobuf();
        return protobuf;
    }

    private void withProtobufPlugin(Runnable action) {
        PluginManager pluginManager = project.getPluginManager();
        if (pluginManager.hasPlugin(PROTOBUF_GRADLE_PLUGIN)) {
            action.run();
        } else {
            pluginManager.withPlugin(PROTOBUF_GRADLE_PLUGIN, plugin -> action.run());
        }
    }

    /**
     * Protobuf compiler built-in which can be configured with the Spine plugin.
     *
     * <p>The names of the enum instances should be used as the names of the built-ins.
     */
    static final class ProtocBuiltIn {

        private final Name name;
        private final @Nullable String option;

        static ProtocBuiltIn called(Name name) {
            checkNotNull(name);
            return new ProtocBuiltIn(name, null);
        }

        static ProtocBuiltIn withOption(Name name, String option) {
            checkNotNull(name);
            checkNotNull(option);
            return new ProtocBuiltIn(name, option);
        }

        private ProtocBuiltIn(Name name, @Nullable String option) {
            this.name = name;
            this.option = option;
        }

        private void createIn(NamedDomainObjectContainer<PluginOptions> builtIns) {
            checkNotNull(builtIns);
            PluginOptions options = builtIns.maybeCreate(name.name());
            if (option != null) {
                options.option(option);
            }
        }

        private void removeFrom(NamedDomainObjectContainer<PluginOptions> builtIns) {
            String name = this.name.name();
            builtIns.removeIf(taskBuiltIn -> name.equals(taskBuiltIn.getName()));
        }

        enum Name {
            java,
            js
        }
    }
}
