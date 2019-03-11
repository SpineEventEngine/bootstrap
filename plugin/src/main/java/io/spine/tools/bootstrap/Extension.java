/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.bootstrap;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.gradle.util.ConfigureUtil.configure;

public final class Extension extends BaseExtension {

    static final String NAME = "spine";

    private final JavaExtension java;
    private final JavaScriptExtension javaScript;

    private Extension(Project project, ProtobufGenerator generator) {
        super(project);
        this.java = new JavaExtension(project, generator);
        this.javaScript = new JavaScriptExtension(project, generator);
    }

    static Extension createFor(Project project) {
        checkNotNull(project);

        ProtobufGenerator generator = new ProtobufGenerator(project);
        Extension extension = new Extension(project, generator);
        extension.disableJavaGeneration();

        return extension;
    }

    public void java(Closure configuration) {
        checkNotNull(configuration);
        java();
        configure(configuration, java);
    }

    public void java(Action<JavaExtension> configuration) {
        checkNotNull(configuration);
        java();
        configuration.execute(java);
    }

    public void java() {
        java.enableGeneration();
    }

    public void javaScript() {
        javaScript.enableGeneration();
    }

    private void disableJavaGeneration() {
        java.disableGeneration();
    }
}
