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

import io.spine.internal.dependency.Grpc
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.gradle.isSnapshot
import io.spine.internal.gradle.publish.PublishingRepos.cloudArtifactRegistry
import java.io.FileWriter
import java.util.Properties
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id(io.spine.internal.dependency.Protobuf.GradlePlugin.id)
    `java-gradle-plugin`
    id("com.gradle.plugin-publish").version("0.18.0")
    id("com.github.johnrengelman.shadow").version("7.1.0")
}

apply<IncrementGuard>()

val baseVersion: String by extra
val baseTypesVersion: String by extra
val toolBaseVersion: String by extra
val mcVersion: String by extra
val mcJavaVersion: String by extra
val mcJsVersion: String by extra
val mcDartVersion: String by extra

val bootstrapVersion: String by extra

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(Protobuf.GradlePlugin.lib)

    implementation("io.spine:spine-base:$baseVersion")
    implementation("io.spine:spine-base-types:$baseTypesVersion")

    implementation("io.spine.tools:spine-plugin-base:$toolBaseVersion")
    implementation("io.spine.tools:spine-model-compiler:$mcVersion")
    implementation("io.spine.tools:spine-mc-java:$mcJavaVersion")
    implementation("io.spine.tools:spine-mc-java-base:$mcJavaVersion")
    implementation("io.spine.tools:spine-mc-js:$mcJsVersion")
    implementation("io.spine.tools:spine-mc-dart:$mcDartVersion")

    testImplementation("io.spine.tools:spine-testlib:$baseVersion")
    testImplementation("io.spine.tools:spine-plugin-testlib:$toolBaseVersion")
}

val targetResourceDir = "$buildDir/compiledResources/"

val prepareBuildScript by tasks.registering(Copy::class) {
    description = "Creates the `build.gradle` script which is executed " +
            "in functional tests of the plugin."

    from("$projectDir/src/test/build.gradle.template")
    into(targetResourceDir)

    rename { "build.gradle" }
    filter(mapOf("tokens" to mapOf("spine-version" to bootstrapVersion)), ReplaceTokens::class.java)
}

tasks.processTestResources {
    dependsOn(prepareBuildScript)
}

sourceSets {
    test {
        resources.srcDir(targetResourceDir)
    }
}

val pluginName = "spineBootstrapPlugin"

gradlePlugin {
    plugins {
        create(pluginName) {
            id = "io.spine.bootstrap"
            implementationClass = "io.spine.tools.gradle.bootstrap.BootstrapPlugin"
            displayName = "Spine Bootstrap"
            description = "Prepares a Gradle project for development on Spine."
        }
    }
}

pluginBundle {
    website = "https://spine.io/"
    vcsUrl = "https://github.com/SpineEventEngine/bootstrap.git"
    tags = listOf("spine", "event-sourcing", "ddd", "cqrs", "bootstrap")

    mavenCoordinates {
        groupId = "io.spine.tools"
        artifactId = "spine-bootstrap"
        version = bootstrapVersion
    }

    plugins {
        named(pluginName) {
            version = bootstrapVersion
        }
    }
}

/*
 * In order to simplify the Bootstrap plugin usage, the plugin should have no external dependencies
 * which cannot be found in the Plugin portal. Spine core modules are not published to
 * either of those repositories. Thus, we publish the "fat" JAR.
 *
 * As Gradle Plugin plugin always publishes the JAR artifact with the empty classifier, we add
 * the "pure" classifier to the default JAR artifact and generate the "fat" JAR with an empty
 * classifier.
 */

tasks.jar {
    archiveClassifier.set("pure")
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
    isZip64 = true
}

artifacts {
    archives(tasks.shadowJar)
}

val bootstrapDir = file("$buildDir/bootstrap")

sourceSets.main {
    resources.srcDir(bootstrapDir)
}

val versionSnapshot = file("$bootstrapDir/artifact-snapshot.properties")
val configDir = file("$rootDir/config")

val timeVersion: String by extra
val coreJavaVersion: String by extra
val webVersion: String by extra
val gCloudVersion: String by extra

/*
  This task creates the `artifact-snapshot.properties` file which is later added to the classpath of
  the Bootstrap plugin. The file contains versions, artifact notations, repositories, etc. used in
  the Gradle scripts which should also be used in the runtime of the Bootstrap plugin.

  The keys for the `artifact-snapshot.properties` file are duplicated in
  the `io.spine.tools.gradle.config.ArtifactSnapshot` class, where the file is parsed.
 */
val writeDependencies by tasks.registering {
    group = "Spine bootstrapping"

    inputs.dir(configDir)
    inputs.property("version", bootstrapVersion)
    outputs.file(versionSnapshot)

    doFirst {
        bootstrapDir.mkdirs()
        if (!versionSnapshot.exists()) {
            versionSnapshot.createNewFile()
        }
    }

    doLast {
        val artifacts = Properties()

        artifacts.setProperty("spine.version.base", baseVersion)
        artifacts.setProperty("spine.version.time", timeVersion)
        artifacts.setProperty("spine.version.core", coreJavaVersion)
        artifacts.setProperty("spine.version.web", webVersion)
        artifacts.setProperty("spine.version.gcloud", gCloudVersion)
        artifacts.setProperty("protobuf.compiler", Protobuf.compiler)
        artifacts.setProperty("protobuf.java", Protobuf.libs[0])
        artifacts.setProperty("grpc.stub", Grpc.stub)
        artifacts.setProperty("grpc.protobuf", Grpc.protobuf)
        artifacts.setProperty(
            "repository.spine.release",
            cloudArtifactRegistry.releases
        )
        artifacts.setProperty(
            "repository.spine.snapshot",
            cloudArtifactRegistry.snapshots
        )

        FileWriter(versionSnapshot).use {
            artifacts.store(it, "Dependencies and versions required by Spine.")
        }
    }
}

tasks.processResources {
    dependsOn(writeDependencies)
}

/**
 * Publish the plugin at Gradle Plugin Portal only if it's not a snapshot version.
 *
 * See rules for publishing in the Gradle documentation
 * ["How do I publish my plugin to the Plugin Portal?"](https://plugins.gradle.org/docs/publish-plugin).
 */
val publishPlugins: Task by tasks.getting {
    enabled = !bootstrapVersion.isSnapshot()
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
