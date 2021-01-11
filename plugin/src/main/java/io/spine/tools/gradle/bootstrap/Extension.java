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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import groovy.lang.Closure;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.project.Dependant;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.project.SourceSuperset;
import io.spine.tools.gradle.protoc.ProtobufGenerator;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskContainer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.groovy.ConsumerClosure.closure;
import static org.gradle.util.ConfigureUtil.configure;

/**
 * The {@code spine} Gradle DSL extension.
 *
 * <p>Configures the project as a {@linkplain #enableJava() Java} or/and
 * a {@linkplain #enableJavaScript() JavaScript} project based on Spine.
 */
public final class Extension {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in tests and with other meanings.
    static final String NAME = "spine";

    private final JavaExtension java;
    private final JavaScriptExtension javaScript;
    private final DartExtension dart;
    private final ModelExtension modelExtension;
    private final ArtifactSnapshot artifacts;
    private final Project project;
    private boolean javaEnabled;
    private boolean forceDependencies;

    private Extension(Builder builder) {
        this.java = builder.buildJavaExtension();
        this.javaScript = builder.buildJavaScriptExtension();
        this.dart = builder.buildDartExtension();
        this.modelExtension = builder.buildModelExtension();
        this.project = builder.project;
        this.artifacts = builder.artifacts;
    }

    /**
     * Obtains the version of the framework.
     *
     * <p>In a Gradle plugin, reference {@code spine.version()} in order to obtain the current Spine
     * version.
     *
     * @return the currently used version of Spine as a string
     */
    public String version() {
        return artifacts.spineVersion();
    }

    /**
     * Marks this project as a Java project and configures the Java code generation.
     *
     * @param configuration
     *         Groovy style configuration
     * @see #enableJava()
     */
    public void enableJava(Closure configuration) {
        checkNotNull(configuration);
        enableJava();
        configure(configuration, java);
    }

    /**
     * Marks this project as a Java project and configures the Java code generation.
     *
     * @param configuration
     *         Java/Kotlin style configuration
     * @see #enableJava()
     */
    public void enableJava(Action<JavaExtension> configuration) {
        checkNotNull(configuration);
        enableJava();
        configuration.execute(java);
    }

    /**
     * Marks this project as a Java project and configures the Java code generation.
     *
     * <p>Enables the Java code generation from Protobuf. If the {@code spine-model-compiler} plugin
     * is not applied to this project, applies it immediately. Also adds the
     * {@code io.spine:spine-testlib}
     */
    @CanIgnoreReturnValue
    public JavaExtension enableJava() {
        java.enableGeneration();
        toggleJavaTasks(true);
        disableTransitiveProtos();
        return java;
    }

    /**
     * Marks this project as a JavaScript project and configures the JavaScript code generation.
     *
     * <p>Enables the JS code generation from Protobuf. If the {@code spine-proto-js-plugin} is
     * not applied to this project, applies it immediately.
     */
    @CanIgnoreReturnValue
    public JavaScriptExtension enableJavaScript() {
        javaScript.enableGeneration();
        if (!this.javaEnabled) {
            toggleJavaTasks(false);
        }
        disableTransitiveProtos();
        return javaScript;
    }

    /**
     * Marks this project as a Dart project and configures the Dart code generation.
     *
     * <p>Enables the Dart code generation from Protobuf. If the {@code spine-proto-dart-plugin} is
     * not applied to this project, applies it immediately.
     */
    @CanIgnoreReturnValue
    public DartExtension enableDart() {
        dart.enableGeneration();
        if (!this.javaEnabled) {
            toggleJavaTasks(false);
        }
        disableTransitiveProtos();
        return dart;
    }

    /**
     * Marks this project as a project that contains the Protobuf model definition.
     *
     * <p>Enables the {@code protobuf} and {@code java} plugins. Also adds the generated source
     * sets.
     */
    public void assembleModel() {
        this.modelExtension.enableGeneration();
    }

    /**
     * Returns {@code true} if the dependency enforcement is enabled for the current project.
     *
     * <p>If the option is enabled, certain dependencies will be forced to resolve to the versions
     * needed by the Spine Bootstrap plugin.
     */
    public boolean getForceDependencies() {
        return forceDependencies;
    }

    /**
     * Enables or disables the dependency enforcement for the current project.
     *
     * <p>In Spine Bootstrap plugin, for some elements, it's necessary to have the particular
     * dependency no lower than version {@code X} in the project.
     *
     * <p>Set this field to {@code true} to ensure the "correct" dependency version is used
     * regardless of project environment.
     *
     * <p>In Gradle build script may be used as follows:
     * <pre>
     *     {@code
     *     spine {
     *         forceDependencies = true
     *     }
     *     }
     * </pre>
     */
    public void setForceDependencies(boolean forceDependencies) {
        this.forceDependencies = forceDependencies;
        if (forceDependencies) {
            forceDependencies();
        } else {
            disableDependencyEnforcement();
        }
    }

    /**
     * Enforces the dependency configuration needed for all child extensions.
     *
     * @see #setForceDependencies(boolean)
     */
    private void forceDependencies() {
        java.forceDependencies();
        javaScript.forceDependencies();
        modelExtension.forceDependencies();
    }

    /**
     * Disables dependency enforcement for all child extensions.
     *
     * @see #setForceDependencies(boolean)
     */
    private void disableDependencyEnforcement() {
        java.disableDependencyEnforcement();
        javaScript.disableDependencyEnforcement();
        modelExtension.disableDependencyEnforcement();
    }

    /**
     * Disables the Java code generation.
     *
     * <p>By default, Protobuf Gradle plugin enables the Java codegen. However, it is not required
     * in some cases. Thus, disable the Java codegen before the configuration start and re-enable it
     * if required.
     */
    void disableJavaGeneration() {
        java.disableGeneration();
        toggleJavaTasks(false);
    }

    private void toggleJavaTasks(boolean enabled) {
        this.javaEnabled = enabled;
        toggleCompileJavaTasks(enabled);
    }

    /**
     * If the {@code protobuf} configuration is present, disables its transitibity.
     *
     * <p>Disabling transitivity leads to exclusion of {@code spine} and
     * {@code com.google.protobuf} dependencies.
     */
    private void disableTransitiveProtos() {
        project.configurations(closure((ConfigurationContainer container) -> {
            Configuration protobuf = container.findByName(ConfigurationName.protobuf.name());
            if (protobuf != null) {
                protobuf.setTransitive(false);
            }
        }));
    }

    /**
     * Attempts to find and change the {@code enabled} flag of
     * {@code compileJava} and {@code compileTestJava} tasks in the current project.
     *
     * <p>If such tasks could not be found in the project, performs no action.
     */
    private void toggleCompileJavaTasks(boolean enabled) {
        TaskContainer tasks = project.getTasks();
        Task compileJavaTask = tasks.findByPath(compileJava.name());
        Task compileTestJavaTask = tasks.findByPath(compileTestJava.name());
        if (compileJavaTask != null) {
            compileJavaTask.setEnabled(enabled);
        }
        if (compileTestJavaTask != null) {
            compileTestJavaTask.setEnabled(enabled);
        }
    }

    /**
     * Creates a new instance of {@code Builder} for {@code Extension} instances.
     *
     * @return new instance of {@code Builder}
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code Extension} instances.
     */
    static final class Builder {

        private Project project;
        private ProtobufGenerator generator;
        private PluginTarget pluginTarget;
        private SourceSuperset layout;
        private Dependant dependencyTarget;
        private ArtifactSnapshot artifacts;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        Builder setProject(Project project) {
            this.project = checkNotNull(project);
            this.generator = new ProtobufGenerator(project);
            return this;
        }

        Builder setPluginTarget(PluginTarget pluginTarget) {
            this.pluginTarget = checkNotNull(pluginTarget);
            return this;
        }

        Builder setLayout(SourceSuperset layout) {
            this.layout = checkNotNull(layout);
            return this;
        }

        Builder setDependencyTarget(Dependant dependencyTarget) {
            this.dependencyTarget = checkNotNull(dependencyTarget);
            return this;
        }

        Builder setArtifactSnapshot(ArtifactSnapshot artifacts) {
            this.artifacts = checkNotNull(artifacts);
            return this;
        }

        private JavaExtension buildJavaExtension() {
            JavaExtension javaExtension = JavaExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setSourceSuperset(layout)
                    .setArtifactSnapshot(artifacts)
                    .build();
            return javaExtension;
        }

        private JavaScriptExtension buildJavaScriptExtension() {
            JavaScriptExtension javaScriptExtension = JavaScriptExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setArtifactSnapshot(artifacts)
                    .build();
            return javaScriptExtension;
        }

        private DartExtension buildDartExtension() {
            DartExtension dartExtension = DartExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setArtifactSnapshot(artifacts)
                    .build();
            return dartExtension;
        }

        private ModelExtension buildModelExtension() {
            ModelExtension modelExtension = ModelExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setSourceSuperset(layout)
                    .setArtifactSnapshot(artifacts)
                    .build();
            return modelExtension;
        }

        /**
         * Creates a new instance of {@code Extension}.
         *
         * @return new instance of {@code Extension}
         */
        public Extension build() {
            checkNotNull(project);
            checkNotNull(generator);
            checkNotNull(pluginTarget);
            checkNotNull(layout);
            checkNotNull(dependencyTarget);
            checkNotNull(artifacts);

            return new Extension(this);
        }
    }
}
