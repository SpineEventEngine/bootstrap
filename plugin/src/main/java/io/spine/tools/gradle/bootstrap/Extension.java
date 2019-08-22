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
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.config.SpineDependency;
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
import static io.spine.tools.gradle.ConfigurationName.testImplementation;
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
public final class Extension implements ConfigurationSensitive {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in tests and with other meanings.
    static final String NAME = "spine";

    static final String COMPILE_JAVA = compileJava.value();
    private static final String COMPILE_TEST_JAVA = compileTestJava.value();

    private final JavaExtension java;
    private final JavaScriptExtension javaScript;
    private final ModelExtension modelExtension;
    private final ArtifactSnapshot artifacts;

    private final Project project;
    private boolean javaEnabled = false;

    private boolean forceConfiguration;

    private Extension(Builder builder) {
        this.java = builder.buildJavaExtension();
        this.javaScript = builder.buildJavaScriptExtension();
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
        Artifact testlib = SpineDependency.testlib().ofVersion(version());
        java.dependant().depend(testImplementation, testlib.notation());
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
     * Marks this project as a project that contains the Protobuf model definition.
     *
     * <p>Enables the {@code protobuf} and {@code java} plugins. Also adds the generated source
     * sets.
     */
    public void assembleModel() {
        this.modelExtension.enableGeneration();
    }

    /**
     * Returns {@code true} if configuration enforcement is enabled for the current project.
     *
     * <p>Enabled configuration enforcement means certain dependencies will be forced to resolve
     * to particular versions needed by the Spine Bootstrap plugin.
     */
    public boolean getForceConfiguration() {
        return forceConfiguration;
    }

    /**
     * Enables or disables the configuration enforcement for the current project.
     *
     * <p>A convenience wrapper for {@link #forceConfiguration()} and
     * {@link #disableConfigurationEnforcement()}.
     *
     * <p>In Gradle build script may be used as follows:
     * <pre>
     *     {@code
     *     spine {
     *         forceConfiguration = true
     *     }
     *     }
     * </pre>
     *
     */
    public void setForceConfiguration(boolean forceConfiguration) {
        this.forceConfiguration = forceConfiguration;
        if (forceConfiguration) {
            forceConfiguration();
        } else {
            disableConfigurationEnforcement();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Enforces a configuration needed for all child extensions.
     */
    @Override
    public void forceConfiguration() {
        java.forceConfiguration();
        javaScript.forceConfiguration();
        modelExtension.forceConfiguration();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Disables configuration enforcement for all child extensions.
     */
    @Override
    public void disableConfigurationEnforcement() {
        java.disableConfigurationEnforcement();
        javaScript.disableConfigurationEnforcement();
        modelExtension.disableConfigurationEnforcement();
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
        Task compileJava = tasks.findByPath(COMPILE_JAVA);
        Task compileTestJava = tasks.findByPath(COMPILE_TEST_JAVA);
        if (compileJava != null) {
            compileJava.setEnabled(enabled);
        }
        if (compileTestJava != null) {
            compileTestJava.setEnabled(enabled);
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
