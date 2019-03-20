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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

final class Ext {

    private final ExtraPropertiesExtension ext;

    private Ext(ExtraPropertiesExtension ext) {
        this.ext = ext;
    }

    static Ext of(Project project) {
        checkNotNull(project);

        ExtraPropertiesExtension ext = project.getExtensions()
                                              .getExtraProperties();
        checkNotNull(ext);
        return new Ext(ext);
    }

    Versions versions() {
        return new Versions();
    }

    Artifacts artifacts() {
        return new Artifacts();
    }

    final class Versions {

        String spine() {
            return Ext.this.property("spineVersion").value();
        }

        String grpc() {
            return property().subProperty("grpc").value();
        }

        String protobuf() {
            return property().subProperty("protobuf").value();
        }

        private Property property() {
            return deps().subProperty("versions");
        }
    }

    final class Artifacts {

        String protoc() {
            return property().subProperty("protoc").value();
        }

        List<String> grpc() {
            return deps().subProperty("grpc").allValues();
        }

        private Property property() {
            return deps().subProperty("build");
        }
    }

    private Property deps() {
        return property("deps");
    }

    private Property property(String name) {
        Object value = ext.get(name);
        checkNotNull(value);
        return new Property(value);
    }

    private static final class Property {

        private final Object value;

        private Property(Object value) {
            this.value = value;
        }

        private String value() {
            return value.toString();
        }

        private List<String> allValues() {
            @SuppressWarnings("unchecked") // Groovy interop.
            Map<String, ?> map = (Map<String, ?>) value;
            ImmutableList<String> result = map.values()
                                              .stream()
                                              .map(Object::toString)
                                              .collect(toImmutableList());
            return result;
        }

        private Property subProperty(String name) {
            @SuppressWarnings("unchecked") // Groovy interop.
            Map<String, ?> map = (Map<String, ?>) value;
            checkArgument(map.containsKey(name));
            Object subValue = map.get(name);
            checkNotNull(subValue);
            return new Property(subValue);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("value", value)
                              .toString();
        }
    }
}
