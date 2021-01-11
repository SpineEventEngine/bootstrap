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

package io.spine.tools.gradle.bootstrap.func;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.IterableSubject;
import io.spine.code.proto.FileDescriptors;
import io.spine.testing.SlowTest;
import io.spine.testing.TempDir;
import io.spine.tools.gradle.testing.GradleProject;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.BaseTaskName.build;
import static io.spine.tools.gradle.ProtoJsTaskName.generateJsonParsers;
import static io.spine.tools.gradle.bootstrap.DartExtension.TYPES_FILE;
import static java.nio.file.Files.exists;
import static java.util.Collections.emptySet;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SlowTest
@DisplayName("`io.spine.tools.gradle.bootstrap` plugin should")
class SpineBootstrapPluginTest {

    private static final String ADDITIONAL_CONFIG_SCRIPT = "config.gradle";
    private static final String TRANSITIVE_JS_DEPENDENCY = "any_pb.js";

    private GradleProject.Builder project;
    private Path projectDir;

    @BeforeEach
    void setUp() {
        this.projectDir = TempDir.forClass(SpineBootstrapPluginTest.class).toPath();
        projectDir.toFile().deleteOnExit();
        this.project = GradleProject
                .newBuilder()
                .setProjectName("func-test")
                .setProjectFolder(projectDir.toFile())
                .withPluginClasspath()
                .addProtoFile("roller_coaster.proto");
    }

    @Test
    @DisplayName("be applied to a project successfully")
    void apply() {
        noAdditionalConfig();
        project.build()
               .executeTask(build);
    }

    @Test
    @DisplayName("generate no code if none requested")
    void generateNothing() {
        noAdditionalConfig();
        project.build()
               .executeTask(build);
        Path compiledClasses = compiledJavaClasses();
        if (exists(compiledClasses)) {
            File compiledClassesDirectory = compiledClasses.toFile();
            assertThat(compiledClassesDirectory.list()).isEmpty();
        }
    }

    @Test
    @DisplayName("generate Java if requested")
    void generateJava() {
        configureJavaGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> packageContents = generatedClassFileNames();
        IterableSubject assertPackageContents = assertThat(packageContents);
        assertPackageContents.containsAtLeast("LunaParkProto.class",
                                              "RollerCoaster.class",
                                              "Wagon.class",
                                              "Altitude.class");
    }

    @Test
    @DisplayName("apply 'spine-model-compiler' plugin, generating descriptor set files")
    void applyModelCompiler() {
        configureJavaGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> resourceFiles = assembledResources();
        String projectDir = this.projectDir.getFileName()
                                           .toString();
        boolean containsDescriptorSetFile =
                resourceFiles.stream()
                             .filter(f -> f.endsWith(FileDescriptors.DESC_EXTENSION))
                             .anyMatch(f -> f.contains(projectDir));
        assertThat(containsDescriptorSetFile)
                .isTrue();
        assertThat(resourceFiles)
                .contains("desc.ref");
    }

    @Test
    @DisplayName("generate JavaScript if requested")
    void generateJs() {
        configureJsGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).contains("roller_coaster_pb.js");
    }

    @Test
    @DisplayName("generate Dart if requested")
    void generateDart() {
        configureDartGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> dartFileNames = generatedDartFileNames();
        String protoName = "roller_coaster";
        assertThat(dartFileNames)
                .containsExactly(
                        TYPES_FILE,
                        protoName + ".pb.dart",
                        protoName + ".pbjson.dart",
                        protoName + ".pbenum.dart",
                        protoName + ".pbserver.dart"
                );
    }

    @Test
    @DisplayName("generate an `index.js` file")
    void generateIndexJs() {
        configureJsGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).contains("index.js");
    }

    @Test
    @DisplayName("not generate transitive Spine dependencies for pure JS projects")
    void skipTransitiveProtos() {
        configureJsGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).doesNotContain(TRANSITIVE_JS_DEPENDENCY);
    }

    @Test
    @DisplayName("not generate transitive Spine dependencies for mixed projects")
    void skipTransitiveProtosForMixed() {
        configureJavaAndJs();
        GradleProject project = this.project.build();
        project.executeTask(build);
        assertThat(generatedJsFileNames()).doesNotContain(TRANSITIVE_JS_DEPENDENCY);
        assertThat(generatedClassFileNames()).doesNotContain("Any.class");
    }

    @Test
    @DisplayName("apply 'spine-proto-js-plugin'")
    void applyJsPlugin() {
        configureJsGeneration();
        GradleProject project = this.project.build();
        BuildResult result = project.executeTask(build);

        assertThat(result.task(generateJsonParsers.path())
                         .getOutcome()).isEqualTo(SUCCESS);
    }

    @Test
    @DisplayName("add client dependencies to the project")
    void clientDeps() {
        configureJavaClient();
        GradleProject project = this.project.build();
        project.executeTask(build);
        assertThat(generatedClassFileNames())
                .contains("ReceivedQuery.class");
    }

    @Test
    @DisplayName("add server dependencies to the project")
    void serverDeps() {
        configureJavaServer();
        GradleProject project = this.project.build();
        project.executeTask(build);
        assertThat(generatedClassFileNames())
                .contains("Nonevent.class");
    }

    @Test
    @DisplayName("generate gRPC stubs if required")
    void generateGrpc() {
        configureGrpc();
        GradleProject project = this.project.build();
        project.executeTask(build);
        assertThat(generatedClassFileNames())
                .containsAtLeast("OrderServiceGrpc.class",
                                 "OrderServiceGrpc$OrderServiceStub.class",
                                 "OrderServiceGrpc$OrderServiceImplBase.class");
    }

    @Test
    @DisplayName("register `generated/main/resources` as a resource directory")
    void includeResources() {
        configureJavaGeneration();
        String resourceName = "foo.txt";
        Set<String> emptyFile = emptySet();
        GradleProject project =
                this.project.createFile("generated/main/resources/" + resourceName, emptyFile)
                            .build();
        project.executeTask(build);
        Collection<String> resourceFiles = assembledResources();
        assertThat(resourceFiles).contains(resourceName);
    }

    @Test
    @DisplayName("disable Java codegen")
    void disableJava() {
        configureJavaWithoutGen();
        Path compiledClasses = compiledJavaClasses();
        assertFalse(exists(compiledClasses));
    }

    @Test
    @DisplayName("disable Java codegen and ignore gRPC settings")
    void disableJavaAndGrpc() {
        configureJavaAndGrpcWithoutGen();
        Path compiledClasses = compiledJavaClasses();
        assertFalse(exists(compiledClasses));
    }

    @Test
    @DisplayName("disable rejection throwable generation")
    void ignoreRejections() {
        configureJavaWithoutProtoOrSpine();
        GradleProject project = this.project
                .addProtoFile("restaurant_rejections.proto")
                .build();
        project.executeTask(build);
        Path compiledClasses = compiledJavaClasses();
        assertFalse(exists(compiledClasses));
    }

    @Test
    @DisplayName("generate no code for projects that only define the model")
    void noJsForModelProjects() {
        configureModelProject();
        GradleProject project = this.project.build();
        project.executeTask(build);

        assertThat(generatedFiles().toFile()
                                   .exists()).isFalse();
    }

    private void noAdditionalConfig() {
        writeConfigGradle();
    }

    private void configureJavaGeneration() {
        writeConfigGradle("spine.enableJava()");
    }

    private void configureJavaAndJs() {
        writeConfigGradle("spine.enableJava()",
                          "spine.enableJavaScript()");
    }

    private void configureJsGeneration() {
        writeConfigGradle(
                "spine.enableJavaScript()"
        );
    }

    private void configureDartGeneration() {
        writeConfigGradle(
                "spine.enableDart()"
        );
    }

    @SuppressWarnings("CheckReturnValue")
    private void configureJavaClient() {
        writeConfigGradle(
                "spine.enableJava().client()"
        );
        project.addProtoFile("client.proto");
    }

    @SuppressWarnings("CheckReturnValue")
    private void configureJavaServer() {
        writeConfigGradle(
                "spine.enableJava().server()"
        );
        project.addProtoFile("server.proto");
    }

    @SuppressWarnings("CheckReturnValue")
    private void configureGrpc() {
        writeConfigGradle(
                "spine {",
                "    enableJava {",
                "        codegen.grpc = true",
                "    }",
                "}"
        );
        project.addProtoFile("restaurant.proto");
    }

    private void configureJavaWithoutGen() {
        writeConfigGradle("spine.enableJava().codegen.protobuf = false");
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    // Part of the file contents may be duplicated.
    private void configureJavaAndGrpcWithoutGen() {
        writeConfigGradle(
                "spine.enableJava {",
                "    codegen {",
                "        protobuf = false",
                "        grpc = true",
                "    }",
                "}");
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    // Part of the file contents may be duplicated.
    private void configureJavaWithoutProtoOrSpine() {
        writeConfigGradle(
                "spine.enableJava {",
                "    codegen {",
                "        protobuf = false",
                "        spine = false",
                "    }",
                "}");
    }

    private void configureModelProject() {
        writeConfigGradle("spine.assembleModel()");
    }

    @SuppressWarnings("CheckReturnValue")
    private void writeConfigGradle(String... lines) {
        project.createFile(ADDITIONAL_CONFIG_SCRIPT, ImmutableSet.copyOf(lines));
    }

    private Collection<String> assembledResources() {
        Path resourcePath = projectDir.resolve("build")
                                      .resolve("resources")
                                      .resolve("main");
        File resourceDir = resourcePath.toFile();
        assertTrue(resourceDir.exists());
        assertTrue(resourceDir.isDirectory());
        String[] resources = resourceDir.list();
        assertNotNull(resources);
        return ImmutableList.copyOf(resources);
    }

    private Collection<String> generatedClassFileNames() {
        Path compiledJavaClasses = compiledJavaClasses();
        File compiledClassesDir = compiledJavaClasses.toFile();
        assertTrue(compiledClassesDir.exists());
        assertTrue(compiledClassesDir.isDirectory());
        @SuppressWarnings("ConstantConditions")
        ImmutableSet<String> dirContents = ImmutableSet.copyOf(compiledClassesDir.list());
        IterableSubject assertCompiledClassesDir = assertThat(dirContents);
        assertCompiledClassesDir.isNotEmpty();
        assertCompiledClassesDir.containsExactly("io");

        Path compiledClassesPackage = resolveClassesInPackage(compiledJavaClasses);
        @SuppressWarnings("ConstantConditions")
        ImmutableSet<String> packageContents = ImmutableSet.copyOf(compiledClassesPackage.toFile()
                                                                                         .list());
        return packageContents;
    }

    private Path compiledJavaClasses() {
        Path compiledClasses = projectDir.resolve("build")
                                         .resolve("classes")
                                         .resolve("java")
                                         .resolve("main");
        return compiledClasses;
    }

    private Path generatedFiles() {
        Path generated = projectDir.resolve("generated");
        return generated;
    }

    private Path generatedJsFiles() {
        Path compiledJsFiles = generatedFiles()
                .resolve("main")
                .resolve("js");
        return compiledJsFiles;
    }

    private static Path resolveClassesInPackage(Path compiledJavaClasses) {
        return compiledJavaClasses.resolve("io")
                                  .resolve("spine")
                                  .resolve("tools")
                                  .resolve("bootstrap")
                                  .resolve("test");
    }

    private Collection<String> generatedJsFileNames() {
        Path compiledJsFiles = generatedJsFiles();
        File compiledJsDir = compiledJsFiles.toFile();
        assertTrue(compiledJsDir.exists());
        assertTrue(compiledJsDir.isDirectory());
        @SuppressWarnings("ConstantConditions")
        ImmutableSet<String> packageContents = ImmutableSet.copyOf(compiledJsDir.list());
        return packageContents;
    }

    private Collection<String> generatedDartFileNames() {
        Path libDir = projectDir.resolve("lib");
        File libDirFile = libDir.toFile();
        assertTrue(libDirFile.exists());
        assertTrue(libDirFile.isDirectory());
        @SuppressWarnings("ConstantConditions")
        ImmutableSet<String> packageContents = ImmutableSet.copyOf(libDirFile.list());
        return packageContents;
    }
}
