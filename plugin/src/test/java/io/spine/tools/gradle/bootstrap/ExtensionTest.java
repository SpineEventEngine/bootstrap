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

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Correspondence;
import com.google.common.truth.IterableSubject;
import com.google.protobuf.gradle.ProtobufPlugin;
import io.spine.dart.gradle.ProtoDartPlugin;
import io.spine.js.gradle.ProtoJsPlugin;
import io.spine.testing.TempDir;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.bootstrap.given.FakeArtifacts;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import io.spine.tools.gradle.project.PlugableProject;
import io.spine.tools.gradle.project.PluginTarget;
import io.spine.tools.gradle.testing.MemoizingDependant;
import io.spine.tools.gradle.testing.MemoizingSourceSuperset;
import io.spine.tools.groovy.ConsumerClosure;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.ProtobufDependencies.protobufLite;
import static io.spine.tools.gradle.bootstrap.given.FakeArtifacts.GRPC_PROTO_DEPENDENCY;
import static io.spine.tools.gradle.bootstrap.given.FakeArtifacts.GRPC_STUB_DEPENDENCY;
import static io.spine.tools.gradle.bootstrap.given.FakeArtifacts.spineVersion;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`spine` extension should")
class ExtensionTest {

    /**
     * A {@link Correspondence} of a Gradle {@link Task} to its {@linkplain TaskName name}.
     *
     * <p>Allow to assert facts about a collection of tasks referencing them by names instead of
     * looking up individual elements.
     */
    private static final
    Correspondence<@NonNull Task, @NonNull TaskName> names = Correspondence.from(
            (@NonNull Task task, @NonNull TaskName name) -> task.getName().equals(name.name()),
            "has name"
    );

    private PluginTarget pluginTarget;
    private Extension extension;
    private MemoizingSourceSuperset codeLayout;
    private MemoizingDependant dependencyTarget;
    private Path projectDir;
    private Project project;

    @BeforeEach
    void setUp() {
        this.projectDir = TempDir.forClass(ExtensionTest.class).toPath();
        this.project = ProjectBuilder
                .builder()
                .withName(BootstrapPluginTest.class.getSimpleName())
                .withProjectDir(projectDir.toFile())
                .build();
        pluginTarget = new PlugableProject(project);
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
    }

    @Nested
    @DisplayName("if project type is specified")
    class ApplyPlugins {

        @Test
        @DisplayName("add `base` dependency to a Java project")
        void addBaseDependencyToJava() {
            extension.enableJava();
            assertThat(dependencyTarget.dependencies())
                    .contains(baseDependency());
        }

        @Test
        @DisplayName("add `base` dependency to a JS project")
        void addBaseDependencyToJs() {
            extension.enableJavaScript();
            assertThat(dependencyTarget.dependencies())
                    .contains(baseDependency());
        }

        @Test
        @DisplayName("add `base` dependency to a model-only project")
        void addBaseDependencyToModel() {
            extension.assembleModel();
            assertThat(dependencyTarget.dependencies())
                    .contains(baseDependency());
        }

        @Test
        @DisplayName("add `time` dependency to a Java project")
        void addTimeDependencyToJava() {
            extension.enableJava();
            assertThat(dependencyTarget.dependencies())
                    .contains(timeDependency());
        }

        @Test
        @DisplayName("add `time` dependency to a JS project")
        void addTimeDependencyToJs() {
            extension.enableJavaScript();
            assertThat(dependencyTarget.dependencies())
                    .contains(timeDependency());
        }

        @Test
        @DisplayName("add `time` dependency to a model-only project")
        void addTimeDependencyToModel() {
            extension.assembleModel();
            assertThat(dependencyTarget.dependencies())
                    .contains(timeDependency());
        }

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
        @DisplayName("add `testutil-time` dependency to a Java project")
        void addTestUtilTimeDependecy() {
            extension.enableJava();
            assertThat(dependencyTarget.dependencies())
                    .contains(testUtilTimeDependency());
        }

        @Test
        @DisplayName("add `web` dependencies to a Java project")
        void addWebDependency() {
            extension.enableJava(JavaExtension::webServer);
            assertThat(dependencyTarget.dependencies())
                    .contains(webDependency());
        }

        @Test
        @DisplayName("add `firebase-web` dependencies to a Java project")
        void addFirebaseWebDependency() {
            extension.enableJava(JavaExtension::firebaseWebServer);
            assertThat(dependencyTarget.dependencies())
                    .contains(firebaseWebDependency());
        }

        @Test
        @DisplayName("add `gcloud` dependency to a Java project")
        void addGCloudDependency() {
            extension.enableJava(JavaExtension::withDatastore);
            IterableSubject assertDependencies = assertThat(dependencyTarget.dependencies());
            assertDependencies
                    .contains(datastoreDependency());
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
        @DisplayName("not add a `testutil-time` dependency to a JS project")
        void noTestUtilTimeForJs() {
            extension.enableJavaScript();

            assertThat(dependencyTarget.dependencies())
                    .doesNotContain(testUtilTimeDependency());
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
            extension.enableJava()
                     .server();

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
            extension.enableJava()
                     .server();

            assertThat(dependencyTarget.dependencies())
                    .contains(testUtilServerDependency());
        }

        @Test
        @DisplayName("add client dependencies if required")
        void client() {
            extension.enableJava()
                     .client();

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

        @Test
        @DisplayName("apply Proto Dart plugin to a Dart project")
        void applyProtoDart() {
            DartExtension dartExtension = extension.enableDart();
            assertThat(dartExtension)
                    .isNotNull();
            assertApplied(ProtoDartPlugin.class);
        }

        @Test
        @DisplayName("apply Protobuf plugin to a Dart project")
        void applyProtobufToDart() {
            extension.enableDart();
            assertApplied(ProtobufPlugin.class);
        }

        private String baseDependency() {
            return "io.spine:spine-base:" + spineVersion;
        }

        private String timeDependency() {
            return "io.spine:spine-time:" + spineVersion;
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

        private String testUtilTimeDependency() {
            return "io.spine:spine-testutil-time:" + spineVersion;
        }

        private String webDependency() {
            return "io.spine:spine-web:" + spineVersion;
        }

        private String firebaseWebDependency() {
            return "io.spine.gcloud:spine-firebase-web:" + spineVersion;
        }

        private String datastoreDependency() {
            return "io.spine.gcloud:spine-datastore:" + spineVersion;
        }

        private void assertApplied(Class<? extends Plugin<? extends Project>> pluginClass) {
            GradlePlugin<?> plugin = GradlePlugin.implementedIn(pluginClass);
            assertTrue(pluginTarget.isApplied(plugin),
                       format("Plugin %s must be applied.", plugin));
        }

        private void assertNotApplied(Class<? extends Plugin<? extends Project>> pluginClass) {
            GradlePlugin<?> plugin = GradlePlugin.implementedIn(pluginClass);
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
    @DisplayName("force configuration to resolve particular versions of required dependencies")
    void forceDependencies() {
        JavaExtension javaExtension = extension.enableJava();
        this.extension.setForceDependencies(true);

        String dependencySpec = javaExtension.protobufJavaSpec();
        assertThat(dependencyTarget.forcedDependencies())
                .containsExactly(dependencySpec);
    }

    @Test
    @DisplayName("disable previously enabled configuration enforcement")
    void disableDependencyEnforcing() {
        JavaExtension javaExtension = extension.enableJava();
        String dependencySpec = javaExtension.protobufJavaSpec();
        dependencyTarget.force(dependencySpec);

        extension.setForceDependencies(false);

        assertThat(dependencyTarget.forcedDependencies())
                .isEmpty();
    }

    @Test
    @DisplayName("expose whether configuration enforcement is enabled")
    void exposeWhetherConfigurationForced() {
        assertThat(extension.getForceDependencies()).isFalse();
        extension.setForceDependencies(true);
        assertThat(extension.getForceDependencies()).isTrue();
    }

    @Test
    @DisplayName("add `generateDart` tasks if needed")
    void addDartTasks() {
        extension.enableDart();
        TaskContainer tasks = project.getTasks();
        assertThat(tasks)
                .comparingElementsUsing(names)
                .containsAtLeastElementsIn(DartTaskName.values());
    }
}
