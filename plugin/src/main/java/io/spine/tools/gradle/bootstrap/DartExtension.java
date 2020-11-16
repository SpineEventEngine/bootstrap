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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import io.spine.dart.gradle.Extension;
import io.spine.dart.PubCache;
import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.protoc.ProtocPlugin;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskContainer;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.nio.file.Path;

import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.bootstrap.DartTaskName.generateDart;
import static io.spine.tools.gradle.bootstrap.DartTaskName.generateTestDart;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.dart;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS;

/**
 * An extension which configures Dart code generation.
 */
@VisibleForTesting // Would be package private, but needed for integration tests.
public final class DartExtension extends CodeGenExtension {

    public static final String TYPES_FILE = "types.dart";
    private static final String DART_TOOL_NAME = "dart_code_gen";
    private static final Joiner commandJoiner = Joiner.on(' ');

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
        project.afterEvaluate((p) -> {
            mainTask.dependsOn(generateProto.name());
            testTask.dependsOn(generateTestProto.name());
        });
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
        Task task = tasks.create(name.name());
        task.doLast(t -> runDartTool(descriptorFile, dartDir));
        return task;
    }

    private void runDartTool(Property<Object> descriptorFile, DirectoryProperty dartDir) {
        if (project.file(descriptorFile)
                   .exists()) {
            @SuppressWarnings("UseOfProcessBuilder")
            ProcessBuilder processBuilder = buildDartToolProcess(descriptorFile, dartDir);
            int exitCode;
            try {
                Process dartToolProcess = processBuilder.start();
                exitCode = dartToolProcess.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new GradleException(format("Failed to execute `%s`.", DART_TOOL_NAME), e);
            }
            if (exitCode != 0) {
                throw onProcessError(processBuilder, exitCode);
            }
        }
    }

    private ProcessBuilder buildDartToolProcess(Property<Object> descriptorFile,
                                                DirectoryProperty dartDir) {
        Path command = dartCodeGenCommand();
        @SuppressWarnings("UseOfProcessBuilder")
        ProcessBuilder processBuilder = new ProcessBuilder(
                command.toString(),
                "--descriptor", project.file(descriptorFile)
                                       .getAbsolutePath(),
                "--destination", dartDir.file(TYPES_FILE)
                                        .get()
                                        .getAsFile()
                                        .getAbsolutePath(),
                "--standard-types", "spine_client",
                "--import-prefix", "."
        ).inheritIO();
        return processBuilder;
    }

    private static GradleException onProcessError(
            @SuppressWarnings("UseOfProcessBuilder") ProcessBuilder processBuilder,
            int exitCode
    ) {
        String command = commandJoiner.join(processBuilder.command());
        throw new GradleException(format("Command `%s` exited with code %s.", command, exitCode));
    }

    private Path dartCodeGenCommand() {
        String extension = Os.isFamily(FAMILY_WINDOWS) ? ".bat" : "";
        Path command = PubCache.bin()
                               .resolve(DART_TOOL_NAME + extension);
        if (!exists(command)) {
            _warn().log("Cannot locate `dart_code_gen` under `%s`. " +
                                "To install, run `pub global activate %s`.",
                        command, DART_TOOL_NAME);
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
