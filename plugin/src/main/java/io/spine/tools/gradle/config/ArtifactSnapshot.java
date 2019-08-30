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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * A snapshot of Spine-related dependencies which may be used in Spine-based projects.
 *
 * <p>In order to guarantee absence of collisions, the plugin uses the same versions, repositories,
 * artifacts, etc. as does the Spine core code.
 */
public final class ArtifactSnapshot {

    private static final ArtifactSnapshot instance = load();

    private final String spineVersion;
    private final String protoc;
    private final String protobufJava;
    private final String grpcProtobuf;
    private final String grpcStub;

    private final Url spineRepository;
    private final Url spineSnapshotRepository;

    /**
     * Prevents direct instantiation.
     */
    private ArtifactSnapshot(Builder builder) {
        this.spineVersion = checkNotNull(builder.spineVersion);
        this.protoc = checkNotNull(builder.protoc);
        this.protobufJava = checkNotNull(builder.protobufJava);
        this.grpcProtobuf = checkNotNull(builder.grpcProtobuf);
        this.grpcStub = checkNotNull(builder.grpcStub);
        this.spineRepository = checkNotNull(builder.spineRepository);
        this.spineSnapshotRepository = checkNotNull(builder.spineSnapshotRepository);
    }

    private static ArtifactSnapshot load() {
        Resource file = Resource.file("artifact-snapshot.properties");
        Properties properties = new Properties();
        try (InputStream resource = file.open()) {
            properties.load(resource);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        ArtifactSnapshot snapshot = newBuilder()
                .setSpineVersion(properties.getProperty("spine.version"))
                .setProtoc(properties.getProperty("protobuf.compiler"))
                .setProtobufJava(properties.getProperty("protobuf.java"))
                .setGrpcProtobuf(properties.getProperty("grpc.protobuf"))
                .setGrpcStub(properties.getProperty("grpc.stub"))
                .setSpineRepository(properties.getProperty("repository.spine.release"))
                .setSpineSnapshotRepository(properties.getProperty("repository.spine.snapshot"))
                .build();
        return snapshot;
    }

    /**
     * Loads the snapshot from the {@code artifact-snapshot.properties} resource of the plugin.
     *
     * <p>The resource is generated on the plugin build time.
     */
    public static ArtifactSnapshot fromResources() {
        return instance;
    }

    /**
     * Obtains the current version of Spine.
     */
    public String spineVersion() {
        return spineVersion;
    }

    /**
     * Obtains the Maven repository which hosts Spine artifacts with release versions.
     */
    public Url spineRepository() {
        return spineRepository;
    }

    /**
     * Obtains the Maven repository which hosts Spine artifacts with {@code -SNAPSHOT} versions.
     */
    public Url spineSnapshotRepository() {
        return spineSnapshotRepository;
    }

    /**
     * Obtains the artifact spec for the Protobuf compiler.
     *
     * <p>The value may be something similar to {@code com.google.protobuf:protoc:3.9.0}.
     */
    public String protoc() {
        return protoc;
    }

    /**
     * Obtains the artifact spec for the core Protobuf Java library.
     *
     * <p>The value may be something similar to {@code com.google.protobuf:protobuf-java:3.9.0}.
     */
    public String protobufJava() {
        return protobufJava;
    }

    /**
     * Obtains the artifacts required to generate and compile gRPC stubs successfully.
     *
     * <p>Note that gRPC may require some additional dependencies at runtime.
     */
    public ImmutableList<String> grpcDependencies() {
        return ImmutableList.of(grpcProtobuf, grpcStub);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for the {@code ArtifactsSnapshot} instances.
     */
    public static final class Builder {

        private String spineVersion;
        private String protoc;
        private String protobufJava;
        private String grpcProtobuf;
        private String grpcStub;
        private Url spineRepository;
        private Url spineSnapshotRepository;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        public Builder setSpineVersion(String version) {
            this.spineVersion = version;
            return this;
        }

        public Builder setProtoc(String artifact) {
            this.protoc = artifact;
            return this;
        }

        public Builder setProtobufJava(String artifact) {
            this.protobufJava = artifact;
            return this;
        }

        public Builder setGrpcProtobuf(String artifact) {
            this.grpcProtobuf = artifact;
            return this;
        }

        public Builder setGrpcStub(String artifact) {
            this.grpcStub = artifact;
            return this;
        }

        public Builder setSpineRepository(String repositoryUrl) {
            this.spineRepository = Url
                    .newBuilder()
                    .setSpec(repositoryUrl)
                    .build();
            return this;
        }

        public Builder setSpineSnapshotRepository(String repositoryUrl) {
            this.spineSnapshotRepository = Url
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
