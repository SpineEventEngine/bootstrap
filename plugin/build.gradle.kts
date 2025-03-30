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

import io.spine.dependency.lib.Kotlin
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Base
import io.spine.dependency.local.BaseTypes
import io.spine.dependency.local.ModelCompiler
import io.spine.dependency.local.TestLib
import io.spine.dependency.local.ToolBase
import io.spine.dependency.local.Validation
import io.spine.dependency.test.JUnit
import io.spine.gradle.isSnapshot
import io.spine.gradle.publish.IncrementGuard
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    module
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish").version("1.2.1")
//    id("com.github.johnrengelman.shadow")
    `prepare-config-resources`
    `version-to-resources`
    `write-manifest`
    idea
}

apply<IncrementGuard>()

@Suppress(
    "UnstableApiUsage" /* testing suites feature */
)
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(JUnit.version)
            dependencies {
                implementation(Kotlin.GradlePlugin.lib)
                implementation(gradleKotlinDsl())
                implementation(Protobuf.GradlePlugin.lib)
                implementation(ToolBase.pluginBase)
                implementation(ToolBase.pluginTestlib)
            }
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter(JUnit.version)
            dependencies {
                implementation(Kotlin.GradlePlugin.lib)
                implementation(Kotlin.testJUnit5)
                implementation(ToolBase.pluginBase)
                implementation(TestLib.lib)
                implementation(ToolBase.pluginTestlib)
                implementation(project(":plugin"))
            }
        }
    }
}

dependencies {
    compileOnlyApi(gradleApi())
    compileOnlyApi(Protobuf.GradlePlugin.lib)
    implementation(Base.lib)
    implementation(BaseTypes.lib)
    implementation(Validation.runtime)
    implementation(ToolBase.pluginTestlib)
    implementation(ModelCompiler.lib)

    testImplementation(TestLib.lib)
    testImplementation(ToolBase.pluginTestlib)
}

/**
 * Make functional tests depend on publishing all the submodules to Maven Local so that
 * the Gradle plugin can get all the dependencies when it's applied to the test projects.
 */
val functionalTest: Task by tasks.getting {
    val task = this
    productionModules.forEach { subproject ->
        task.dependsOn(":${subproject.name}:publishToMavenLocal")
    }
}

val spineVersion: String by extra
val spineBaseVersion: String by extra
val pluginVersion: String by extra

val targetResourceDir = layout.buildDirectory.dir("compiledResources/").get()

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

gradlePlugin {
    website.set("https://spine.io/")
    vcsUrl.set("https://github.com/SpineEventEngine/ProtoData.git")
    plugins {
        create("spineBootstrapPlugin") {
            id = "io.spine.bootstrap"
            implementationClass = "io.spine.tools.gradle.bootstrap.BootstrapPlugin"
            displayName = "Spine Bootstrap Gradle Plugin"
            description = "Prepares a Gradle project for development on Spine SDK."
            tags.set(listOf("spine", "event-sourcing", "ddd", "cqrs", "bootstrap"))
        }
    }
    val functionalTest by sourceSets.getting
    testSourceSets(
        functionalTest
    )
}

val bootstrapVersion: String by extra

val publishPlugins: Task by tasks.getting {
    enabled = !bootstrapVersion.isSnapshot()
}

val publish: Task by tasks.getting {
    dependsOn(publishPlugins)
}

tasks {
    check {
        dependsOn(testing.suites.named("functionalTest"))
    }

    ideaModule {
        notCompatibleWithConfigurationCache("https://github.com/gradle/gradle/issues/13480")
    }

    publishPlugins {
        notCompatibleWithConfigurationCache("https://github.com/gradle/gradle/issues/21283")
    }
}

/**
 * Do it here because the call in `subprojects` does not have effect on the dependency
 * of the `publishPluginJar` on `createVersionFile`.
 */
afterEvaluate {
    configureTaskDependencies()

    val writeDependencies by tasks.getting
    val sourcesJar by tasks.getting {
        dependsOn(writeDependencies)
    }
}
