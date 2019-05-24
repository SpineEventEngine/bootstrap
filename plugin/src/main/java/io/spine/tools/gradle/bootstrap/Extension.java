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

package io.spine.tools.gradle.bootstrap;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import groovy.lang.Closure;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.ProtocConfigurationPlugin;
import io.spine.tools.gradle.config.SpineDependency;
import io.spine.tools.gradle.project.Dependant;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.project.SourceSuperset;
import io.spine.tools.gradle.protoc.ProtobufGenerator;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.tasks.TaskContainer;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.TEST_IMPLEMENTATION;
import static io.spine.tools.gradle.TaskName.compileJava;
import static io.spine.tools.gradle.TaskName.compileTestJava;
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

    static final String COMPILE_JAVA = compileJava.value();
    private static final String COMPILE_TEST_JAVA = compileTestJava.value();

    private final ModelExtension model;
    private final JavaExtension java;
    private final JavaScriptExtension javaScript;
    private final Project project;
    private boolean javaEnabled = false;

    private Extension(Builder builder) {
        this.java = builder.buildJavaExtension();
        this.javaScript = builder.buildJavaScriptExtension();
        this.model = builder.buildModelExtension();
        this.project = builder.project;
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
        String spineVersion = java.spineVersion();
        Artifact testlib = SpineDependency.testlib().ofVersion(spineVersion);
        java.dependant().depend(TEST_IMPLEMENTATION, testlib.notation());
        toggleJavaTasks(true);
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
        return javaScript;
    }

    /** Marks this project as the one that contains Protobuf model description. */
    public void assembleModel() {
        model.enableGeneration();
        GradlePlugin plugin = GradlePlugin.implementedIn(ProtocConfigurationPlugin.class);
        model.pluginTarget().apply(plugin);
        this.model.addSourceSets();
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

    private void toggleJavaTasks(boolean shouldEnable) {
        this.javaEnabled = shouldEnable;
        toggleCompileJavaTasks(shouldEnable);
        toggleTransitiveProtos(shouldEnable);
    }

    /**
     * If the {@code protobuf} configuration is present, toggles the transitivity according to the
     * specified parameter.
     *
     * <p>Disabling transitivity leads to exclusion of {@code spine} and
     * {@code com.google.protobuf} dependencies.
     *
     * @param shouldEnable
     *         whether the transitivity should be enabled
     */
    private void toggleTransitiveProtos(boolean shouldEnable) {
        project.configurations(closure((ConfigurationContainer container) -> {
            try {
                @SuppressWarnings("DuplicateStringLiteralInspection" /* Different context. */)
                Configuration protobuf = container.getByName("protobuf");
                protobuf.setTransitive(shouldEnable);
            } catch (UnknownConfigurationException ignored) {
                /*
                 * If the configuration could not be found then transitive dependencies can be
                 * considered disabled.
                 */
            }
        }));
    }

    /**
     * Attempts to find and change the {@code enabled} flag of
     * {@code compileJava} and {@code compileTestJava} tasks in the current project.
     *
     * <p>If such tasks could not be found in the project, performs no action.
     */
    private void toggleCompileJavaTasks(boolean shouldEnable) {
        TaskContainer tasks = project.getTasks();
        Optional.ofNullable(tasks.findByPath(COMPILE_JAVA))
                .ifPresent(task -> task.setEnabled(shouldEnable));
        Optional.ofNullable(tasks.findByPath(COMPILE_TEST_JAVA))
                .ifPresent(task -> task.setEnabled(shouldEnable));
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

        private JavaExtension buildJavaExtension() {
            JavaExtension javaExtension = JavaExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setSourceSuperset(layout)
                    .build();
            return javaExtension;
        }

        private ModelExtension buildModelExtension() {
            ModelExtension modelExtension = ModelExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setSourceSuperset(layout)
                    .build();
            return modelExtension;
        }

        private JavaScriptExtension buildJavaScriptExtension() {
            JavaScriptExtension javaScriptExtension = JavaScriptExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependant(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .build();
            return javaScriptExtension;
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

            return new Extension(this);
        }
    }
}
