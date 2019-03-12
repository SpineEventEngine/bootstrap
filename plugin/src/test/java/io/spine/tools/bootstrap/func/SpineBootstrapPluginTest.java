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

import io.spine.tools.gradle.testing.GradleProject;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static io.spine.tools.gradle.TaskName.build;

@ExtendWith(TempDirectory.class)
@Functional
@DisplayName("`io.spine.bootstrap` plugin should")
class SpineBootstrapPluginTest {

    private GradleProject project;

    @BeforeEach
    void setUp(@TempDir Path dir) throws NoSuchFieldException, IllegalAccessException {
        project = GradleProject
                .newBuilder()
                .setProjectName(SpineBootstrapPluginTest.class.getSimpleName())
                .setProjectFolder(dir.toFile())
                .build();

        // TODO:2019-03-12:dmytro.dashenkov: Update GradleProject API to allow this config.
        Field runnerField = project.getClass()
                                   .getDeclaredField("gradleRunner");
        runnerField.setAccessible(true);
        GradleRunner runner = (GradleRunner) runnerField.get(project);
        runner.withPluginClasspath();
    }

    @Test
    @DisplayName("be applied to a project successfully")
    void apply() {
        project.executeTask(build);
    }
}
