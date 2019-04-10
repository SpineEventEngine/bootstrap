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
 * A {@link Dependency} on a Spine module.
 */
public final class SpineDependency implements Dependency {

    private static final String SPINE_PREFIX = "spine-";

    private static final SpineDependency base = new SpineDependency("base");
    private static final SpineDependency client = new SpineDependency("client");
    private static final SpineDependency server = new SpineDependency("server");

    private final String shortName;

    private SpineDependency(String name) {
        this.shortName = name;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-base} module.
     */
    public static SpineDependency base() {
        return base;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-client} module.
     */
    public static SpineDependency client() {
        return client;
    }

    /**
     * Obtains a dependency on the {@code io.spine:spine-server} module.
     */
    public static SpineDependency server() {
        return server;
    }

    @Override
    public  String name() {
        return SPINE_PREFIX + shortName;
    }

    @Override
    public String groupId() {
        return "io.spine";
    }

    @Override
    public String toString() {
        return groupId() + ':' + name();
    }
}
