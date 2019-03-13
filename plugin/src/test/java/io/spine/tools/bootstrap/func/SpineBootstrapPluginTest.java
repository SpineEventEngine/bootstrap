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

import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.testing.GradleProject;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TempDirectory.class)
@Functional
@DisplayName("`io.spine.bootstrap` plugin should")
class SpineBootstrapPluginTest {

    private GradleProject.Builder project;
    private Path projectDir;

    @BeforeEach
    void setUp(@TempDir Path dir) {
        this.projectDir = dir;
        this.project = GradleProject
                .newBuilder()
                .setProjectName("func-test")
                .setProjectFolder(projectDir.toFile())
                .withPluginClasspath();
    }

    @Test
    @DisplayName("be applied to a project successfully")
    void apply() {
        GradleProject project = this.project.build();
        project.executeTask(TaskName.build);
    }

    @Test
    @DisplayName("generate no code if none requested")
    void generateNothing() {
        GradleProject project = this.project
                .addProtoFile("roller_coaster.proto")
                .build();
        project.executeTask(TaskName.build);
        Path compiledClasses = projectDir.resolve("build")
                                         .resolve("classes")
                                         .resolve("java")
                                         .resolve("main");
        if (Files.exists(compiledClasses)) {
            File compiledClassesDirectory = compiledClasses.toFile();
            assertThat(compiledClassesDirectory.list()).isEmpty();
        }
    }
}