/*
 * Copyright (C) 2025 Ancevt.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ancevt.util.runtime;

import java.io.*;
import java.util.function.Supplier;

/**
 * A utility class that provides "silent" wrappers around common operations
 * (sleep, I/O, runnables), reducing boilerplate at the cost of swallowing
 * exceptions.
 *
 * <h2>⚠️ Usage Warning ⚠️</h2>
 * <ul>
 *   <li>This class is designed for <b>best-effort</b> scenarios such as tests,
 *   shutdown hooks, background tasks, or optional operations.</li>
 *   <li>In production code, it must be used <b>with extreme caution</b>, as
 *   many methods silently ignore exceptions or convert them into unchecked
 *   {@link RuntimeException}s.</li>
 *   <li>Critical errors (I/O failures, persistence issues, networking errors)
 *   should generally <b>not</b> be suppressed, since they may lead to
 *   inconsistent state or data loss.</li>
 *   <li>Some methods catch {@link Throwable}, which includes {@link Error}
 *   (e.g. {@link OutOfMemoryError}). In most cases you should avoid swallowing
 *   errors of this kind.</li>
 * </ul>
 *
 * <h2>When to use</h2>
 * <ul>
 *   <li>Writing tests, prototypes, quick scripts.</li>
 *   <li>Utility code where failure can be ignored safely (e.g. closing
 *   a resource during shutdown).</li>
 *   <li>Background or monitoring tasks where best-effort is acceptable.</li>
 * </ul>
 *
 * <h2>When NOT to use</h2>
 * <ul>
 *   <li>Business-critical logic where failure must be reported.</li>
 *   <li>Database, file, or network operations where ignoring exceptions
 *   could cause data corruption or loss.</li>
 *   <li>Performance-sensitive code that must distinguish between different
 *   exception types.</li>
 * </ul>
 *
 * <p>
 * Consider providing alternative methods with explicit logging or rethrowing
 * exceptions if using this utility in production systems.
 * </p>
 */
public final class Silent {

    private Silent() {
    }

    /**
     * Sleeps for the given number of milliseconds, rethrowing
     * {@link InterruptedException} as unchecked.
     *
     * @param ms the number of milliseconds to sleep
     * @throws RuntimeException if the thread is interrupted
     */
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the given {@link Closeable} quietly, ignoring exceptions.
     *
     * @param closeable the resource to close; may be null
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Flushes the given {@link Flushable} quietly, ignoring exceptions.
     *
     * @param flushable the resource to flush; may be null
     */
    public static void flushQuietly(Flushable flushable) {
        if (flushable == null) return;
        try {
            flushable.flush();
        } catch (Exception ignored) {
        }
    }

    /**
     * Reads all bytes from the given input stream and returns them as a byte array.
     * <p>
     * If an {@link IOException} occurs during reading, this method returns {@code null}.
     * The input stream is not closed by this method.
     *
     * @param input the input stream to read from
     * @return the byte array containing the contents of the stream, or {@code null} if reading fails
     */
    public static byte[] readAllBytesOrNull(InputStream input) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int bytesRead;

            while ((bytesRead = input.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }

            return buffer.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Copies all bytes from the input stream to the output stream, swallowing any exceptions.
     *
     * @param input  the source stream
     * @param output the destination stream
     */
    public static void copyQuietly(InputStream input, OutputStream output) {
        try {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Executes a supplier and returns its result, or {@code null} if an exception is thrown.
     *
     * @param supplier the supplier to execute
     * @param <T>      the return type
     * @return the result or {@code null} if an error occurs
     */
    public static <T> T getOrNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Executes a runnable and swallows any thrown exceptions.
     *
     * @param runnable the runnable to execute
     */
    public static void runQuietly(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ignored) {
        }
    }

    /**
     * Attempts to run the given {@link Runnable}, and rethrows any exception as {@link RuntimeException}.
     *
     * @param runnable the runnable to execute
     */
    public static void runUnchecked(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Swallows any throwable thrown by the given runnable.
     *
     * @param unsafeRunnable the action to run
     */
    public static void swallow(ThrowableRunnable unsafeRunnable) {
        try {
            unsafeRunnable.run();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Functional interface for lambdas that throw any {@link Throwable}.
     */
    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Throwable;
    }
}
