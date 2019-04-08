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

package io.spine.tools.gradle.bootstrap.given;

import com.google.common.collect.ImmutableSet;
import io.spine.tools.gradle.ArtifactModule;
import io.spine.tools.gradle.DependencyTarget;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * A memoizing test-only implementation of {@link DependencyTarget}.
 */
public final class TestDependencyTarget implements DependencyTarget {

    private final Set<String> dependencies = newHashSet();
    private final Set<ArtifactModule> exclusions = newHashSet();

    @Override
    public void depend(String configuration, String notation) {
        dependencies.add(notation);
    }

    @Override
    public void exclude(ArtifactModule module) {
        exclusions.add(module);
    }

    public ImmutableSet<String> dependencies() {
        return ImmutableSet.copyOf(dependencies);
    }

    public ImmutableSet<ArtifactModule> exclusions() {
        return ImmutableSet.copyOf(exclusions);
    }
}
