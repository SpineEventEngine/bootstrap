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

@file:Suppress("RemoveRedundantQualifierName") // To prevent IDEA replacing FQN imports.

import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.FindBugs
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Truth
import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.kotlin.applyJvmToolchain
import io.spine.internal.gradle.kotlin.setFreeCompilerArgs
import io.spine.internal.gradle.publish.Publish.Companion.publishProtoArtifact
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.PublishingRepos.cloudArtifactRegistry
import io.spine.internal.gradle.publish.PublishingRepos.cloudRepo
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.testing.configureLogging
import io.spine.internal.gradle.testing.exposeTestArtifacts
import io.spine.internal.gradle.testing.registerTestTasks
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    idea
    jacoco
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id)
    }
    kotlin("jvm")
}

spinePublishing {
    projectsToPublish.addAll(subprojects.map { it.path })
    targetRepositories.addAll(
        cloudRepo,
        cloudArtifactRegistry,
        PublishingRepos.gitHub("bootstrap")
    )
    spinePrefix.set(true)
}

apply(from = "$rootDir/version.gradle.kts")

val bootstrapVersion: String by extra

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
        apply(from = "$rootDir/version.gradle.kts")
    }
    apply(from = "$rootDir/version.gradle.kts")

    repositories {
        PublishingRepos.gitHub("base")
        PublishingRepos.gitHub("tool-base")
        PublishingRepos.gitHub("model-compiler")
        applyStandard()
    }

    group = "io.spine.tools"
    version = bootstrapVersion
}

val baseVersion: String by extra

subprojects {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("idea")
        plugin("net.ltgt.errorprone")
        plugin("pmd-settings")
    }

    dependencies {
        errorprone(ErrorProne.core)
        compileOnlyApi(FindBugs.annotations)
        compileOnlyApi(CheckerFramework.annotations)
        ErrorProne.annotations.forEach { compileOnlyApi(it) }

        implementation(Guava.lib)
        implementation("io.spine:spine-base:$baseVersion")

        testImplementation(Guava.testLib)
        JUnit.api.forEach { testImplementation(it) }
        Truth.libs.forEach { testImplementation(it) }
        testRuntimeOnly(JUnit.runner)
    }

    val baseVersion: String by extra
    val toolBaseVersion: String by extra
    val mcVersion: String by extra
    with(configurations) {
        forceVersions()
        excludeProtobufLite()
        all {
            resolutionStrategy {
                force(
                    "io.spine:spine-base:$baseVersion",
                    "io.spine.tools:spine-testlib:$baseVersion",
                    "io.spine.tools:spine-tool-base:$toolBaseVersion",
                    "io.spine.tools:spine-plugin-base:$toolBaseVersion",
                    "io.spine.tools:spine-model-compiler:$mcVersion"
                )
            }
        }
    }

    java {
        exposeTestArtifacts()
    }

    tasks.withType<JavaCompile> {
        configureJavac()
        configureErrorProne()
    }

    JavadocConfig.applyTo(project)
    CheckStyleConfig.applyTo(project)

    val javaVersion = 11
    kotlin {
        applyJvmToolchain(javaVersion)
        explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        setFreeCompilerArgs()
    }

    tasks {
        registerTestTasks()
        test {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
            configureLogging()
        }
    }

    tasks.register("sourceJar", Jar::class) {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.register("testOutputJar", Jar::class) {
        from(sourceSets.test.get().output)
        archiveClassifier.set("test")
    }

    tasks.register("javadocJar", Jar::class) {
        from("$projectDir/build/docs/javadoc")
        archiveClassifier.set("javadoc")
        dependsOn(tasks.javadoc)
    }

    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    apply<IncrementGuard>()
    apply<VersionWriter>()
    publishProtoArtifact(project)
    LicenseReporter.generateReportIn(project)
}
