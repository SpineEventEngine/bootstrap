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
import com.google.protobuf.gradle.ProtobufConfigurator;
import com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection;
import com.google.protobuf.gradle.ProtobufConvention;
import io.spine.value.StringTypeValue;
import org.gradle.api.Project;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.groovy.ConsumerClosure.closure;

final class ProtobufGenerator {

    private final Project project;

    ProtobufGenerator(Project project) {
        this.project = checkNotNull(project);
    }

    void enable(BuiltIn builtIn) {
        String name = builtIn.name();
        configureTasks(task -> task.getBuiltins().maybeCreate(name));
    }

    void disable(BuiltIn builtIn) {
        String name = builtIn.name();
        configureTasks(task -> task.getBuiltins()
                                   .removeIf(
                                           taskBuiltin -> name.equals(taskBuiltin.getName())
                                   )
        );
    }

    private void configureTasks(Consumer<GenerateProtoTask> config) {
        configure(protobuf -> protobuf.generateProtoTasks(
                closure(
                        (GenerateProtoTaskCollection tasks) ->
                                tasks.all()
                                     .forEach(
                                             config
                                     )
                ))
        );
    }

    private void configure(Consumer<ProtobufConfigurator> config) {
        project.getConvention()
               .getPlugin(ProtobufConvention.class)
               .protobuf(closure(config::accept));
    }

    interface GenerationJob {

        String name();
    }

    static class PlugIn extends StringTypeValue implements GenerationJob {

        static final PlugIn gRPC = new PlugIn("grpc");
        static final PlugIn spineProtoc = new PlugIn("spineProtoc");

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
