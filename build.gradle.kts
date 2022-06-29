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

import io.spine.gradle.internal.DependencyResolution
import io.spine.gradle.internal.Deps
import io.spine.gradle.internal.PublishingRepos

plugins {
    java
    idea
    jacoco
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    id("net.ltgt.errorprone").version(io.spine.gradle.internal.Deps.versions.errorPronePlugin)
}

extra["credentialsPropertyFile"] = PublishingRepos.cloudRepo.credentials
extra["projectsToPublish"] = listOf("plugin")

apply(from = "$rootDir/version.gradle.kts")

val spineVersion: String by extra
val spineBaseVersion: String by extra
val pluginVersion: String by extra

allprojects {
    apply(from = "$rootDir/version.gradle.kts")
    apply(from = "$rootDir/config/gradle/dependencies.gradle")

    group = "io.spine.tools"
    version = pluginVersion
}

subprojects {
    apply {
        plugin("java")
        plugin("idea")
        plugin("net.ltgt.errorprone")
        plugin("pmd")

        from(Deps.scripts.slowTests(project))
        from(Deps.scripts.testOutput(project))
        from(Deps.scripts.javadocOptions(project))
        from(Deps.scripts.pmd(project))
        from(Deps.scripts.projectLicenseReport(project))
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    DependencyResolution.defaultRepositories(repositories)

    dependencies {
        errorprone(Deps.build.errorProneCore)
        errorproneJavac(Deps.build.errorProneJavac)

        implementation(Deps.build.guava)
        implementation("io.spine:spine-base:$spineBaseVersion")

        compileOnly(Deps.build.checkerAnnotations)
        compileOnly(Deps.build.jsr305Annotations)
        Deps.build.errorProneAnnotations.forEach { compileOnly(it) }

        testImplementation(Deps.test.guavaTestlib)
        testImplementation(Deps.test.junitPioneer)
        Deps.test.junit5Api.forEach { testImplementation(it) }
        Deps.test.truth.forEach { testImplementation(it) }
        testRuntimeOnly(Deps.test.junit5Runner)
    }

    DependencyResolution.forceConfiguration(configurations)

    tasks.withType(Test::class) {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
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
}

apply {
    from(Deps.scripts.publish(project))
    from(Deps.scripts.jacoco(project))
    from(Deps.scripts.repoLicenseReport(project))
    from(Deps.scripts.generatePom(project))
}

rootProject.afterEvaluate {
    val pluginProject = project(":plugin")
    pluginProject.tasks["publish"].dependsOn(pluginProject.tasks["publishPlugins"])
}
