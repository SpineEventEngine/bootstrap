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

package io.spine.tools.gradle;

import com.google.protobuf.gradle.ProtobufPlugin;
import io.spine.js.gradle.ProtoJsPlugin;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import org.gradle.api.plugins.JavaPlugin;

/**
 * A target of Gradle plugin application.
 *
 * <p>Typically, represented by a Gradle {@link org.gradle.api.Project}.
 */
public interface PluginTarget {

    /**
     * Applies the given plugin.
     */
    void apply(GradlePlugin plugin);

    /**
     * Checks if the given plugin is already applied.
     */
    boolean isApplied(GradlePlugin plugin);

    void apply(PluginScript pluginScript);

    /**
     * Checks if the given plugin is not applied yet.
     */
    default boolean isNotApplied(GradlePlugin plugin) {
        return !isApplied(plugin);
    }

    /**
     * Applies the {@link ProtobufPlugin} and the {@link JavaPlugin}.
     *
     * <p>The Protobuf plugin requires the Java plugin. Thus, the Java plugin is applied first.
     */
    default void applyProtobufPlugin() {
        GradlePlugin javaPlugin = GradlePlugin.implementedIn(JavaPlugin.class);
        apply(javaPlugin);
        GradlePlugin protoPlugin = GradlePlugin.implementedIn(ProtobufPlugin.class);
        apply(protoPlugin);
    }

    /**
     * Applies the {@link ModelCompilerPlugin}.
     */
    default void applyModelCompiler() {
        GradlePlugin plugin = GradlePlugin.implementedIn(ModelCompilerPlugin.class);
        apply(plugin);
    }

    /**
     * Applies the {@link ProtoJsPlugin}.
     */
    default void applyProtoJsPlugin() {
        GradlePlugin plugin = GradlePlugin.implementedIn(ProtoJsPlugin.class);
        apply(plugin);
    }
}
