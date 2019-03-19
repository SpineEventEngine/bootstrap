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

import com.google.protobuf.gradle.ProtobufPlugin;
import io.spine.js.gradle.ProtoJsPlugin;
import io.spine.tools.bootstrap.given.TestCodeLayout;
import io.spine.tools.bootstrap.given.TestDependencyTarget;
import io.spine.tools.bootstrap.given.TestPluginRegistry;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import io.spine.tools.groovy.ConsumerClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.bootstrap.given.ExtensionTextEnv.addExt;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("`spine` extension should")
class ExtensionTest {

    private PluginTarget pluginTarget;
    private Extension extension;

    @BeforeEach
    void setUp(@TempDir Path projectDir) {
        Project project = ProjectBuilder
                .builder()
                .withName(BootstrapPluginTest.class.getSimpleName())
                .withProjectDir(projectDir.toFile())
                .build();
        addExt(project);
        pluginTarget = new TestPluginRegistry();
        extension = Extension.newInstance(project, pluginTarget, new TestCodeLayout(), new TestDependencyTarget());
    }

    @Nested
    @DisplayName("if project type is specified")
    class ApplyPlugins {

        @Test
        @DisplayName("apply Model Compiler plugin to a Java project")
        void applyModelCompiler() {
            extension.java();

            assertApplied(ModelCompilerPlugin.class);
            assertNotApplied(ProtoJsPlugin.class);
        }

        @Test
        @DisplayName("apply `java` plugin to a Java project")
        void applyJava() {
            extension.java();

            assertApplied(JavaPlugin.class);
        }

        @Test
        @DisplayName("apply `com.google.protobuf` plugin to a Java project")
        void applyProtoForJava() {
            extension.java();

            assertApplied(ProtobufPlugin.class);
        }

        @Test
        @DisplayName("not apply `java` if already present")
        void notApplyJavaIfJavaLib() {
            pluginTarget.apply(GradlePlugin.implementedIn(JavaPlugin.class));

            assertApplied(JavaPlugin.class);

            extension.java();
        }

        @Test
        @DisplayName("apply Proto JS plugin to a JS project")
        void applyProtoJs() {
            extension.javaScript();

            assertApplied(ProtoJsPlugin.class);
            assertNotApplied(ModelCompilerPlugin.class);
        }

        @Test
        @DisplayName("apply `com.google.protobuf` plugin to a JS project")
        void applyProtoForJs() {
            extension.javaScript();

            assertApplied(ProtobufPlugin.class);
        }

        @Test
        @DisplayName("apply both Model Compiler and Proto JS plugin to a complex project")
        void combine() {
            extension.javaScript();
            extension.java();

            assertApplied(JavaPlugin.class);
            assertApplied(ProtoJsPlugin.class);
            assertApplied(ModelCompilerPlugin.class);
        }

        private void assertApplied(Class<? extends Plugin<? extends Project>> pluginClass) {
            GradlePlugin plugin = GradlePlugin.implementedIn(pluginClass);
            assertTrue(pluginTarget.isApplied(plugin),
                       format("Plugin %s must be applied.", plugin.className()));
        }

        private void assertNotApplied(Class<? extends Plugin<? extends Project>> pluginClass) {
            GradlePlugin plugin = GradlePlugin.implementedIn(pluginClass);
            assertFalse(pluginTarget.isApplied(plugin),
                       format("Plugin %s must NOT be applied.", plugin.className()));
        }
    }

    @Nested
    @DisplayName("allow to configure")
    class Configuration {

        @Nested
        @DisplayName("gRPC code gen for Java")
        class GrpcJava {

            @Test
            @DisplayName("with an action")
            void action() {
                AtomicBoolean executedAction = new AtomicBoolean(false);
                extension.java(javaExtension -> {
                    boolean defaultValue = javaExtension.getGrpc();
                    assertThat(defaultValue).isFalse();

                    javaExtension.setGrpc(true);

                    boolean newValue = javaExtension.getGrpc();
                    assertThat(newValue).isTrue();

                    executedAction.set(true);
                });
                assertTrue(executedAction.get());
            }

            @Test
            @DisplayName("with a closure")
            void closure() {
                AtomicBoolean executedClosure = new AtomicBoolean(false);
                extension.java(ConsumerClosure.<JavaExtension>closure(javaExtension -> {
                    boolean defaultValue = javaExtension.getGrpc();
                    assertThat(defaultValue).isFalse();

                    javaExtension.setGrpc(true);

                    boolean newValue = javaExtension.getGrpc();
                    assertThat(newValue).isTrue();

                    executedClosure.set(true);
                }));
                assertTrue(executedClosure.get());
            }
        }
    }
}
