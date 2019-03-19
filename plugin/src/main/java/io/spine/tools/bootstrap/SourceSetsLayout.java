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

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

final class SourceSetsLayout implements CodeLayout {

    private final Project project;

    private SourceSetsLayout(Project project) {
        this.project = project;
    }

    static SourceSetsLayout of(Project project) {
        checkNotNull(project);
        return new SourceSetsLayout(project);
    }

    @Override
    public void javaSourcesRoot(Path rootDirectory) {
        checkNotNull(rootDirectory);

        SourceSetContainer sourceSets = sourceSets();
        sourceSets.forEach(sourceSet -> {
            Path scopeDir = rootDirectory.resolve(sourceSet.getName());
            sourceSet.getJava()
                     .srcDirs(
                             scopeDir.resolve("java").toFile(),
                             scopeDir.resolve("spine").toFile()
                     );
        });
    }

    private SourceSetContainer sourceSets() {
        JavaPluginConvention javaConvention = project.getConvention()
                                                     .getPlugin(JavaPluginConvention.class);
        SourceSetContainer sourceSets = javaConvention.getSourceSets();
        return sourceSets;
    }
}
