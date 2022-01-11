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

import io.spine.gradle.internal.Deps
import io.spine.gradle.internal.IncrementGuard
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("com.gradle.plugin-publish").version("0.12.0")
    id("com.github.johnrengelman.shadow").version("6.1.0")
    `bootstrap-plugin`
    `prepare-config-resources`
}

apply<IncrementGuard>()

val spineVersion: String by extra
val spineBaseVersion: String by extra
val pluginVersion: String by extra

dependencies {
    implementation(gradleApi())
    implementation(Deps.build.gradlePlugins.protobuf)
    implementation("io.spine:spine-base:$spineBaseVersion")
    implementation("io.spine.tools:spine-plugin-base:$spineBaseVersion")
    implementation("io.spine.tools:spine-model-compiler:$spineBaseVersion")
    implementation("io.spine.tools:spine-proto-js-plugin:$spineBaseVersion")
    implementation("io.spine.tools:spine-proto-dart-plugin:$spineBaseVersion")

    testImplementation("io.spine:spine-testlib:$spineBaseVersion")
    testImplementation("io.spine.tools:spine-plugin-testlib:$spineBaseVersion")
}

val targetResourceDir = "$buildDir/compiledResources/"

val prepareBuildScript by tasks.registering(Copy::class) {
    description = "Creates the `build.gradle` script which is executed " +
            "in functional tests of the plugin."

    from("$projectDir/src/test/build.gradle.template")
    into(targetResourceDir)

    rename { "build.gradle" }
    filter(mapOf("tokens" to mapOf("spine-version" to spineVersion)), ReplaceTokens::class.java)
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
        version = pluginVersion
    }

    withDependencies { clear() }

    plugins {
        named("spineBootstrapPlugin") {
            version = pluginVersion
        }
    }
}

/*
 * In order to simplify the Bootstrap plugin usage, the plugin should have no external dependencies
 * which cannot be found in the Plugin portal or in JCenter. Spine core modules are not published to
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
}

artifacts {
    archives(tasks.shadowJar)
}
