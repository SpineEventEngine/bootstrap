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

package io.spine.tools.bootstrap.func;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.IterableSubject;
import io.spine.tools.gradle.testing.GradleProject;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.TaskName.build;
import static io.spine.tools.gradle.TaskName.generateJsonParsers;
import static io.spine.tools.gradle.TaskName.generateValidatingBuilders;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@Functional
@DisplayName("`io.spine.bootstrap` plugin should")
class SpineBootstrapPluginTest {

    private static final String ADDITIONAL_CONFIG_SCRIPT = "config.gradle";

    private GradleProject.Builder project;
    private Path projectDir;

    @BeforeEach
    void setUp(@TempDir Path dir) {
        this.projectDir = dir;
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
        if (Files.exists(compiledClasses)) {
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
        assertPackageContents.containsAllOf("LunaParkProto.class",
                                            "RollerCoaster.class",
                                            "Wagon.class",
                                            "Altitude.class");
    }

    @Test
    @DisplayName("apply 'spine-model-compiler' plugin")
    void applyModelCompiler() {
        configureJavaGeneration();
        GradleProject project = this.project.build();
        BuildResult result = project.executeTask(build);

        assertThat(result.task(generateValidatingBuilders.path()).getOutcome()).isEqualTo(SUCCESS);

        Collection<String> packageContents = generatedClassFileNames();
        IterableSubject assertPackageContents = assertThat(packageContents);
        assertPackageContents.containsAllOf("RollerCoasterVBuilder.class",
                                            "WagonVBuilder.class",
                                            "AltitudeVBuilder.class");
    }

    @Test
    @DisplayName("generate JavaScript if requested")
    void generateJs() {
        configureJsGeneration();
        GradleProject project = this.project.build();
        project.executeTask(build);

        Collection<String> jsFileNames = generatedJsFileNames();
        assertThat(jsFileNames).containsExactly("roller_coaster_pb.js");
    }

    @Test
    @DisplayName("apply 'spine-proto-js-plugin'")
    void applyJsPlugin() {
        configureJsGeneration();
        GradleProject project = this.project.build();
        BuildResult result = project.executeTask(build);

        assertThat(result.task(generateJsonParsers.path()).getOutcome()).isEqualTo(SUCCESS);
    }

    @SuppressWarnings("CheckReturnValue")
    private void noAdditionalConfig() {
        project.createFile(ADDITIONAL_CONFIG_SCRIPT, ImmutableSet.of());
    }

    @SuppressWarnings("CheckReturnValue")
    private void configureJavaGeneration() {
        project.createFile(ADDITIONAL_CONFIG_SCRIPT, ImmutableSet.of("spine.java()"));
    }

    @SuppressWarnings("CheckReturnValue")
    private void configureJsGeneration() {
        project.createFile(ADDITIONAL_CONFIG_SCRIPT, ImmutableSet.of(
                "spine.javaScript()",
                "compileJava.enabled = false"
        ));
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

    private static Path resolveClassesInPackage(Path compiledJavaClasses) {
        return compiledJavaClasses.resolve("io")
                                  .resolve("spine")
                                  .resolve("tools")
                                  .resolve("bootstrap")
                                  .resolve("test");
    }

    private Collection<String> generatedJsFileNames() {
        Path compiledJsFiles = projectDir.resolve("build")
                                         .resolve("generated")
                                         .resolve("source")
                                         .resolve("proto")
                                         .resolve("main")
                                         .resolve("js");
        File compiledJsDir = compiledJsFiles.toFile();
        assertTrue(compiledJsDir.exists());
        assertTrue(compiledJsDir.isDirectory());
        @SuppressWarnings("ConstantConditions")
        ImmutableSet<String> packageContents = ImmutableSet.copyOf(compiledJsDir.list());
        return packageContents;
    }
}
