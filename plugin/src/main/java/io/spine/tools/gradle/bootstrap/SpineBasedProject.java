/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.bootstrap;

import io.spine.net.Url;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.Dependency;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.project.Dependant;
import io.spine.tools.gradle.project.DependantProject;
import org.checkerframework.checker.regex.qual.Regex;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Dependant} Gradle project which uses Spine.
 */
final class SpineBasedProject implements Dependant {

    private static final @Regex String SPINE_GROUP_PATTERN = "io\\.spine\\b.*";

    private final Dependant dependencies;
    private final Project project;

    private SpineBasedProject(Dependant dependencies, Project project) {
        this.dependencies = checkNotNull(dependencies);
        this.project = checkNotNull(project);
    }

    /**
     * Wraps the given Gradle project.
     */
    static SpineBasedProject from(Project project) {
        checkNotNull(project);
        DependantProject dependantProject = DependantProject.from(project);
        return new SpineBasedProject(dependantProject, project);
    }

    @Override
    public void depend(ConfigurationName configurationName, String notation) {
        dependencies.depend(configurationName, notation);
    }

    @Override
    public void exclude(Dependency dependency) {
        dependencies.exclude(dependency);
    }

    @Override
    public void force(Artifact artifact) {
        dependencies.force(artifact);
    }

    @Override
    public void force(String notation) {
        dependencies.force(notation);
    }

    @Override
    public void removeForcedDependency(Dependency dependency) {
        dependencies.removeForcedDependency(dependency);
    }

    @Override
    public void removeForcedDependency(String notation) {
        dependencies.removeForcedDependency(notation);
    }

    /**
     * Sets up Maven repositories required by Spine.
     *
     * <p>Adds the following repositories:
     * <ol>
     *     <li>Spine releases repository for Spine artifacts;
     *     <li>Spine snapshots repository for Spine artifacts;
     *     <li>JCenter repository for third-party artifacts.
     * </ol>
     */
    void prepareRepositories(ArtifactSnapshot artifacts) {
        RepositoryHandler repositories = project.getRepositories();
        addSpineRepository(artifacts.spineRepository(),
                           MavenRepositoryContentDescriptor::releasesOnly);
        addSpineRepository(artifacts.spineSnapshotRepository(),
                           MavenRepositoryContentDescriptor::snapshotsOnly);
        repositories.jcenter();
    }

    @SuppressWarnings("UnstableApiUsage")
        // Usage of the advanced repository configuration API.
    private void addSpineRepository(Url repositoryUrl,
                                    Consumer<MavenRepositoryContentDescriptor> contentConfig) {
        project.getRepositories().maven(repo -> {
            repo.setUrl(repositoryUrl.getSpec());
            repo.mavenContent(contentConfig::accept);
            repo.content(descriptor -> descriptor.includeGroupByRegex(SPINE_GROUP_PATTERN));
        });
    }
}
