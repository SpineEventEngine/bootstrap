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
import com.google.protobuf.gradle.GenerateProtoTask;
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import groovy.lang.Closure;
import io.spine.value.StringTypeValue;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginManager;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.groovy.ConsumerClosure.closure;

final class ProtobufGenerator {

    @VisibleForTesting
    static final String PROTOBUF_GRADLE_PLUGIN = "com.google.protobuf";

    private final Project project;

    ProtobufGenerator(Project project) {
        this.project = checkNotNull(project);
    }

    void enable(BuiltIn builtIn) {
        String name = builtIn.name();
        withProtobufPlugin(
                () -> configureTasks(task -> task.getBuiltins().maybeCreate(name))
        );
    }

    void disable(BuiltIn builtIn) {
        withProtobufPlugin(
                () -> configureTasks(task -> deleteBuiltIn(task, builtIn))
        );
    }

    private static void deleteBuiltIn(GenerateProtoTask task, BuiltIn builtIn) {
        String name = builtIn.name();
        task.getBuiltins()
            .removeIf(taskBuiltin -> name.equals(taskBuiltin.getName()));
    }

    private void configureTasks(Consumer<GenerateProtoTask> config) {
        Closure forEachTask = closure((GenerateProtoTaskCollection tasks) ->
                                              tasks.all()
                                                   .forEach(config));
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

    interface GenerationJob {

        String name();
    }

    static class PlugIn extends StringTypeValue implements GenerationJob {

        static final PlugIn gRPC = new PlugIn("grpc");

        private static final long serialVersionUID = 0L;

        PlugIn(String value) {
            super(value);

        }

        @Override
        public String name() {
            return value();
        }
    }

    enum BuiltIn implements GenerationJob {

        java,
        js
    }
}
