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

package io.spine.tools.gradle;

/**
 * A factory of Protobuf-related artifact specs.
 */
public final class ProtobufArtifacts {

    private static final String GROUP_ID = "com.google.protobuf";
    private static final String PROTOBUF_LITE = "protobuf-lite";

    /**
     * Prevents the utility class instantiation.
     */
    private ProtobufArtifacts() {
    }

    /**
     * Obtains the ID of the Protobuf Gradle plugin.
     */
    public static String gradlePlugin() {
        return GROUP_ID;
    }

    /**
     * Obtains the {@link DependencyModule} of the Protobuf Lite Java runtime library.
     */
    public static DependencyModule protobufLite() {
        return new DependencyModule(GROUP_ID, PROTOBUF_LITE);
    }
}
