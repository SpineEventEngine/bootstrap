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

import com.google.common.collect.ImmutableSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.net.URL;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

final class PluginScript implements Plugin<Project> {

    private final Name resourceName;

    static ImmutableSet<PluginScript> all() {
        return Stream.of(Name.values())
                     .map(PluginScript::new)
                     .collect(toImmutableSet());
    }

    private PluginScript(Name resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public void apply(Project target) {
        target.apply(config -> config.from(resourceName.url()));
    }

    enum Name {

        dependencies,
        version;

        private static final String SCRIPT_EXTENSION = ".gradle";

        private URL url() {
            String resourceName = name() + SCRIPT_EXTENSION;
            URL resource = PluginScript.class.getClassLoader()
                                             .getResource(resourceName);
            checkNotNull(resource, "Resource `%s` not found.", resourceName);
            return resource;
        }
    }
}
