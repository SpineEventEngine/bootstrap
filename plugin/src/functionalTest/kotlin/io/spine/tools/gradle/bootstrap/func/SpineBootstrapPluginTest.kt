/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.gradle.bootstrap.func

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.truth.Truth
import io.spine.code.proto.FileDescriptors
import io.spine.testing.SlowTest
import io.spine.tools.gradle.testing.GradleProject
import io.spine.tools.gradle.task.BaseTaskName
import io.spine.tools.gradle.testing.GradleProjectSetup
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@SlowTest
@DisplayName("`io.spine.bootstrap` plugin should")
@Disabled("Until new API is introduced")
internal class SpineBootstrapPluginTest {

    private lateinit var project: GradleProjectSetup
    private lateinit var projectDir: File

    @BeforeEach
    fun setUp(@TempDir projectDir: File) {
        this.projectDir = projectDir
    }

    /**
     * Creates the project environment by copying the `roller_coaster.proto` file with
     * optional [additionalProtoFile] from the `func-test` resource directory.
     */
    private fun setupProject(vararg additionalProtoFile: String) {
        val filesToInclude =
            listOf("build.gradle.kts",
                "settings.gradle.kts",
                "roller_coaster.proto")
            additionalProtoFile.toMutableList()
        
        project = GradleProject.setupAt(projectDir)
            .fromResources("func-test") { path ->
                val pathStr = path.toString()
                filesToInclude.any { pathStr.endsWith(it) }
            }
            .withPluginClasspath()
    }

    @Test
    fun `be applied to a project successfully`() {
        noAdditionalConfig()
        project.create()
            .executeTask(BaseTaskName.build)
    }

    @Test
    fun `generate no code if none requested`() {
        noAdditionalConfig()
        project.create()
            .executeTask(BaseTaskName.build)
        val compiledClasses = compiledJavaClasses()
        if (Files.exists(compiledClasses)) {
            val compiledClassesDirectory = compiledClasses.toFile()
            Truth.assertThat(compiledClassesDirectory.list()).isEmpty()
        }
    }

    @Test
    fun `generate Java if requested`() {
        configureJavaGeneration()
        val project = project.create()
        project.executeTask(BaseTaskName.build)

        val packageContents = generatedClassFileNames()
        val assertPackageContents = Truth.assertThat(packageContents)
        assertPackageContents.containsAtLeast(
            "LunaParkProto.class",
            "RollerCoaster.class",
            "Wagon.class",
            "Altitude.class"
        )
    }

    @Test
    fun `apply 'spine-model-compiler' plugin, generating descriptor set files`() {
        configureJavaGeneration()
        val project = project.create()
        project.executeTask(BaseTaskName.build)

        val resourceFiles = assembledResources()
        val projectDir = projectDir.toString()
        val containsDescriptorSetFile =
            resourceFiles.stream()
                .filter { f: String -> f.endsWith(FileDescriptors.DESC_EXTENSION) }
                .anyMatch { f: String -> f.contains(projectDir) }
        Truth.assertThat(containsDescriptorSetFile)
            .isTrue()
        Truth.assertThat(resourceFiles)
            .contains("desc.ref")
    }

    @Test
    fun `add client dependencies to the project`() {
        configureJavaClient()
        val project = project.create()
        project.executeTask(BaseTaskName.build)
        Truth.assertThat(generatedClassFileNames())
            .contains("ReceivedQuery.class")
    }

    @Test
    fun `add server dependencies to the project`() {
        configureJavaServer()
        val project = project.create()
        project.executeTask(BaseTaskName.build)
        Truth.assertThat(generatedClassFileNames())
            .contains("Nonevent.class")
    }

    @Test
    fun `generate gRPC stubs if required`() {
        configureGrpc()
        val project = project.create()
        project.executeTask(BaseTaskName.build)
        Truth.assertThat(generatedClassFileNames())
            .containsAtLeast(
                "OrderServiceGrpc.class",
                "OrderServiceGrpc\$OrderServiceStub.class",
                "OrderServiceGrpc\$OrderServiceImplBase.class"
            )
    }

    @Test
    fun `register 'generated-main-resources' as a resource directory`() {
        configureJavaGeneration()
        val resourceName = "foo.txt"
        val emptyFile = emptySet<String>()
        val project =
            project.addFile("generated/main/resources/$resourceName", emptyFile)
                .create()
        project.executeTask(BaseTaskName.build)
        val resourceFiles = assembledResources()
        Truth.assertThat(resourceFiles).contains(resourceName)
    }

    @Test
    fun `disable Java codegen`() {
        configureJavaWithoutGen()
        val compiledClasses = compiledJavaClasses()
        Assertions.assertFalse(Files.exists(compiledClasses))
    }

    @Test
    fun `disable Java codegen and ignore gRPC settings`() {
        configureJavaAndGrpcWithoutGen()
        val compiledClasses = compiledJavaClasses()
        Assertions.assertFalse(Files.exists(compiledClasses))
    }

    @Test
    fun `disable rejection throwable generation`() {
//        configureJavaWithoutProtoOrSpine();
//        GradleProject project = this.project
//                .addProtoFile("restaurant_rejections.proto")
//                .build();
//        project.executeTask(build);
//        Path compiledClasses = compiledJavaClasses();
//        assertFalse(exists(compiledClasses));
    }

    @Test
    fun `generate no code for projects that only define the model`() {
        configureModelProject()
        val project = project.create()
        project.executeTask(BaseTaskName.build)

        Truth.assertThat(
            generatedFiles().toFile()
                .exists()
        ).isFalse()
    }

    private fun noAdditionalConfig() {
        setupProject()
        writeConfigGradle()
    }

    private fun configureJavaGeneration() {
        setupProject()
        writeConfigGradle("spine.enableJava()")
    }

    private fun configureJavaClient() {
        setupProject("client.proto")
        writeConfigGradle(
            "spine.enableJava().client()"
        )
    }

    private fun configureJavaServer() {
        setupProject("server.proto")
        writeConfigGradle(
            "spine.enableJava().server()"
        )
    }

    private fun configureGrpc() {
        setupProject("restaurant.proto")
        writeConfigGradle(
            "spine {",
            "    enableJava {",
            "        codegen.grpc = true",
            "    }",
            "}"
        )
    }

    private fun configureJavaWithoutGen() {
        setupProject()
        writeConfigGradle("spine.enableJava().codegen.protobuf = false")
    }

    // Part of the file contents may be duplicated.
    private fun configureJavaAndGrpcWithoutGen() {
        writeConfigGradle(
            "spine.enableJava {",
            "    codegen {",
            "        protobuf = false",
            "        grpc = true",
            "    }",
            "}"
        )
    }

    private fun configureModelProject() {
        setupProject()
        writeConfigGradle("spine.assembleModel()")
    }

    private fun writeConfigGradle(vararg lines: String) {
        project.addFile(ADDITIONAL_CONFIG_SCRIPT, ImmutableSet.copyOf(lines))
    }

    private fun assembledResources(): Collection<String> {
        val resourceDir = projectDir.resolve("build")
            .resolve("resources")
            .resolve("main")
        assertTrue(resourceDir.exists())
        assertTrue(resourceDir.isDirectory)
        val resources = resourceDir.list()
        assertNotNull(resources)
        return ImmutableList.copyOf(resources!!)
    }

    private fun generatedClassFileNames(): Collection<String?> {
        val compiledJavaClasses = compiledJavaClasses()
        val compiledClassesDir = compiledJavaClasses.toFile()
        assertTrue(compiledClassesDir.exists())
        assertTrue(compiledClassesDir.isDirectory)
        val dirContents = ImmutableSet.copyOf(compiledClassesDir.list()!!)
        val assertCompiledClassesDir = Truth.assertThat(dirContents)
        assertCompiledClassesDir.isNotEmpty()
        assertCompiledClassesDir.containsExactly("io")

        val compiledClassesPackage = resolveClassesInPackage(compiledJavaClasses)
        val packageContents = ImmutableSet.copyOf(
            compiledClassesPackage.toFile().list()!!
        )
        return packageContents
    }

    private fun compiledJavaClasses(): Path {
        val compiledClasses = projectDir.resolve("build")
            .resolve("classes")
            .resolve("java")
            .resolve("main")
        return compiledClasses.toPath()
    }

    private fun generatedFiles(): Path {
        val generated = projectDir.resolve("generated")
        return generated.toPath()
    }

    companion object {
        private const val ADDITIONAL_CONFIG_SCRIPT = "config.gradle"

        private fun resolveClassesInPackage(compiledJavaClasses: Path): Path {
            return compiledJavaClasses.resolve("io")
                .resolve("spine")
                .resolve("tools")
                .resolve("bootstrap")
                .resolve("test")
        }
    }
}
