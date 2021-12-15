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

import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.dependency.Protobuf

import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id(io.spine.internal.dependency.Protobuf.GradlePlugin.id)

    id("com.gradle.plugin-publish").version("0.18.0")
    id("com.github.johnrengelman.shadow").version("7.1.0")
    `bootstrap-plugin`
    `prepare-config-resources`
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

pluginBundle {
    website = "https://spine.io/"
    vcsUrl = "https://github.com/SpineEventEngine/bootstrap.git"
    tags = listOf("spine", "event-sourcing", "ddd", "cqrs", "bootstrap")

    mavenCoordinates {
        groupId = "io.spine.tools"
        artifactId = "spine-bootstrap"
        version = bootstrapVersion
    }

    withDependencies { clear() }

    plugins {
        named("spineBootstrapPlugin") {
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

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
