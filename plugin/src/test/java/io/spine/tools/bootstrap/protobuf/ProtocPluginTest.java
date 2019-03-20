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

package io.spine.tools.bootstrap.protobuf;

import com.google.protobuf.gradle.GenerateProtoTask.PluginOptions;
import io.spine.tools.bootstrap.protobuf.ProtocPlugin.Name;
import io.spine.tools.bootstrap.protobuf.given.TestPluginOptionsContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.bootstrap.protobuf.ProtocPlugin.Name.java;
import static io.spine.tools.bootstrap.protobuf.ProtocPlugin.Name.js;

@DisplayName("ProtocPlugin should")
class ProtocPluginTest {

    @Test
    @DisplayName("add/remove a simple plugin")
    void withoutOption() {
        TestPluginOptionsContainer options = new TestPluginOptionsContainer();
        Name name = js;
        ProtocPlugin plugin = ProtocPlugin.called(name);
        plugin.createIn(options);
        assertThat(options).contains(new PluginOptions(name.name()));
        plugin.removeFrom(options);
        assertThat(options).isEmpty();
    }

    @Test
    @DisplayName("add/remove a plugin with an option")
    void withOption() {
        TestPluginOptionsContainer options = new TestPluginOptionsContainer();
        Name name = java;
        String option = "test-option";
        ProtocPlugin plugin = ProtocPlugin.called(name);
        plugin.createIn(options);
        PluginOptions pluginOptions = new PluginOptions(name.name());
        pluginOptions.option(option);
        assertThat(options).contains(pluginOptions);
        plugin.removeFrom(options);
        assertThat(options).isEmpty();
    }
}