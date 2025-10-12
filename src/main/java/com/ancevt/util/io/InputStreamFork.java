package com.ancevt.util.io;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A small utility for duplicating a single {@link InputStream} into multiple
 * independent readable streams.
 * </p>
 *
 * <p>
 * All resulting {@link InputStream}s share the same in-memory byte buffer,
 * allowing each reader to process the data independently without consuming
 * the source stream multiple times.
 * </p>
 *
 * <p>
 * This is especially useful in cases such as:
 * </p>
 * <ul>
 *   <li>Logging and parsing network packets simultaneously</li>
 *   <li>Analyzing streamed content in multiple consumers</li>
 *   <li>Debugging / testing pipelines that read from the same data source</li>
 * </ul>
 *
 * Example usage
 *
 * <pre>{@code
 * try (InputStream src = new FileInputStream("packet.bin")) {
 *     InputStreamFork fork = InputStreamFork.fork(src, 2);
 *     InputStream logStream = fork.left();
 *     InputStream parserStream = fork.right();
 *
 *     // Example: read with parser
 *     byte[] header = new byte[4];
 *     parserStream.read(header);
 *
 *     // Example: log raw data
 *     System.out.println("Raw bytes: " + header[0]);
 *
 *     fork.close(); // closes both streams
 * }
 * }</pre>
 *
 * Implementation details
 * <ul>
 *   <li>The entire input stream is read into memory once.</li>
 *   <li>Each forked stream is backed by the same byte array — no redundant copies.</li>
 *   <li>Use {@link #forkLazy(InputStream, int, int)} if you need to copy while reading in real time.</li>
 *   <li>Not suitable for very large files or streams — the full content is buffered in memory.</li>
 * </ul>
 *
 * Thread-safety
 * <p>
 * Each resulting {@link InputStream} is thread-safe for independent reads,
 * but the {@code InputStreamFork} itself is not intended for concurrent modifications.
 * </p>
 */
public final class InputStreamFork implements Closeable {

    private final List<ByteArrayInputStream> streams;
    private byte[] data;

    private InputStreamFork(byte[] data, int count) {
        this.data = data;
        this.streams = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            streams.add(new ByteArrayInputStream(data));
        }
    }

    /**
     * Returns an unmodifiable view of all forked {@link InputStream}s.
     *
     * @return list of forked streams
     */
    public List<InputStream> getInputStreams() {
        return Collections.unmodifiableList(streams);
    }

    /**
     * Returns the first forked {@link InputStream}.
     *
     * @return the left stream
     */
    public InputStream left() {
        return streams.get(0);
    }

    /**
     * Returns the second forked {@link InputStream}, if present.
     *
     * @return the right stream
     * @throws IllegalStateException if there is only one stream
     */
    public InputStream right() {
        if (streams.size() < 2)
            throw new IllegalStateException("No right() stream: count < 2");
        return streams.get(1);
    }

    /**
     * @return the number of forked streams
     */
    public int size() {
        return streams.size();
    }

    /**
     * Returns the underlying shared byte buffer.
     * The returned array should be treated as read-only.
     *
     * @return shared byte buffer, or {@code null} if using a lazy fork
     */
    public byte[] getBuffer() {
        return data;
    }

    /**
     * Closes all forked streams.
     * This does not affect the underlying byte buffer.
     */
    @Override
    public void close() {
        for (InputStream s : streams) {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
    }

    // ------------------------------------------------------------------------
    // Factory methods
    // ------------------------------------------------------------------------

    /**
     * Reads the entire content of the given {@link InputStream} into memory
     * and returns an {@code InputStreamFork} with two identical readers.
     *
     * <p>Equivalent to {@code fork(source, 2)}.</p>
     *
     * @param source the input stream to duplicate
     * @return an {@code InputStreamFork} with two identical readers
     */
    public static InputStreamFork fork(InputStream source) {
        return fork(source, 2);
    }

    /**
     * Reads the full {@link InputStream} once, stores its content in memory,
     * and returns multiple {@link ByteArrayInputStream} readers over the same data.
     *
     * @param source the input stream to duplicate
     * @param count  the number of streams to create (must be > 0)
     * @return a fork containing {@code count} identical input streams
     * @throws IllegalArgumentException if {@code count <= 0}
     * @throws UncheckedIOException     if an I/O error occurs
     */
    public static InputStreamFork fork(InputStream source, int count) {
        if (count <= 0)
            throw new IllegalArgumentException("count must be > 0");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = source.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            source.close();
            return new InputStreamFork(baos.toByteArray(), count);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates a “lazy” fork: reads data from {@code src} in real time
     * and duplicates it into multiple output buffers simultaneously.
     * This mode avoids storing a shared buffer, but duplicates data on the fly.
     *
     * <h4>Use case:</h4>
     * When you need to tee an incoming stream to multiple consumers,
     * e.g., for parsing + logging pipelines.
     *
     * @param src        the source input stream
     * @param count      number of duplicate streams to produce
     * @param bufferSize size of temporary buffer (in bytes)
     * @return a new {@code InputStreamFork} with independent byte buffers
     * @throws IOException if an I/O error occurs
     */
    public static InputStreamFork forkLazy(InputStream src, int count, int bufferSize) throws IOException {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        if (bufferSize <= 0) bufferSize = 8192;

        // Prepare output buffers
        List<ByteArrayOutputStream> outputs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) outputs.add(new ByteArrayOutputStream());

        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = src.read(buf)) != -1) {
            for (ByteArrayOutputStream out : outputs) {
                out.write(buf, 0, len);
            }
        }

        List<ByteArrayInputStream> inputs = new ArrayList<>(count);
        for (ByteArrayOutputStream out : outputs) {
            inputs.add(new ByteArrayInputStream(out.toByteArray()));
        }

        InputStreamFork fork = new InputStreamFork(new byte[0], 0);
        fork.streams.clear();
        fork.streams.addAll(inputs);
        fork.data = null; // data not shared
        return fork;
    }
}
