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

import io.spine.tools.gradle.Artifact;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME;

/**
 * A container of dependencies of a certain project.
 *
 * <p>Typically, represented by a {@link org.gradle.api.artifacts.dsl.DependencyHandler} of
 * the project.
 */
public interface DependencyTarget {

    /**
     * Adds a new dependency within a given configuration.
     *
     * @param configurationName
     *         the name of the Gradle configuration
     * @param notation
     *         the dependency string, e.g. {@code "io.spine:spine-base:1.0.0"}
     */
    void depend(String configurationName, String notation);

    /**
     * Adds a new dependency within the {@code compile} configuration.
     *
     * @see #compile(String)
     */
    default void compile(Artifact artifact) {
        compile(artifact.notation());
    }

    /**
     * Adds a new dependency within the {@code compile} configuration.
     *
     * <p>Though {@code compile} configuration is deprecated in Gradle, it is still used in order to
     * define Protobuf dependencies without re-generating the Java/JS sources from the upstream
     * Protobuf definitions.
     *
     * @see #depend(String, String)
     */
    default void compile(String notation) {
        @SuppressWarnings("deprecation")
        // Required in order to add Protobuf dependencies.
        // See issue https://github.com/google/protobuf-gradle-plugin/issues/242.
        String configurationName = COMPILE_CONFIGURATION_NAME;
        depend(configurationName, notation);
    }

    /**
     * Adds a new dependency within the {@code implementation} configuration.
     *
     * @see #depend(String, String)
     */
    default void implementation(String notation) {
        depend(IMPLEMENTATION_CONFIGURATION_NAME, notation);
    }
}
