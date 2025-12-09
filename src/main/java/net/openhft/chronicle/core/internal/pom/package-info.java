/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Internal access to Maven {@code pom.properties} resources.
 *
 * <p>These classes implement the mechanics for locating and loading
 * build metadata, which is exposed to callers via the public
 * {@code net.openhft.chronicle.core.pom} facade.
 *
 * <p>The exact loading strategy and caching behaviour are internal
 * implementation details and may change between releases.
 */
package net.openhft.chronicle.core.internal.pom;
