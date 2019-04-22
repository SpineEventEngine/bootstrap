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

package io.spine.tools.gradle.bootstrap;

import io.spine.tools.gradle.compiler.Extension;
import io.spine.tools.gradle.config.Ext;
import io.spine.tools.gradle.project.Dependant;
import io.spine.tools.gradle.protoc.ProtobufGenerator;
import io.spine.tools.gradle.protoc.ProtocPlugin;
import io.spine.tools.gradle.protoc.ProtocPlugin.Name;
import org.gradle.api.Project;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.protoc.ProtocPlugin.called;

public final class JavaCodegenExtension {

    private static final ProtocPlugin JAVA_PLUGIN = called(Name.java);
    private static final ProtocPlugin GRPC_PLUGIN = called(Name.grpc);
    private static final ProtocPlugin SPINE_PLUGIN = called(Name.spineProtoc);

    private final Project project;
    private final ProtobufGenerator protobufGenerator;
    private final Dependant dependant;

    private boolean protobuf = true;
    private boolean grpc = false;
    private boolean spine = false;

    private JavaCodegenExtension(Project project,
                                 ProtobufGenerator protobufGenerator,
                                 Dependant dependant) {
        this.project = project;
        this.protobufGenerator = protobufGenerator;
        this.dependant = dependant;
    }

    public static JavaCodegenExtension of(Project project, Dependant dependant) {
        checkNotNull(project);
        checkNotNull(dependant);
        ProtobufGenerator generator = new ProtobufGenerator(project);
        return new JavaCodegenExtension(project, generator, dependant);
    }

    public boolean getProtobuf() {
        return protobuf;
    }

    public boolean getGrpc() {
        return grpc;
    }

    public boolean getSpine() {
        return spine;
    }

    public void setProtobuf(boolean protobuf) {
        this.protobuf = protobuf;
        if (protobuf) {
            protobufGenerator.enableBuiltIn(JAVA_PLUGIN);
        } else {
            protobufGenerator.disableBuiltIn(JAVA_PLUGIN);
        }
    }

    public void setGrpc(boolean grpc) {
        this.grpc = grpc;
        switchPlugin(GRPC_PLUGIN, grpc);
        if (grpc) {
            addGrpcDependencies();
        }
    }

    private void addGrpcDependencies() {
        Ext.of(project)
           .artifacts()
           .grpc()
           .forEach(dependant::implementation);
    }

    public void setSpine(boolean spine) {
        this.spine = spine;
        switchPlugin(SPINE_PLUGIN, spine);
        Extension modelCompilerExtension = project.getExtensions()
                                                  .getByType(Extension.class);
        modelCompilerExtension.generateValidatingBuilders = spine;
    }

    private void switchPlugin(ProtocPlugin plugin, boolean enabled) {
        if (enabled) {
            protobufGenerator.enablePlugin(plugin);
        } else {
            protobufGenerator.disablePlugin(plugin);
        }
    }
}
