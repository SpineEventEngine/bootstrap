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
import io.spine.tools.gradle.project.DependencyTarget;
import io.spine.tools.gradle.project.DirectoryStructure;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.protoc.ProtobufGenerator;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
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

    private Extension(Builder builder) {
        this.java = builder.buildJavaExtension();
        this.javaScript = builder.buildJavaScriptExtension();
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
     * is not applied to this project, applies it immediately.
     */
    @CanIgnoreReturnValue
    public JavaExtension enableJava() {
        java.enableGeneration();
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
        return javaScript;
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
        private DirectoryStructure layout;
        private DependencyTarget dependencyTarget;

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

        Builder setLayout(DirectoryStructure layout) {
            this.layout = checkNotNull(layout);
            return this;
        }

        Builder setDependencyTarget(DependencyTarget dependencyTarget) {
            this.dependencyTarget = checkNotNull(dependencyTarget);
            return this;
        }

        private JavaExtension buildJavaExtension() {
            JavaExtension javaExtension = JavaExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependencyTarget(dependencyTarget)
                    .setPluginTarget(pluginTarget)
                    .setProtobufGenerator(generator)
                    .setDirectoryStructure(layout)
                    .build();
            return javaExtension;
        }

        private JavaScriptExtension buildJavaScriptExtension() {
            JavaScriptExtension javaScriptExtension = JavaScriptExtension
                    .newBuilder()
                    .setProject(project)
                    .setDependencyTarget(dependencyTarget)
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
