/*
 * Copyright 2020, TeamDev. All rights reserved.
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

plugins {
    base
}

/*
 * Creates a configuration named `fetch`.
 *
 * The configuration is used in order to download artifacts. The artifacts are NOT added into
 * the application classpath.
 */
configurations { create("fetch") }

val spineVersion: String by extra
val spineBaseVersion: String by extra

var spineProtocPluginDependency: Dependency? = null
dependencies {
    spineProtocPluginDependency = "fetch"("io.spine.tools:spine-protoc-plugin:${spineBaseVersion}@jar")
}

val spineArtifactDir = file("$projectDir/.spine")

val downloadProtocPlugin by tasks.registering {
    description = "Downloads the Spine Protoc plugin for functional tests."

    doLast {
        val executableJar = configurations["fetch"]
                .fileCollection(spineProtocPluginDependency)
                .getSingleFile()
        spineArtifactDir.mkdirs()
        copy {
            from(executableJar)
            into(spineArtifactDir)
        }
    }

    mustRunAfter(tasks.clean)
}

tasks.withType(Test::class) {
    dependsOn(downloadProtocPlugin)
}

tasks.clean {
    delete(spineArtifactDir)
}
