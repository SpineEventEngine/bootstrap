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

package io.spine.tools.gradle.protoc;

import com.google.protobuf.gradle.GenerateProtoTask;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.NamedDomainObjectContainer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Protobuf compiler built-in which can be configured with the Spine plugin.
 *
 * <p>The names of the enum instances should be used as the names of the built-ins.
 */
public final class ProtocPlugin {

    private final Name name;
    private final @Nullable String option;

    private ProtocPlugin(Name name, @Nullable String option) {
        this.name = name;
        this.option = option;
    }

    public static ProtocPlugin called(Name name) {
        checkNotNull(name);
        return new ProtocPlugin(name, null);
    }

    public static ProtocPlugin withOption(Name name, String option) {
        checkNotNull(name);
        checkNotNull(option);
        return new ProtocPlugin(name, option);
    }

    public void createIn(NamedDomainObjectContainer<GenerateProtoTask.PluginOptions> plugins) {
        checkNotNull(plugins);
        GenerateProtoTask.PluginOptions options = plugins.maybeCreate(name.name());
        if (option != null) {
            options.option(option);
        }
    }

    public void removeFrom(NamedDomainObjectContainer<GenerateProtoTask.PluginOptions> plugins) {
        String name = this.name.name();
        plugins.removeIf(taskBuiltIn -> name.equals(taskBuiltIn.getName()));
    }

    /**
     * The enumeration of known protoc built-ins and plugins.
     */
    public enum Name {
        java,
        js,
        grpc,
        spineProtoc
    }
}
