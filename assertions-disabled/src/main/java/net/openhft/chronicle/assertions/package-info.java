/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Assertion utilities with assertions compiled out.
 *
 * <p>This variant of the {@code net.openhft.chronicle.assertions}
 * package is built so that calls guarded by
 * {@code AssertUtil.SKIP_ASSERTIONS} compile down to no operation in
 * performance critical code paths.
 *
 * <p>The package is intended to be selected via build profiles rather
 * than referenced directly. Application code should only depend on
 * the public surface of {@code AssertUtil}.
 */
package net.openhft.chronicle.assertions;
