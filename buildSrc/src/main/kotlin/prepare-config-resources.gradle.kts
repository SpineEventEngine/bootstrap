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

import java.util.Properties
import java.io.FileWriter

import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Grpc
import io.spine.internal.gradle.publish.PublishingRepos.cloudArtifactRegistry

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

val baseVersion: String by extra
val timeVersion: String by extra
val bootstrapVersion: String by extra
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
    group = taskGroup

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
        artifacts.setProperty("repository.spine.release",
            io.spine.internal.gradle.Repos.spine
//            cloudArtifactRegistry.releases
        )
        artifacts.setProperty("repository.spine.snapshot",
            io.spine.internal.gradle.Repos.spineSnapshots
//            cloudArtifactRegistry.snapshots
        )

        FileWriter(versionSnapshot).use {
            artifacts.store(it, "Dependencies and versions required by Spine.")
        }
    }
}

tasks.processResources {
    dependsOn(writeDependencies)
}
