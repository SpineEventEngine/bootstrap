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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.bootstrap.ProtobufGenerator.BuiltIn.java;

public final class JavaExtension extends SubExtension {

    private final PluginTarget pluginTarget;

    private boolean generateGrpc = false;

    JavaExtension(ProtobufGenerator generator, PluginTarget pluginTarget) {
        super(generator, java);
        this.pluginTarget = checkNotNull(pluginTarget);
    }

    public boolean getGenerateGrpc() {
        return generateGrpc;
    }

    public void setGenerateGrpc(boolean generateGrpc) {
        this.generateGrpc = generateGrpc;
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    void enableGeneration() {
        super.enableGeneration();
        pluginTarget.applyModelCompiler();
    }
}
