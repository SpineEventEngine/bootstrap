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

import Bootstrap_plugin_gradle.Nodes.findAll
import Bootstrap_plugin_gradle.Nodes.findFirst
import org.w3c.dom.Node
import org.w3c.dom.NodeList

plugins {
    java
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("spineBootstrapPlugin") {
            id = "io.spine.tools.gradle.bootstrap"
            implementationClass = "io.spine.tools.gradle.bootstrap.BootstrapPlugin"
            displayName = "Spine Bootstrap"
            description = "Prepares a Gradle project for development on Spine."
        }
    }
}

/**
 * Utilities for working with DOM nodes.
 */
object Nodes {

    /**
     * Finds the first node with the given name in this `NodeList`.
     *
     * @return a node with the given name or `null` if this list does not contain such a node
     */
    fun NodeList.findFirst(name: String): Node? {
        for (i in (0 until length)) {
            val child = item(i)
            if (child.nodeName == name) {
                return child
            }
        }
        return null
    }

    /**
     * Finds all the nodes with the given name in this `NodeList`.
     *
     * @return a list of all the nodes with a given name; an empty list if this `NodeList` does not
     * contain such nodes
     */
    fun NodeList.findAll(name: String): List<Node> {
        val nodes = mutableListOf<Node>()
        for (i in (0 until length)) {
            val child = item(i)
            if (child.nodeName == name) {
                nodes.add(child)
            }
        }
        return nodes
    }
}

project.afterEvaluate {
    tasks.withType(GenerateMavenPom::class) {
        pom.withXml {
            val root = asElement()
            val children = root.childNodes
            val dependenciesNode = children.findFirst("dependencies")
            dependenciesNode?.apply {
                childNodes.findAll("dependency").forEach { removeChild(it) }
            }
        }
    }
}
