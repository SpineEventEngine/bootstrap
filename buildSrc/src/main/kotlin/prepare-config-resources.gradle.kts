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
import io.spine.gradle.internal.Repos
import java.util.Properties
import java.io.FileWriter

plugins {
    java
}

val bootstrapDir = file("$buildDir/bootstrap")
val versionSnapshot = file("$bootstrapDir/artifact-snapshot.properties")
val configDir = file("$rootDir/config")

sourceSets.main {
    resources.srcDir(bootstrapDir)
}

val taskGroup = "Spine bootstrapping"

val copyModelCompilerConfig by tasks.registering(Copy::class) {
    group = taskGroup

    from(file("$configDir/gradle/model-compiler.gradle"))
    into(file(bootstrapDir))

    doFirst {
        bootstrapDir.mkdirs()
    }
}

tasks.processResources {
    dependsOn(copyModelCompilerConfig)
}

val spineBaseVersion: String by extra
val spineTimeVersion: String by extra
val spineVersion: String by extra
val spineWebVersion: String by extra
val spineGCloudVersion: String by extra

/*
  This task creates the `artifact-snapshot.properties` file which is later added to the classpath of
  the Bootstrap plugin. The file contains versions, artifact notations, repositories, etc. used in
  the Gradle scripts which should also be used in the runtime of the Bootstrap plugin.

  The keys for the `artifact-snapshot.properties` file are duplicated in
  the `io.spine.tools.gradle.config.ArtifactSnapshot` class, where the file is parsed.
 */
val writeDependencies by tasks.registering {
    group = taskGroup

    inputs.dir(configDir)
    inputs.property("version", spineVersion)
    outputs.file(versionSnapshot)

    doFirst {
        bootstrapDir.mkdirs()
        if (!versionSnapshot.exists()) {
            versionSnapshot.createNewFile()
        }
    }

    doLast {
        val artifacts = Properties()

        artifacts.setProperty("spine.version.base", spineBaseVersion)
        artifacts.setProperty("spine.version.time", spineTimeVersion)
        artifacts.setProperty("spine.version.core", spineVersion)
        artifacts.setProperty("spine.version.web", spineWebVersion)
        artifacts.setProperty("spine.version.gcloud", spineWebVersion)
        artifacts.setProperty("protobuf.compiler", Deps.build.protoc)
        artifacts.setProperty("protobuf.java", Deps.build.protobuf[0])
        artifacts.setProperty("grpc.stub", Deps.grpc.stub)
        artifacts.setProperty("grpc.protobuf", Deps.grpc.protobuf)
        artifacts.setProperty("repository.spine.release", Repos.spine)
        artifacts.setProperty("repository.spine.snapshot", Repos.spineSnapshots)

        FileWriter(versionSnapshot).use {
            artifacts.store(it, "Dependencies and versions required by Spine.")
        }
    }
}

tasks.processResources {
    dependsOn(writeDependencies)
}
