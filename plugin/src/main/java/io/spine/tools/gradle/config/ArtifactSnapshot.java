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

    private final String spineBaseVersion;
    private final String spineTimeVersion;
    private final String spineCoreVersion;
    private final String spineWebVersion;
    private final String spineGCloudVersion;

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
        this.spineBaseVersion = checkNotNull(builder.spineBaseVersion);
        this.spineTimeVersion = checkNotNull(builder.spineTimeVersion);
        this.spineCoreVersion = checkNotNull(builder.spineCoreVersion);
        this.spineWebVersion = checkNotNull(builder.spineWebVersion);
        this.spineGCloudVersion = checkNotNull(builder.spineGCloudVersion);
        this.protoc = checkNotNull(builder.protoc);
        this.protobufJava = checkNotNull(builder.protobufJava);
        this.grpcProtobuf = checkNotNull(builder.grpcProtobuf);
        this.grpcStub = checkNotNull(builder.grpcStub);
        this.spineRepository = checkNotNull(builder.spineRepository);
        this.spineSnapshotRepository = checkNotNull(builder.spineSnapshotRepository);
    }

    /**
     * Loads the values from the {@code artifact-snapshot.properties} file from classpath.
     *
     * <p>The keys for the {@code artifact-snapshot.properties} file are duplicated in
     * the {@code prepare-config-resources.gradle.kts} Gradle script, where the file is generated.
     *
     * @return loaded {@code ArtifactSnapshot}
     */
    private static ArtifactSnapshot load() {
        Resource file = Resource.file(
                "artifact-snapshot.properties", ArtifactSnapshot.class.getClassLoader()
        );
        Properties properties = new Properties();
        try (InputStream resource = file.open()) {
            properties.load(resource);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
        ArtifactSnapshot snapshot = newBuilder()
                .setSpineCoreVersion(properties.getProperty("spine.version.core"))
                .setSpineBaseVersion(properties.getProperty("spine.version.base"))
                .setSpineTimeVersion(properties.getProperty("spine.version.time"))
                .setSpineWebVersion(properties.getProperty("spine.version.web"))
                .setSpineGCloudVersion(properties.getProperty("spine.version.gcloud"))
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
     * Obtains the current version of Spine core.
     */
    public String spineVersion() {
        return spineCoreVersion;
    }

    /**
     * Obtains the current version of Spine {@code base}.
     */
    public String spineBaseVersion() {
        return spineBaseVersion;
    }

    /**
     * Obtains the current version of Spine {@code time}.
     */
    public String spineTimeVersion() {
        return spineTimeVersion;
    }

    /**
     * Obtains the current version of Spine {@code web} API.
     */
    public String spineWebVersion() {
        return spineWebVersion;
    }

    /**
     * Obtains the current version of Spine GCloud.
     */
    public String spineGCloudVersion() {
        return spineGCloudVersion;
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

        private String spineBaseVersion;
        private String spineTimeVersion;
        private String spineCoreVersion;
        private String spineWebVersion;
        private String spineGCloudVersion;
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

        public Builder setSpineBaseVersion(String spineBaseVersion) {
            this.spineBaseVersion = checkNotNull(spineBaseVersion);
            return this;
        }

        public Builder setSpineTimeVersion(String spineTimeVersion) {
            this.spineTimeVersion = checkNotNull(spineTimeVersion);
            return this;
        }

        public Builder setSpineCoreVersion(String version) {
            this.spineCoreVersion = checkNotNull(version);
            return this;
        }

        public Builder setSpineWebVersion(String spineWebVersion) {
            this.spineWebVersion = checkNotNull(spineWebVersion);
            return this;
        }

        public Builder setSpineGCloudVersion(String spineGCloudVersion) {
            this.spineGCloudVersion = checkNotNull(spineGCloudVersion);
            return this;
        }

        public Builder setProtoc(String artifact) {
            this.protoc = checkNotNull(artifact);
            return this;
        }

        public Builder setProtobufJava(String artifact) {
            this.protobufJava = checkNotNull(artifact);
            return this;
        }

        public Builder setGrpcProtobuf(String artifact) {
            this.grpcProtobuf = checkNotNull(artifact);
            return this;
        }

        public Builder setGrpcStub(String artifact) {
            this.grpcStub = checkNotNull(artifact);
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
