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

import org.apache.tools.ant.taskdefs.condition.Os;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS;

public final class PubCache {

    private static final Path location = findCache();

    private static Path findCache() {
        String pubCache;
        if (Os.isFamily(FAMILY_WINDOWS)) {
            @SuppressWarnings("CallToSystemGetenv")
            String localAppData = System.getenv("LOCALAPPDATA");
            pubCache = localAppData + "/Pub/Cache/bin";
        } else {
            @SuppressWarnings("AccessOfSystemProperties")
            String userHome = System.getProperty("user.home");
            pubCache = userHome + "/.pub-cache/bin";
        }
        return Paths.get(pubCache);
    }

    public static Path location() {
        return location;
    }
}
