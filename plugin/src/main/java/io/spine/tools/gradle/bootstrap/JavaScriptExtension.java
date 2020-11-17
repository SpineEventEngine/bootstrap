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

package io.spine.tools.gradle.bootstrap;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.tools.gradle.protoc.ProtocPlugin;

import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.js;

/**
 * An extension which configures JavaScript code generation.
 */
final class JavaScriptExtension extends CodeGenExtension {

    private static final String IMPORT_STYLE_OPTION = "import_style=commonjs";

    private JavaScriptExtension(Builder builder) {
        super(builder);
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    void enableGeneration() {
        super.enableGeneration();
        pluginTarget().applyProtoJsPlugin();
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder extends CodeGenExtension.Builder<JavaScriptExtension, Builder> {

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
            super(ProtocPlugin.withOption(js, IMPORT_STYLE_OPTION));
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        JavaScriptExtension doBuild() {
            return new JavaScriptExtension(this);
        }
    }
}
