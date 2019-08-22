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

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.IterableSubject;
import com.google.protobuf.gradle.ProtobufPlugin;
import io.spine.js.gradle.ProtoJsPlugin;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.bootstrap.given.FakeArtifacts;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.testing.MemoizingDependant;
import io.spine.tools.gradle.testing.MemoizingPluginRegistry;
import io.spine.tools.gradle.testing.MemoizingSourceSuperset;
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
import static io.spine.tools.gradle.ProtobufDependencies.protobufLite;
import static io.spine.tools.gradle.bootstrap.JavaExtension.ForcedDependency.PROTOBUF_JAVA;
import static io.spine.tools.gradle.bootstrap.given.FakeArtifacts.GRPC_PROTO_DEPENDENCY;
import static io.spine.tools.gradle.bootstrap.given.FakeArtifacts.GRPC_STUB_DEPENDENCY;
import static io.spine.tools.gradle.bootstrap.given.FakeArtifacts.spineVersion;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("`spine` extension should")
class ExtensionTest {

    private PluginTarget pluginTarget;
    private Extension extension;
    private MemoizingSourceSuperset codeLayout;
    private MemoizingDependant dependencyTarget;
    private Path projectDir;

    @BeforeEach
    void setUp(@TempDir Path projectDir) {
        Project project = ProjectBuilder
                .builder()
                .withName(BootstrapPluginTest.class.getSimpleName())
                .withProjectDir(projectDir.toFile())
                .build();
        this.projectDir = project.getProjectDir().toPath();
        pluginTarget = new MemoizingPluginRegistry();
        dependencyTarget = new MemoizingDependant();
        codeLayout = new MemoizingSourceSuperset();
        extension = Extension
                .newBuilder()
                .setProject(project)
                .setLayout(codeLayout)
                .setPluginTarget(pluginTarget)
                .setDependencyTarget(dependencyTarget)
                .setArtifactSnapshot(FakeArtifacts.snapshot())
                .build();
        project.getExtensions()
               .add(ModelCompilerPlugin.extensionName(),
                    new io.spine.tools.gradle.compiler.Extension());
    }

    @Nested
    @DisplayName("if project type is specified")
    class ApplyPlugins {

        @Test
        @DisplayName("apply Model Compiler plugin to a Java project")
        void applyModelCompiler() {
            extension.enableJava();

            assertApplied(ModelCompilerPlugin.class);
            assertNotApplied(ProtoJsPlugin.class);
        }

        @Test
        @DisplayName("apply `java` plugin to a Java project")
        void applyJava() {
            extension.enableJava();

            assertApplied(JavaPlugin.class);
        }

        @Test
        @DisplayName("add `testlib` dependency to a Java project")
        void addTestlibDependecy() {
            extension.enableJava();
            assertThat(dependencyTarget.dependencies())
                    .contains(testlibDependency());
        }

        @Test
        @DisplayName("apply `com.google.protobuf` plugin to a Java project")
        void applyProtoForJava() {
            extension.enableJava();

            assertApplied(ProtobufPlugin.class);
        }

        @Test
        @DisplayName("not apply `enableJava` if already present")
        void notApplyJavaIfJavaLib() {
            pluginTarget.apply(GradlePlugin.implementedIn(JavaPlugin.class));

            assertApplied(JavaPlugin.class);

            extension.enableJava();
        }

        @Test
        @DisplayName("apply Proto JS plugin to a JS project")
        void applyProtoJs() {
            extension.enableJavaScript();

            assertApplied(ProtoJsPlugin.class);
            assertNotApplied(ModelCompilerPlugin.class);
        }

        @Test
        @DisplayName("not add a `testlib` dependency to a JS project")
        void noTestLibForJs() {
            extension.enableJavaScript();

            assertThat(dependencyTarget.dependencies())
                    .doesNotContain(testlibDependency());
        }

        @Test
        @DisplayName("apply `com.google.protobuf` plugin to a JS project")
        void applyProtoForJs() {
            extension.enableJavaScript();

            assertApplied(ProtobufPlugin.class);
        }

        @Test
        @DisplayName("apply both Model Compiler and Proto JS plugin to a complex project")
        void combine() {
            extension.enableJavaScript();
            extension.enableJava();

            assertApplied(JavaPlugin.class);
            assertApplied(ProtoJsPlugin.class);
            assertApplied(ModelCompilerPlugin.class);
        }

        @Test
        @DisplayName("add server dependencies if required")
        void server() {
            extension.enableJava().server();

            assertApplied(JavaPlugin.class);
            assertThat(dependencyTarget.dependencies()).contains(serverDependency());
        }

        @Test
        @DisplayName("not contain `testutil-server` for enableJava() declaring modules")
        void notContainTestUtil() {
            extension.enableJava();

            assertThat(dependencyTarget.dependencies())
                    .doesNotContain(testUtilServerDependency());
        }

        @Test
        @DisplayName("add `testutil-server` dependency for enableJava().server() declaring modules")
        void testUtilDependencyAdded() {
            extension.enableJava().server();

            assertThat(dependencyTarget.dependencies())
                    .contains(testUtilServerDependency());
        }

        @Test
        @DisplayName("add client dependencies if required")
        void client() {
            extension.enableJava().client();

            IterableSubject assertDependencies = assertThat(dependencyTarget.dependencies());
            assertDependencies.contains(clientDependency());
            assertDependencies.doesNotContain(serverDependency());
        }

        @Test
        @DisplayName("add `testutil-server` dependencies together with server dependencies")
        void testUtilServer() {
            extension.enableJava()
                     .client();

            IterableSubject assertDependencies = assertThat(dependencyTarget.dependencies());
            assertDependencies.contains(testUtilClientDependency());
            assertDependencies.doesNotContain(testUtilServerDependency());
        }

        @Test
        @DisplayName("exclude Protobuf Lite dependencies for Java projects")
        void noExclusions() {
            assertThat(dependencyTarget.exclusions()).isEmpty();
            extension.enableJava();
            assertThat(dependencyTarget.exclusions()).containsExactly(protobufLite());
        }

        @Test
        @DisplayName("declare `generated` directory a source root")
        void declareGeneratedDirectory() {
            extension.enableJava();

            assertApplied(JavaPlugin.class);
            ImmutableSet<Path> declaredPaths = codeLayout.javaSourceDirs();
            assertThat(declaredPaths).contains(projectDir.resolve("generated"));
        }

        @Test
        @DisplayName("declare gRPC dependencies when codegen is required")
        void grpcDeps() {
            extension.enableJava()
                     .getCodegen()
                     .setGrpc(true);

            assertApplied(JavaPlugin.class);
            assertThat(dependencyTarget.dependencies())
                    .containsAtLeast(GRPC_PROTO_DEPENDENCY, GRPC_STUB_DEPENDENCY);
        }

        /** Applying {@code Java} plugin is necessary to apply the {@code protobuf} plugin. */
        @Test
        @DisplayName("apply Java plugin to projects that contain only the model definition")
        void javaPluginToModelProjects() {
            extension.assembleModel();
            assertApplied(JavaPlugin.class);
        }

        @Test
        @DisplayName("NOT apply Protobuf plugin to projects that contain only the model definition")
        void protobufPluginToModelProjects() {
            extension.assembleModel();
            assertNotNull(ProtobufPlugin.class);
        }

        @Test
        @DisplayName("disable Java code generation in Java projects")
        void disableCodegen() {
            JavaExtension javaExtension = ExtensionTest.this.extension.enableJava();
            JavaCodegenExtension codegen = javaExtension.getCodegen();
            assertTrue(codegen.getProtobuf());
            codegen.setProtobuf(false);
            assertFalse(codegen.getProtobuf());
        }

        private String serverDependency() {
            return "io.spine:spine-server:" + spineVersion;
        }

        private String testUtilServerDependency() {
            return "io.spine:spine-testutil-server:" + spineVersion;
        }

        private String clientDependency() {
            return "io.spine:spine-client:" + spineVersion;
        }

        private String testUtilClientDependency() {
            return "io.spine:spine-testutil-client:" + spineVersion;
        }

        private String testlibDependency() {
            return "io.spine:spine-testlib:" + spineVersion;
        }

        private void assertApplied(Class<? extends Plugin<? extends Project>> pluginClass) {
            GradlePlugin plugin = GradlePlugin.implementedIn(pluginClass);
            assertTrue(pluginTarget.isApplied(plugin),
                       format("Plugin %s must be applied.", plugin));
        }

        private void assertNotApplied(Class<? extends Plugin<? extends Project>> pluginClass) {
            GradlePlugin plugin = GradlePlugin.implementedIn(pluginClass);
            assertFalse(pluginTarget.isApplied(plugin),
                        format("Plugin %s must NOT be applied.", plugin));
        }
    }

    @Nested
    @DisplayName("allow to configure")
    class Configuration {

        private static final String WITH_AN_ACTION = "with an action";
        private static final String WITH_A_CLOSURE = "with a closure";

        @Test
        @DisplayName("gRPC codegen")
        void grpc() {
            JavaCodegenExtension codegen = extension.enableJava()
                                                    .getCodegen();
            assertFalse(codegen.getGrpc());
            codegen.setGrpc(true);
            assertTrue(codegen.getGrpc());
            assertThat(dependencyTarget.dependencies())
                    .containsAtLeast(GRPC_PROTO_DEPENDENCY, GRPC_STUB_DEPENDENCY);

            codegen.setGrpc(false);
            assertFalse(codegen.getGrpc());
        }

        @Test
        @DisplayName("Protobuf to Java codegen")
        void protobufJava() {
            JavaCodegenExtension codegen = extension.enableJava()
                                                    .getCodegen();
            assertTrue(codegen.getProtobuf());
            codegen.setProtobuf(false);
            assertFalse(codegen.getProtobuf());

            codegen.setProtobuf(true);
            assertTrue(codegen.getProtobuf());
        }

        @Nested
        @DisplayName("Java")
        class Java {

            @Test
            @DisplayName(WITH_AN_ACTION)
            void action() {
                AtomicBoolean executedAction = new AtomicBoolean(false);
                extension.enableJava(javaExtension -> {
                    JavaCodegenExtension codegen = javaExtension.getCodegen();
                    boolean defaultValue = codegen.getGrpc();
                    assertThat(defaultValue).isFalse();

                    codegen.setGrpc(true);

                    boolean newValue = codegen.getGrpc();
                    assertThat(newValue).isTrue();

                    executedAction.set(true);
                });
                assertTrue(executedAction.get());
            }

            @Test
            @DisplayName(WITH_A_CLOSURE)
            void closure() {
                AtomicBoolean executedClosure = new AtomicBoolean(false);
                extension.enableJava(ConsumerClosure.<JavaExtension>closure(javaExtension -> {
                    JavaCodegenExtension codegen = javaExtension.getCodegen();
                    boolean defaultValue = codegen.getGrpc();
                    assertThat(defaultValue).isFalse();

                    codegen.setGrpc(true);

                    boolean newValue = codegen.getGrpc();
                    assertThat(newValue).isTrue();

                    executedClosure.set(true);
                }));
                assertTrue(executedClosure.get());
            }
        }

        @Nested
        @DisplayName("Java codegen")
        class CodegenJava {

            @Test
            @DisplayName(WITH_AN_ACTION)
            void action() {
                AtomicBoolean executedAction = new AtomicBoolean(false);
                JavaExtension javaExtension = extension.enableJava();
                javaExtension.codegen(codegen -> {
                    boolean defaultValue = codegen.getSpine();
                    assertThat(defaultValue).isTrue();

                    codegen.setSpine(false);

                    boolean newValue = codegen.getSpine();
                    assertThat(newValue).isFalse();

                    executedAction.set(true);
                });
                assertTrue(executedAction.get());
            }

            @Test
            @DisplayName(WITH_A_CLOSURE)
            void closure() {
                AtomicBoolean executedClosure = new AtomicBoolean(false);
                extension.enableJava()
                         .codegen(ConsumerClosure.<JavaCodegenExtension>closure(
                                 codegen -> {
                                     boolean defaultValue = codegen.getSpine();
                                     assertThat(defaultValue).isTrue();

                                     codegen.setSpine(false);

                                     boolean newValue = codegen.getSpine();
                                     assertThat(newValue).isFalse();

                                     executedClosure.set(true);
                                 }));
                assertTrue(executedClosure.get());
            }
        }
    }

    @Test
    @DisplayName("force configuration to resolve particular versions of needed dependencies")
    void forceDependencies() {
        extension.setForceConfiguration(true);

        assertThat(dependencyTarget.forcedDependencies())
                .containsExactly(PROTOBUF_JAVA.dependency(), PROTOBUF_JAVA.version());
    }

    @Test
    @DisplayName("disable previously enabled configuration enforcing")
    void disableDependencyEnforcing() {
        dependencyTarget.force(PROTOBUF_JAVA.dependency(), PROTOBUF_JAVA.version());
        extension.setForceConfiguration(false);

        assertThat(dependencyTarget.forcedDependencies())
                .isEmpty();
    }

    @Test
    @DisplayName("expose whether configuration enforcing is enabled")
    void exposeWhetherConfigurationForced() {
        assertThat(extension.getForceConfiguration()).isFalse();
        extension.setForceConfiguration(true);
        assertThat(extension.getForceConfiguration()).isTrue();
    }
}
