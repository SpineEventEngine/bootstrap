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

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.gradle.util.ConfigureUtil.configure;

/**
 * The {@code spine} Gradle DSL extension.
 *
 * <p>Configures the project as a {@linkplain #java() Java} or/and a {@linkplain #javaScript()
 * JavaScript} project based on Spine.
 */
public final class Extension {

    static final String NAME = "spine";

    private final JavaExtension java;
    private final JavaScriptExtension javaScript;

    private Extension(JavaExtension java, JavaScriptExtension javaScript) {
        this.java = java;
        this.javaScript = javaScript;
    }

    /**
     * Creates a new instance of {@code Extension} for the given project.
     */
    static Extension newInstance(Project project, PluginTarget pluginTarget, CodeLayout layout, DependencyTarget dependencyTarget) {
        checkNotNull(project);
        checkNotNull(pluginTarget);

        ProtobufGenerator generator = new ProtobufGenerator(project);
        Extension extension = new Extension(new JavaExtension(project, generator, pluginTarget, layout, dependencyTarget),
                                            new JavaScriptExtension(generator, pluginTarget));
        return extension;
    }

    /**
     * Marks this project as a Java project and configures the Java code generation.
     *
     * @param configuration
     *         Groovy style configuration
     * @see #java()
     */
    public void java(Closure configuration) {
        checkNotNull(configuration);
        java();
        configure(configuration, java);
    }

    /**
     * Marks this project as a Java project and configures the Java code generation.
     *
     * @param configuration
     *         Java/Kotlin style configuration
     * @see #java()
     */
    public void java(Action<JavaExtension> configuration) {
        checkNotNull(configuration);
        java();
        configuration.execute(java);
    }

    /**
     * Marks this project as a Java project and configures the Java code generation.
     *
     * <p>Enables the Java code generation from Protobuf. If the {@code spine-model-compiler} plugin
     * is not applied to this project, applies it immediately.
     */
    public void java() {
        java.enableGeneration();
    }

    /**
     * Marks this project as a JavaScript project and configures the JavaScript code generation.
     *
     * <p>Enables the JS code generation from Protobuf. If the {@code spine-proto-js-plugin} is
     * not applied to this project, applies it immediately.
     */
    public void javaScript() {
        javaScript.enableGeneration();
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
}
