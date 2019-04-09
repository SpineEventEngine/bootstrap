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

package io.spine.tools.gradle;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.RUNTIME_CLASSPATH;
import static io.spine.tools.gradle.ConfigurationName.TEST_RUNTIME_CLASSPATH;

/**
 * A {@link DependencyTarget} implemented on top of a {@link DependencyHandler}  of a project.
 */
public final class ProjectDependencyTarget implements DependencyTarget {

    private final DependencyHandler dependencies;
    private final ConfigurationContainer configurations;

    private ProjectDependencyTarget(DependencyHandler dependencies,
                                    ConfigurationContainer configurations) {
        this.dependencies = dependencies;
        this.configurations = configurations;
    }

    /**
     * Creates a new instance of {@code ProjectDependencyTarget} for the given project.
     */
    public static ProjectDependencyTarget from(Project project) {
        checkNotNull(project);
        return new ProjectDependencyTarget(project.getDependencies(), project.getConfigurations());
    }

    @Override
    public void depend(String configurationName, String notation) {
        dependencies.add(configurationName, notation);
    }

    @Override
    public void exclude(Dependency dependency) {
        Configuration mainConfig = configurations.getByName(RUNTIME_CLASSPATH.getValue());
        exclude(mainConfig, dependency);

        Configuration testConfig = configurations.getByName(TEST_RUNTIME_CLASSPATH.getValue());
        exclude(testConfig, dependency);
    }

    private static void exclude(Configuration configuration, Dependency module) {
        configuration.exclude(ImmutableMap.of(
                "group", module.groupId(),
                "module", module.name()
        ));
    }
}
