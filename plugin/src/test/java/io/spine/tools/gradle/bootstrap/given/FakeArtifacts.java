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

package io.spine.tools.gradle.bootstrap.given;

import io.spine.tools.gradle.config.ArtifactSnapshot;

public final class FakeArtifacts {

    public static final String GRPC_PROTO_DEPENDENCY = "io.foo.bar.grpc:fake-pb-dependency:6.14";
    public static final String GRPC_STUB_DEPENDENCY = "io.foo.bar.grpc:stub-dependency:6.14";

    public static final String spineVersion = "42.3.14-AVOCADO";

    /**
     * Prevents the utility class instantiation.
     */
    private FakeArtifacts() {
    }

    public static ArtifactSnapshot snapshot() {
        return ArtifactSnapshot
                .newBuilder()
                .setSpineBaseVersion(spineVersion)
                .setSpineTimeVersion(spineVersion)
                .setSpineCoreVersion(spineVersion)
                .setSpineWebVersion(spineVersion)
                .setSpineGCloudVersion(spineVersion)
                .setGrpcProtobuf(GRPC_PROTO_DEPENDENCY)
                .setGrpcStub(GRPC_STUB_DEPENDENCY)
                .setProtoc("com.google.protobuf:protoc:3.6.1")
                .setProtobufJava("com.google.protobuf:protobuf-java:3.6.1")
                .setSpineRepository("http://fake.maven.repo.org/releases")
                .setSpineSnapshotRepository("http://fake.maven.repo.org/snapshots")
                .build();
    }
}
