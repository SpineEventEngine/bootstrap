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
import com.google.common.collect.Lists;
import io.spine.code.proto.FileDescriptors;
import io.spine.testing.SlowTest;
import io.spine.tools.gradle.testing.GradleProject;
import io.spine.tools.gradle.testing.GradleProjectSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.bootstrap.DartExtension.TYPES_FILE;
import static io.spine.tools.gradle.task.BaseTaskName.build;
import static io.spine.tools.mc.js.gradle.McJsTaskName.generateJsonParsers;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.util.Collections.emptySet;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the Bootstrap Plugin.
 *
 * <p>The following commands should be executed for configuring Protobuf for Dart before
 * running this test suite:
 * <pre>
 * $ ./config/scripts/update-apt.sh
 * $ sudo apt-get install dart
 * $ pub global activate protoc_plugin
 * $ pub global activate dart_code_gen
 * </pre>
 */
@SlowTest
@DisplayName("`io.spine.bootstrap` plugin should")
class SpineBootstrapPluginTest {

    private static final String ADDITIONAL_CONFIG_SCRIPT = "config.gradle";
    private static final String TRANSITIVE_JS_DEPENDENCY = "any_pb.js";
    private static final String RESOURCE_DIR = "func-test";

    private Path projectDir;
    private GradleProjectSetup setup;
    private GradleProject project;

    @BeforeEach
    void setUp(@TempDir File projectDir) {
        this.projectDir = projectDir.toPath();
        //TODO:2022-04-18:alexander.yevsyukov: Load version from resources.
        var version = "2.0.0-SNAPSHOT.87";
        setup = GradleProject.setupAt(projectDir)
                             .replace("@spine-version@", version)
                             .withPluginClasspath();
    }

    @Test
    @DisplayName("be applied to a project successfully")
    void apply() {
        noAdditionalConfig();
        withRollerCoasterProto();
        setup.create()
             .executeTask(build);
    }

    @Test
    @DisplayName("generate no code if none requested")
    void generateNothing() {
        noAdditionalConfig();
        withRollerCoasterProto();
        setup.create()
             .executeTask(build);
        var compiledClasses = compiledJavaClasses();
        if (exists(compiledClasses)) {
            var compiledClassesDirectory = compiledClasses.toFile();
            assertThat(compiledClassesDirectory.list()).isEmpty();
        }
    }

    private void withFiles(String... fileNames) {
        var withBuildScript = Lists.newArrayList(fileNames);
        withBuildScript.add("build.gradle");
        withBuildScript.add("settings.gradle");
        var predicate = (Predicate<Path>) path -> withBuildScript.stream().anyMatch(path::endsWith);
        setup.fromResources(RESOURCE_DIR, predicate);
    }

    private void withGeneralProtoFiles() {
        withFiles("restaurant_rejections.proto",
                  "roller_coaster.proto");
    }

    private void withRollerCoasterProto() {
        withFiles("roller_coaster.proto");
    }

    @Test
    @DisplayName("generate Java if requested")
    void generateJava() {
        configureJavaGeneration();
        withGeneralProtoFiles();
        project = setup.create();
        project.executeTask(build);

        var packageContents = generatedClassFileNames();
        assertThat(packageContents).containsAtLeast(
                "LunaParkProto.class",
                "RollerCoaster.class",
                "Wagon.class",
                "Altitude.class"
        );
    }

    @Test
    @DisplayName("apply 'spine-mc-java' plugin, generating descriptor set files")
    void applyModelCompiler() {
        configureJavaGeneration();
        withGeneralProtoFiles();
        project = setup.create();
        project.executeTask(build);

        var resourceFiles = assembledResources();
        var projectDir = this.projectDir.getFileName()
                                        .toString();
        var containsDescriptorSetFile = resourceFiles.stream()
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
        withRollerCoasterProto();
        project = setup.create();
        project.executeTask(build);

        var jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).contains("roller_coaster_pb.js");
    }

    @Test
    @DisplayName("generate Dart if requested")
    void generateDart() {
        configureDartGeneration();
        withRollerCoasterProto();
        setup.withOptions("--info");

        project = setup.create();
        project.executeTask(build);

        var dartFileNames = generatedDartFileNames();
        var protoName = "roller_coaster";
        assertThat(dartFileNames)
                .containsExactly(
                        TYPES_FILE,
                        protoName + ".pb.dart",
                        protoName + ".pbjson.dart",
                        protoName + ".pbenum.dart",
                        protoName + ".pbserver.dart"
                );
    }

    /**
     * Verifies that {@code index.js} file is generated.
     *
     * <p>The creation of the file is one of the steps performed by the {@code generateJsonParsers}
     * task performed by the {@code mc-js} plugin.
     */
    @Test
    @DisplayName("generate an `index.js` file")
    void generateIndexJs() {
        configureJsGeneration();
        withGeneralProtoFiles();
        project = setup.create();
        project.executeTask(build);

        var jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).contains("index.js");
    }

    @Test
    @DisplayName("not generate transitive Spine dependencies for pure JS projects")
    void skipTransitiveProtos() {
        configureJsGeneration();
        withGeneralProtoFiles();
        project = setup.create();
        project.executeTask(build);

        var jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).doesNotContain(TRANSITIVE_JS_DEPENDENCY);
    }

    @Test
    @DisplayName("not generate transitive Spine dependencies for mixed projects")
    void skipTransitiveProtosForMixed() {
        configureJavaAndJs();
        withGeneralProtoFiles();
        project = setup.create();
        project.executeTask(build);
        assertThat(generatedJsFileNames()).doesNotContain(TRANSITIVE_JS_DEPENDENCY);
        assertThat(generatedClassFileNames()).doesNotContain("Any.class");
    }

    @Test
    @DisplayName("apply 'spine-proto-js-plugin'")
    void applyJsPlugin() {
        configureJsGeneration();
        withProjectFiles();
        project = setup.create();
        var result = project.executeTask(build);

        assertThat(result.task(generateJsonParsers.path())
                         .getOutcome()).isEqualTo(SUCCESS);
    }

    private void withProjectFiles() {
        setup.fromResources(
                RESOURCE_DIR,
                "build.gradle",
                "settings.gradle"
        );
    }

    @Test
    @DisplayName("add client dependencies to the project")
    void clientDeps() {
        configureJavaClient();
        project = this.setup.create();
        project.executeTask(build);
        assertThat(generatedClassFileNames())
                .contains("ReceivedQuery.class");
    }

    @Test
    @DisplayName("add server dependencies to the project")
    void serverDeps() {
        configureJavaServer();
        project = this.setup.create();
        project.executeTask(build);
        assertThat(generatedClassFileNames())
                .contains("Nonevent.class");
    }

    @Test
    @DisplayName("generate gRPC stubs if required")
    void generateGrpc() {
        configureGrpc();
        project = setup.create();
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
        var resourceName = "foo.txt";
        Set<String> noLines = emptySet();
        project = setup.addFile("generated/main/resources/" + resourceName, noLines).create();
        project.executeTask(build);
        var resourceFiles = assembledResources();
        assertThat(resourceFiles).contains(resourceName);
    }

    @Test
    @DisplayName("disable Java codegen")
    void disableJava() {
        configureJavaWithoutGen();
        withProjectFiles();
        var compiledClasses = compiledJavaClasses();
        assertFalse(exists(compiledClasses));
    }

    @Test
    @DisplayName("disable Java codegen and ignore gRPC settings")
    void disableJavaAndGrpc() {
        configureJavaAndGrpcWithoutGen();
        withProjectFiles();
        var compiledClasses = compiledJavaClasses();
        assertFalse(exists(compiledClasses));
    }

    @Test
    @DisplayName("disable rejection throwable generation")
    void ignoreRejections() {
        configureJavaWithoutProtoOrSpine();
        setup.fromResources(
                RESOURCE_DIR,
                "restaurant_rejections.proto",
                "build.gradle",
                "settings.gradle"
        );
        project = setup.create();
        project.executeTask(build);
        var compiledClasses = compiledJavaClasses();
        assertFalse(exists(compiledClasses));
    }

    @Test
    @DisplayName("generate no code for projects that only define the model")
    void noJsForModelProjects() {
        configureModelProject();
        withProjectFiles();
        project = setup.create();
        project.executeTask(build);

        var generatedFiles = generatedFiles();
        assertNotExists(generatedFiles);
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
        withFiles("client.proto", "roller_coaster.proto");
    }

    @SuppressWarnings("CheckReturnValue")
    private void configureJavaServer() {
        writeConfigGradle(
                "spine.enableJava().server()"
        );
        withFiles("server.proto", "roller_coaster.proto");
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
        withFiles("restaurant.proto", "roller_coaster.proto");
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
        setup.addFile(ADDITIONAL_CONFIG_SCRIPT, ImmutableSet.copyOf(lines));
    }

    private Collection<String> assembledResources() {
        var resourcePath = projectDir.resolve("build")
                                     .resolve("resources")
                                     .resolve("main");
        assertExists(resourcePath);
        var resourceDir = resourcePath.toFile();
        assertTrue(resourceDir.isDirectory());
        var resources = resourceDir.list();
        assertNotNull(resources);
        return ImmutableList.copyOf(resources);
    }

    private Collection<String> generatedClassFileNames() {
        var compiledJavaClasses = compiledJavaClasses();
        assertExists(compiledJavaClasses);
        var compiledClassesDir = compiledJavaClasses.toFile();
        assertTrue(compiledClassesDir.isDirectory());
        @SuppressWarnings("ConstantConditions")
        var dirContents = ImmutableSet.copyOf(compiledClassesDir.list());
        var assertCompiledClassesDir = assertThat(dirContents);
        assertCompiledClassesDir.isNotEmpty();
        assertCompiledClassesDir.containsExactly("io");

        var compiledClassesPackage = resolveClassesInPackage(compiledJavaClasses);
        @SuppressWarnings("ConstantConditions")
        var packageContents = ImmutableSet.copyOf(compiledClassesPackage.toFile()
                                                                        .list());
        return packageContents;
    }

    private Path compiledJavaClasses() {
        var compiledClasses = projectDir.resolve("build")
                                        .resolve("classes")
                                        .resolve("java")
                                        .resolve("main");
        return compiledClasses;
    }

    private Path generatedFiles() {
        var generated = projectDir.resolve("generated");
        return generated;
    }

    private Path generatedJsFiles() {
        var compiledJsFiles = generatedFiles()
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

    private static void assertExists(Path path) {
        assertTrue(path.toFile().exists(), format("Expected to exist: `%s`.", path));
    }

    private static void assertNotExists(Path path) {
        assertFalse(path.toFile().exists(), format("Expected to NOT exist: `%s`.", path));
    }
    
    private Collection<String> generatedJsFileNames() {
        var generatedJsFiles = generatedJsFiles();
        assertExists(generatedJsFiles);
        var compiledJsDir = generatedJsFiles.toFile();
        assertTrue(compiledJsDir.isDirectory());
        @SuppressWarnings("ConstantConditions")
        var packageContents = ImmutableSet.copyOf(compiledJsDir.list());
        return packageContents;
    }

    private Collection<String> generatedDartFileNames() {
        var libDir = projectDir.resolve("lib");
        assertExists(libDir);
        var libDirFile = libDir.toFile();
        assertTrue(libDirFile.isDirectory());
        @SuppressWarnings("ConstantConditions")
        var packageContents = ImmutableSet.copyOf(libDirFile.list());
        return packageContents;
    }
}
