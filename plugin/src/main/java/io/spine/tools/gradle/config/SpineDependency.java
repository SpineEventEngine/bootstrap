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

import io.spine.tools.gradle.Dependency;

/**
 * A {@link Dependency} on a Spine module.
 */
public final class SpineDependency implements Dependency {

    private static final String DEFAULT_GROUP = "io.spine";
    private static final String G_CLOUD_GROUP = DEFAULT_GROUP + ".gcloud";

    private static final String SPINE_PREFIX = "spine-";

    private static final SpineDependency BASE =
            new SpineDependency("base");
    private static final SpineDependency TIME =
            new SpineDependency("time");
    private static final SpineDependency CLIENT =
            new SpineDependency("client");
    private static final SpineDependency SERVER =
            new SpineDependency("server");
    private static final SpineDependency TEST_UTIL_SERVER =
            new SpineDependency("testutil-server");
    private static final SpineDependency TEST_UTIL_CLIENT =
            new SpineDependency("testutil-client");
    private static final SpineDependency TESTLIB =
            new SpineDependency("testlib");
    private static final SpineDependency TEST_UTIL_TIME =
            new SpineDependency("testutil-time");
    private static final SpineDependency WEB =
            new SpineDependency("web");
    private static final SpineDependency FIREBASE_WEB =
            new SpineDependency(G_CLOUD_GROUP, "firebase-web");
    private static final SpineDependency DATASTORE =
            new SpineDependency(G_CLOUD_GROUP, "datastore");

    private final String group;
    private final String shortName;

    private SpineDependency(String name) {
        this(DEFAULT_GROUP, name);
    }

    private SpineDependency(String group, String name) {
        this.group = group;
        this.shortName = name;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-base} module.
     */
    public static SpineDependency base() {
        return BASE;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-base} module.
     */
    public static SpineDependency time() {
        return TIME;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-client} module.
     */
    public static SpineDependency client() {
        return CLIENT;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-server} module.
     */
    public static SpineDependency server() {
        return SERVER;
    }

    /**
     * Obtains a dependency on the {@code io.spine:testutil-server} module.
     */
    public static SpineDependency testUtilServer() {
        return TEST_UTIL_SERVER;
    }

    /**
     * Obtains a dependency on the {@code io.spine:testutil-client} module.
     */
    public static SpineDependency testUtilClient() {
        return TEST_UTIL_CLIENT;
    }

    /**
     * Obtains a dependency on the {@code io.spine:testlib} module.
     */
    public static SpineDependency testlib() {
        return TESTLIB;
    }

    /**
     * Obtains a dependency on the {@code io.spine:testutil-time} module.
     */
    public static SpineDependency testUtilTime() {
        return TEST_UTIL_TIME;
    }

    public static SpineDependency web() {
        return WEB;
    }

    public static SpineDependency firebaseWeb() {
        return FIREBASE_WEB;
    }

    public static SpineDependency datastore() {
        return DATASTORE;
    }

    @Override
    public String name() {
        return SPINE_PREFIX + shortName;
    }

    @Override
    public String groupId() {
        return group;
    }

    @Override
    public String toString() {
        return groupId() + ':' + name();
    }
}
