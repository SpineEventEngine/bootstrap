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

import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Accesses the project's {@code ext} values.
 */
public final class Ext {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in other contexts.
    private static final String SPINE_VERSION = "spineVersion";
    private static final String GRPC = "grpc";
    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in other contexts.
    private static final String PROTOC = "protoc";
    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in other contexts.
    private static final String BUILD = "build";
    private static final String DEPS = "deps";

    private final ExtraPropertiesExtension ext;

    private Ext(ExtraPropertiesExtension ext) {
        this.ext = ext;
    }

    /**
     * Creates an instance of {@code Ext} for the given project.
     */
    public static Ext of(Project project) {
        checkNotNull(project);

        ExtraPropertiesExtension ext = project.getExtensions()
                                              .getExtraProperties();
        checkNotNull(ext);
        return new Ext(ext);
    }

    /**
     * Accesses the versions of the project dependencies.
     *
     * <p>These versions are defined in {@code deps.versions} and in the {@code versions.gradle}
     * file.
     */
    public Versions versions() {
        return new Versions();
    }

    /**
     * Accesses the project dependency artifacts.
     *
     * <p>These artifacts are defined in {@code deps.build} and {@code deps.grpc}.
     */
    public Artifacts artifacts() {
        return new Artifacts();
    }

    public final class Versions {

        /**
         * Obtains the version of the Spine framework.
         */
        public String spine() {
            return Ext.this.property(SPINE_VERSION).value();
        }
    }

    public final class Artifacts {

        /**
         * Obtains the artifact spec for the Protobuf compiler.
         */
        public String protoc() {
            return property().subProperty(PROTOC).value();
        }

        /**
         * Obtains artifact specs for the gRPC Java runtime.
         */
        public List<String> grpc() {
            return deps().subProperty(GRPC).allValues();
        }

        private Property property() {
            return deps().subProperty(BUILD);
        }
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass") // Logically belongs to the upper level.
    private Property deps() {
        return property(DEPS);
    }

    private Property property(String name) {
        Object value = ext.get(name);
        checkNotNull(value);
        return new Property(value);
    }

    /**
     * A project extra property.
     */
    private static final class Property {

        private final Object value;

        private Property(Object value) {
            this.value = value;
        }

        /**
         * Obtains the string value of the property.
         */
        private String value() {
            return value.toString();
        }

        /**
         * Obtains all the values of this map property.
         */
        private List<String> allValues() {
            @SuppressWarnings("unchecked") // Groovy interop.
            Map<String, ?> map = (Map<String, ?>) value;
            ImmutableList<String> result = map.values()
                                              .stream()
                                              .map(Object::toString)
                                              .collect(toImmutableList());
            return result;
        }

        /**
         * Obtains a sub-property of this map property.
         */
        private Property subProperty(String name) {
            @SuppressWarnings("unchecked") // Groovy interop.
            Map<String, ?> map = (Map<String, ?>) value;
            checkArgument(map.containsKey(name));
            Object subValue = map.get(name);
            checkNotNull(subValue);
            return new Property(subValue);
        }
    }
}
