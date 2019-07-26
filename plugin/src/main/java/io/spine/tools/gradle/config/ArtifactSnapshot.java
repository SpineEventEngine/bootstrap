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

package io.spine.tools.gradle.config;

import com.google.common.collect.ImmutableList;
import io.spine.io.Resource;
import io.spine.net.Url;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;

public final class ArtifactSnapshot {

    private static final ArtifactSnapshot instance = load();

    private final String spineVersion;
    private final String protoc;
    private final String grpcProtobuf;
    private final String grpcStub;

    private final Url spineRepository;
    private final Url spineSnapshotRepository;

    /**
     * Prevents direct instantiation.
     */
    private ArtifactSnapshot(Builder builder) {
        this.spineVersion = builder.spineVersion;
        this.protoc = builder.protoc;
        this.grpcProtobuf = builder.grpcProtobuf;
        this.grpcStub = builder.grpcStub;
        this.spineRepository = builder.spineRepository;
        this.spineSnapshotRepository = builder.spineSnapshotRepository;
    }

    private static ArtifactSnapshot load() {
        Resource file = Resource.file("artifact-snapshot.properties");
        Properties properties = new Properties();
        try (InputStream resource = file.open()) {
            properties.load(resource);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        ArtifactSnapshot snapshot = new Builder()
                .setSpineVersion(properties.getProperty("spine.version"))
                .setProtoc(properties.getProperty("protobuf.compiler"))
                .setGrpcProtobuf(properties.getProperty("grpc.protobuf"))
                .setGrpcStub(properties.getProperty("grpc.stub"))
                .setSpineRepository(properties.getProperty("repository.spine.release"))
                .setSpineSnapshotRepository(properties.getProperty("repository.spine.snapshot"))
                .build();
        return snapshot;
    }

    public static ArtifactSnapshot fromResources() {
        return instance;
    }

    public String spineVersion() {
        return spineVersion;
    }

    public Url spineRepository() {
        return spineRepository;
    }

    public Url spineSnapshotRepository() {
        return spineSnapshotRepository;
    }

    public String protoc() {
        return protoc;
    }

    public ImmutableList<String> grpcDependencies() {
        return ImmutableList.of(grpcProtobuf, grpcStub);
    }

    /**
     * A builder for the {@code ArtifactsSnapshot} instances.
     */
    private static final class Builder {

        private String spineVersion;
        private String protoc;
        private String grpcProtobuf;
        private String grpcStub;
        private Url spineRepository;
        private Url spineSnapshotRepository;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        private Builder setSpineVersion(String version) {
            this.spineVersion = version;
            return this;
        }

        public Builder setProtoc(String artifact) {
            this.protoc = artifact;
            return this;
        }

        private Builder setGrpcProtobuf(String artifact) {
            this.grpcProtobuf = artifact;
            return this;
        }

        private Builder setGrpcStub(String artifact) {
            this.grpcStub = artifact;
            return this;
        }

        private Builder setSpineRepository(String repositoryUrl) {
            this.spineRepository = Url
                    .newBuilder()
                    .setSpec(repositoryUrl)
                    .build();
            return this;
        }

        private Builder setSpineSnapshotRepository(String repositoryUrl) {
            this.spineRepository = Url
                    .newBuilder()
                    .setSpec(repositoryUrl)
                    .build();
            return this;
        }

        /**
         * Creates a new instance of {@code ArtifactsSnapshot}.
         *
         * @return new instance of {@code ArtifactsSnapshot}
         */
        public ArtifactSnapshot build() {
            return new ArtifactSnapshot(this);
        }
    }
}
