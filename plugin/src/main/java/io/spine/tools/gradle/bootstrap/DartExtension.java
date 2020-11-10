/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.generate.dart.Extension;
import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.config.PubCache;
import io.spine.tools.gradle.protoc.ProtocPlugin;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskContainer;

import java.nio.file.Path;

import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.gradle.bootstrap.DartTaskName.generateDart;
import static io.spine.tools.gradle.bootstrap.DartTaskName.generateTestDart;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.dart;
import static java.nio.file.Files.exists;
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS;

/**
 * An extension which configures JavaScript code generation.
 */
final class DartExtension extends CodeGenExtension {

    private final Project project;

    private DartExtension(Builder builder) {
        super(builder);
        this.project = builder.project();
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    void enableGeneration() {
        super.enableGeneration();
        pluginTarget().applyProtoDartPlugin();
        Extension protoDart = project.getExtensions()
                                     .getByType(Extension.class);
        Task mainTask = createTask(generateDart,
                                   protoDart.getMainDescriptorSet(),
                                   protoDart.getLibDir());
        Task testTask = createTask(generateTestDart,
                                   protoDart.getTestDescriptorSet(),
                                   protoDart.getTestDir());
        Task assembleTask = project.getTasks()
                                   .getByName(assemble.name());
        assembleTask.dependsOn(mainTask, testTask);
        testTask.shouldRunAfter(mainTask);
    }

    private Task createTask(TaskName name,
                            Property<Object> descriptorFile,
                            DirectoryProperty dartDir) {
        TaskContainer tasks = project.getTasks();
        Task foundTask = tasks.findByName(name.name());
        if (foundTask != null) {
            return foundTask;
        }
        Exec task = tasks.create(name.name(), Exec.class);
        task.getInputs()
            .file(descriptorFile);
        project.afterEvaluate(p -> {
            Path command = dartCodeGenCommand();
            task.commandLine(
                    command,
                    "--descriptor", p.file(descriptorFile)
                                     .getAbsolutePath(),
                    "--destination", dartDir.file("types.dart")
                                            .get()
                                            .getAsFile()
                                            .getAbsolutePath(),
                    "--standard-types", "spine_client",
                    "--import_prefix", "."
            );
        });
        return task;
    }

    private Path dartCodeGenCommand() {
        String extension = Os.isFamily(FAMILY_WINDOWS) ? ".bat" : "";
        Path command = PubCache.location().resolve("dart_code_gen" + extension);
        if (exists(command)) {
            _warn().log("Cannot locate `dart_code_gen` under `%s`. " +
                                "To install, run `pub global activate dart_code_gen`.",
                        command);
        }
        return command;
    }

    /**
     * Creates a new instance of {@code Builder} for {@code DartExtension} instances.
     *
     * @return new instance of {@code Builder}
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code DartExtension} instances.
     */
    static final class Builder extends CodeGenExtension.Builder<DartExtension, Builder> {

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
            super(ProtocPlugin.called(dart));
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        DartExtension doBuild() {
            return new DartExtension(this);
        }
    }
}
