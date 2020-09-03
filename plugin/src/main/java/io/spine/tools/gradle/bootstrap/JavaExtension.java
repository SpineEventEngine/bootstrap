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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import groovy.lang.Closure;
import io.spine.tools.gradle.Artifact;
import io.spine.tools.gradle.ConfigurationName;
import io.spine.tools.gradle.GeneratedSourceRoot;
import io.spine.tools.gradle.compiler.Extension;
import io.spine.tools.gradle.config.ArtifactSnapshot;
import io.spine.tools.gradle.config.SpineDependency;
import io.spine.tools.gradle.project.SourceSuperset;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.io.File;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.implementation;
import static io.spine.tools.gradle.ConfigurationName.testImplementation;
import static io.spine.tools.gradle.ProtobufDependencies.protobufLite;
import static io.spine.tools.gradle.config.SpineDependency.testUtilTime;
import static io.spine.tools.gradle.config.SpineDependency.testlib;
import static io.spine.tools.gradle.protoc.ProtocPlugin.Name.java;
import static io.spine.tools.gradle.protoc.ProtocPlugin.called;
import static org.gradle.util.ConfigureUtil.configure;

/**
 * An extension which configures Java code generation.
 */
public final class JavaExtension extends CodeGenExtension {

    private final Project project;
    private final SourceSuperset directoryStructure;
    private final JavaCodegenExtension codegen;
    private final ArtifactSnapshot artifacts;

    private JavaExtension(Builder builder) {
        super(builder);
        this.project = builder.project();
        this.directoryStructure = builder.sourceSuperset();
        this.artifacts = builder.artifactSnapshot();
        this.codegen = JavaCodegenExtension.of(project, dependant(), artifacts);
    }

    @Override
    void enableGeneration() {
        super.enableGeneration();
        dependOn(testlib().ofVersion(artifacts.spineBaseVersion()), testImplementation);
        dependOn(testUtilTime().ofVersion(artifacts.spineTimeVersion()), testImplementation);
        pluginTarget().applyModelCompiler();
        pluginTarget().apply(SpinePluginScripts.modelCompilerConfig());
        addSourceSets();
        excludeProtobufLite();
        pluginTarget().withIdeaPlugin(this::configureIdea);
    }

    private void configureIdea(IdeaModel idea) {
        IdeaModule module = idea.getModule();

        Set<File> mainSrc = module.getSourceDirs();
        Set<File> mainGenerated = module.getGeneratedSourceDirs();
        add(mainSrc, Extension.getMainProtoSrcDir(project));
        add(mainGenerated, Extension.getMainGenProtoDir(project));
        add(mainGenerated, Extension.getMainGenGrpcDir(project));
        add(mainGenerated, Extension.getTargetGenColumnsRootDir(project));
        add(mainGenerated, Extension.getTargetGenRejectionsRootDir(project));

        Set<File> testSrc = module.getTestSourceDirs();
        add(testSrc, Extension.getTestProtoSrcDir(project));
        add(testSrc, Extension.getTestGenProtoDir(project));
        add(testSrc, Extension.getTestGenGrpcDir(project));

        module.setDownloadJavadoc(true);
        module.setDownloadSources(true);
    }

    private static void add(Set<File> files, String path) {
        File file = new File(path);
        files.add(file);
    }

    private void excludeProtobufLite() {
        dependant().exclude(protobufLite());
    }

    public JavaCodegenExtension getCodegen() {
        return codegen;
    }

    public void codegen(Action<JavaCodegenExtension> config) {
        config.execute(codegen);
    }

    public void codegen(@SuppressWarnings("rawtypes") /* For Gradle API. */ Closure config) {
        configure(config, codegen);
    }

    /**
     * Marks this project as a Java client of the system.
     *
     * <p>Adds the {@code io.spine:spine-client} and {@code io.spine:spine-testuil-client}
     * dependencies to the project.
     */
    public void client() {
        dependOnCore(SpineDependency.client(), implementation);
        dependOnCore(SpineDependency.testUtilClient(), testImplementation);
    }

    /**
     * Marks this project as a part of a Java server.
     *
     * <p>Adds the {@code io.spine:spine-server} and {@code io.spine:spine-testutil-server}
     * dependencies to the project.
     */
    public void server() {
        dependOnCore(SpineDependency.server(), implementation);
        dependOnCore(SpineDependency.testUtilServer(), testImplementation);
    }

    /**
     * Marks this project as a part of a Java server and a Web server.
     *
     * <p>Additionally to the {@linkplain #server() server} dependencies, adds a dependency on
     * {@code io.spine:spine-web}.
     */
    public void webServer() {
        dependOnWeb(SpineDependency.web());
    }

    /**
     * Marks this project as a part of a Java server and a Web server based on the Firebase RDB.
     *
     * <p>Additionally to the {@linkplain #server() server} dependencies, adds a dependency on
     * {@code io.spine.gcloud:spine-firebase-web}.
     */
    public void firebaseWebServer() {
        dependOnWeb(SpineDependency.firebaseWeb());
    }

    /**
     * Marks this project as a part of a Java server and adds the Google Cloud Datastore storage
     * dependency to the project.
     */
    public void withDatastore() {
        server();
        Artifact dependency = SpineDependency.datastore()
                                             .ofVersion(artifacts.spineGCloudVersion());
        dependOn(dependency, implementation);
    }

    private void dependOnWeb(SpineDependency dependency) {
        server();
        String version = artifacts.spineWebVersion();
        dependOn(dependency.ofVersion(version), implementation);
    }

    private void dependOn(Artifact module, ConfigurationName configurationName) {
        dependant().depend(configurationName, module.notation());
    }

    private void dependOnCore(SpineDependency module, ConfigurationName configurationName) {
        String spineVersion = artifacts.spineVersion();
        Artifact artifact = module.ofVersion(spineVersion);
        dependOn(artifact, configurationName);
    }

    private void addSourceSets() {
        GeneratedSourceRoot sourceRoot = GeneratedSourceRoot.of(project);
        directoryStructure.register(sourceRoot);
    }

    @Override
    protected ImmutableSet<String> forcedDependencies() {
        return ImmutableSet.of(protobufJavaSpec());
    }

    @VisibleForTesting
    String protobufJavaSpec() {
        return artifacts.protobufJava();
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder extends CodeGenExtension.Builder<JavaExtension, Builder> {

        private SourceSuperset sourceSuperset;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
            super(called(java));
        }

        private SourceSuperset sourceSuperset() {
            return sourceSuperset;
        }

        Builder setSourceSuperset(SourceSuperset sourceSuperset) {
            this.sourceSuperset = sourceSuperset;
            return this;
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        JavaExtension build() {
            checkNotNull(sourceSuperset);
            return super.build();
        }

        @Override
        JavaExtension doBuild() {
            return new JavaExtension(this);
        }
    }
}
