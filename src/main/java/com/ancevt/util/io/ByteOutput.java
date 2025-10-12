package com.ancevt.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>
 * A high-performance, auto-growing byte buffer for serializing data into a compact binary form.
 * Designed primarily for low-GC message encoding and network packet construction
 * in multiplayer game servers, ETL pipelines, and custom binary protocols.
 * </p>
 *
 * <p>
 * {@code ByteOutput} writes primitive values, UTF-8 strings, and variable-length integers
 * into an internal byte array that expands automatically as needed.
 * The resulting data can be directly consumed by {@link ByteInput} or
 * written to an {@link java.io.OutputStream}.
 * </p>
 *
 * Example usage
 * <pre>{@code
 * // Constructing a binary message
 * ByteOutput out = ByteOutput.newInstance();
 * out.writeVarInt(42)
 *    .writeUtfZ("Hello world")
 *    .writeFloat(3.14f)
 *    .writeBoolean(true);
 *
 * // Obtain the byte array
 * byte[] data = out.toByteArray();
 *
 * // Read it back using ByteInput
 * ByteInput in = ByteInput.wrap(data);
 * int id = in.readVarInt();          // 42
 * String msg = in.readUtfZ();        // "Hello world"
 * float f = in.readFloat();          // 3.14
 * boolean b = in.readBoolean();      // true
 * }</pre>
 *
 * Key features
 * <ul>
 *   <li><b>Zero dependencies:</b> pure Java 8 compatible</li>
 *   <li><b>Auto-resizing:</b> grows as you write data</li>
 *   <li><b>Thread-local pooling:</b> use {@link #pooled()} to reuse buffers per thread</li>
 *   <li><b>Binary protocol ready:</b> supports {@code VarInt}, {@code VarLong}, and UTF encodings</li>
 *   <li><b>GC-friendly:</b> minimal allocations; reuses backing arrays</li>
 * </ul>
 *
 * Thread-local usage
 * <pre>{@code
 * // Obtain a reusable buffer for the current thread
 * ByteOutput buf = ByteOutput.pooled();
 * buf.writeVarInt(123).writeUtfZ("Thread-safe reuse");
 *
 * // No need to release manually — it’s reused automatically
 * }</pre>
 */
public final class ByteOutput {

    private static final ThreadLocal<ByteOutput> LOCAL =
            ThreadLocal.withInitial(ByteOutput::newInstance);

    private byte[] buf;
    private int pos;

    private static final int DEFAULT_SIZE = 8192;

    private ByteOutput(int capacity) {
        this.buf = new byte[capacity];
    }

    // ----------------------------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------------------------

    /**
     * Creates a new {@code ByteOutput} with a default capacity (8 KB).
     *
     * @return a new instance
     */
    public static ByteOutput newInstance() {
        return new ByteOutput(DEFAULT_SIZE);
    }

    /**
     * Creates a new {@code ByteOutput} with the given initial capacity.
     *
     * @param capacity the initial buffer size in bytes
     * @return a new instance
     */
    public static ByteOutput newInstance(int capacity) {
        return new ByteOutput(capacity);
    }

    /**
     * Returns a thread-local reusable buffer.
     * Each thread gets its own instance which is reset automatically.
     *
     * @return a per-thread {@code ByteOutput}
     */
    public static ByteOutput pooled() {
        ByteOutput b = LOCAL.get();
        b.reset();
        return b;
    }

    // ----------------------------------------------------------------------------------------
    // Core buffer management
    // ----------------------------------------------------------------------------------------

    private void ensureCapacity(int extra) {
        int required = pos + extra;
        if (required > buf.length) {
            int newCap = Math.max(required, buf.length << 1);
            byte[] newBuf = new byte[newCap];
            System.arraycopy(buf, 0, newBuf, 0, pos);
            buf = newBuf;
        }
    }

    /**
     * Reduces internal buffer size if it has grown too large (e.g. after handling large packets).
     * Useful for long-lived buffers to prevent memory bloat.
     *
     * @return this instance
     */
    public ByteOutput compact() {
        if (buf.length > 65536) { // threshold (64 KB)
            byte[] newBuf = new byte[pos];
            System.arraycopy(buf, 0, newBuf, 0, pos);
            buf = newBuf;
        }
        return this;
    }

    /**
     * Clears the buffer content without deallocating it.
     * The next write will start from position 0.
     *
     * @return this instance
     */
    public ByteOutput reset() {
        pos = 0;
        return this;
    }

    /**
     * @return number of bytes currently written into the buffer
     */
    public int size() {
        return pos;
    }

    /**
     * @return a new array containing all written bytes
     */
    public byte[] toByteArray() {
        byte[] result = new byte[pos];
        System.arraycopy(buf, 0, result, 0, pos);
        return result;
    }

    // ----------------------------------------------------------------------------------------
    // Primitive write methods
    // ----------------------------------------------------------------------------------------

    /**
     * Writes a boolean as a single byte (1 or 0).
     */
    public ByteOutput writeBoolean(boolean v) {
        return writeByte(v ? 1 : 0);
    }

    /**
     * Writes a single byte.
     */
    public ByteOutput writeByte(int v) {
        ensureCapacity(1);
        buf[pos++] = (byte) v;
        return this;
    }

    /**
     * Writes a 16-bit short in big-endian order.
     */
    public ByteOutput writeShort(int v) {
        ensureCapacity(2);
        buf[pos++] = (byte) (v >>> 8);
        buf[pos++] = (byte) v;
        return this;
    }

    /**
     * Writes a 32-bit integer in big-endian order.
     */
    public ByteOutput writeInt(int v) {
        ensureCapacity(4);
        buf[pos++] = (byte) (v >>> 24);
        buf[pos++] = (byte) (v >>> 16);
        buf[pos++] = (byte) (v >>> 8);
        buf[pos++] = (byte) v;
        return this;
    }

    /**
     * Writes a 64-bit long in big-endian order.
     */
    public ByteOutput writeLong(long v) {
        ensureCapacity(8);
        buf[pos++] = (byte) (v >>> 56);
        buf[pos++] = (byte) (v >>> 48);
        buf[pos++] = (byte) (v >>> 40);
        buf[pos++] = (byte) (v >>> 32);
        buf[pos++] = (byte) (v >>> 24);
        buf[pos++] = (byte) (v >>> 16);
        buf[pos++] = (byte) (v >>> 8);
        buf[pos++] = (byte) v;
        return this;
    }

    /**
     * Writes a 32-bit float.
     */
    public ByteOutput writeFloat(float f) {
        return writeInt(Float.floatToIntBits(f));
    }

    /**
     * Writes a 64-bit double.
     */
    public ByteOutput writeDouble(double d) {
        return writeLong(Double.doubleToLongBits(d));
    }

    /**
     * Writes an array of bytes.
     *
     * @param bytes the array to copy
     * @return this instance
     */
    public ByteOutput write(byte[] bytes) {
        ensureCapacity(bytes.length);
        System.arraycopy(bytes, 0, buf, pos, bytes.length);
        pos += bytes.length;
        return this;
    }

    // ----------------------------------------------------------------------------------------
    // Variable-length and string encoding
    // ----------------------------------------------------------------------------------------

    /**
     * Writes a variable-length integer (7-bit encoding, like Minecraft or Protocol Buffers).
     *
     * @param value the integer to encode
     * @return this instance
     */
    public ByteOutput writeVarInt(int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        writeByte(value & 0x7F);
        return this;
    }

    /**
     * Writes a variable-length long (7-bit encoding).
     *
     * @param value the long to encode
     * @return this instance
     */
    public ByteOutput writeVarLong(long value) {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            writeByte((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        writeByte((int) (value & 0x7F));
        return this;
    }

    /**
     * Writes a UTF-8 string followed by a null terminator (0x00).
     * Useful for compact network protocols.
     *
     * @param s the string to encode
     * @return this instance
     */
    public ByteOutput writeUtfZ(String s) {
        byte[] data = s.getBytes(UTF_8);
        write(data);
        writeByte(0);
        return this;
    }

    /**
     * Writes a UTF-8 string with an explicit length prefix
     * (length may be stored as byte, short, or int).
     *
     * @param lenType one of {@code byte.class}, {@code short.class}, or {@code int.class}
     * @param str     the string to encode
     * @return this instance
     */
    public ByteOutput writeUtf(Class<?> lenType, String str) {
        byte[] data = str.getBytes(UTF_8);
        if (lenType == byte.class) writeByte(data.length);
        else if (lenType == short.class) writeShort(data.length);
        else if (lenType == int.class) writeInt(data.length);
        write(data);
        return this;
    }

    // ----------------------------------------------------------------------------------------
    // Utilities
    // ----------------------------------------------------------------------------------------

    /**
     * Writes the current buffer to an {@link OutputStream}.
     *
     * @param os the output stream to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(OutputStream os) throws IOException {
        os.write(buf, 0, pos);
    }

    /**
     * Returns an {@link OutputStream} view that writes directly into this buffer.
     * Useful for integrating with APIs expecting an {@code OutputStream}.
     */
    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                writeByte(b);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                ensureCapacity(len);
                System.arraycopy(b, off, buf, pos, len);
                pos += len;
            }
        };
    }

    /**
     * Calculates a CRC32 checksum for the written data.
     *
     * @return checksum value
     */
    public long checksum() {
        CRC32 crc = new CRC32();
        crc.update(buf, 0, pos);
        return crc.getValue();
    }

    /**
     * Dumps the current buffer contents in hexadecimal format for debugging.
     *
     * @return human-readable hex dump of the buffer
     */
    public String dumpHex() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pos; i++) {
            sb.append(String.format("%02X ", buf[i]));
            if ((i + 1) % 16 == 0) sb.append('\n');
        }
        return sb.toString();
    }
}
