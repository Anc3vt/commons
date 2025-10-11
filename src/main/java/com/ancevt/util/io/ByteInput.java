package com.ancevt.util.io;

import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>
 * A lightweight, zero-copy, read-only wrapper for a byte array that provides
 * convenient methods for reading primitive types and strings from a binary buffer.
 * Designed for high-performance binary protocols, message parsing, and network
 * packet deserialization in real-time systems (such as multiplayer games).
 * </p>
 *
 * <p>
 * {@code ByteInput} maintains an internal cursor (the current read position)
 * and supports relative reads (methods advance the position automatically)
 * and random access (via {@link #position(int)}, {@link #mark()}, and {@link #resetToMark()}).
 * </p>
 *
 * Example usage
 *
 * <pre>{@code
 * // Writing a packet
 * ByteOutput out = ByteOutput.newInstance();
 * out.writeVarInt(42)
 *    .writeUtfZ("Hello world")
 *    .writeFloat(3.14f);
 *
 * // Reading the same packet
 * ByteInput in = ByteInput.wrap(out);
 * int id = in.readVarInt();            // 42
 * String message = in.readUtfZ();      // "Hello world"
 * float value = in.readFloat();        // 3.14
 *
 * // Peek and reset example
 * in.mark();
 * int x = in.readInt();
 * in.resetToMark(); // rewind
 * }</pre>
 *
 * Performance characteristics
 * <ul>
 *   <li>Zero-copy: no intermediate streams or wrappers</li>
 *   <li>GC-friendly: operates directly on provided byte[]</li>
 *   <li>Thread-confined: not thread-safe by design</li>
 *   <li>Fully compatible with Java 8 and above</li>
 * </ul>
 *
 */
public final class ByteInput {

    private final byte[] buf;
    private int pos;
    private final int limit;
    private int mark;

    private ByteInput(byte[] bytes) {
        this.buf = bytes;
        this.limit = bytes.length;
        this.pos = 0;
    }

    /**
     * Wraps an existing {@link ByteOutput} as an input source without copying.
     *
     * @param out the {@link ByteOutput} to wrap
     * @return a new {@code ByteInput} backed by the same byte array
     */
    public static ByteInput wrap(ByteOutput out) {
        return new ByteInput(out.toByteArray());
    }

    /**
     * Wraps the given byte array as an input buffer.
     *
     * @param bytes the byte array to wrap
     * @return a new {@code ByteInput} instance
     */
    public static ByteInput wrap(byte[] bytes) {
        return new ByteInput(bytes);
    }

    /**
     * Reads a UTF-8 string terminated by a null byte (0x00).
     * Commonly used in lightweight game/network protocols.
     *
     * @return the decoded string
     */
    public String readUtfZ() {
        int start = pos;
        while (pos < limit && buf[pos] != 0) pos++;
        String s = new String(buf, start, pos - start, UTF_8);
        pos++; // skip null terminator
        return s;
    }

    /**
     * Reads a single byte and interprets it as a boolean value.
     * Returns {@code true} for nonzero and {@code false} for zero.
     *
     * @return the decoded boolean
     */
    public boolean readBoolean() {
        return readByte() != 0;
    }

    /**
     * Reads a variable-length integer (7-bit encoding).
     * This format is space-efficient for small positive integers.
     *
     * <p>Example: 300 (0x012C) is stored in 2 bytes as 0xAC 0x02.</p>
     *
     * @return the decoded integer
     */
    public int readVarInt() {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = readByte();
            int value = (read & 0x7F);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) throw new RuntimeException("VarInt too big");
        } while ((read & 0x80) != 0);
        return result;
    }

    /**
     * Reads a variable-length long (7-bit encoding).
     *
     * @return the decoded long value
     */
    public long readVarLong() {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = readByte();
            long value = (read & 0x7F);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) throw new RuntimeException("VarLong too big");
        } while ((read & 0x80) != 0);
        return result;
    }

    /** @return {@code true} if more bytes are available to read */
    public boolean hasRemaining() {
        return pos < limit;
    }

    /** @return the current read position (index into the buffer) */
    public int position() {
        return pos;
    }

    /**
     * Marks the current position in the buffer.
     * Can be restored later via {@link #resetToMark()}.
     */
    public void mark() {
        mark = pos;
    }

    /** Resets the read position to the last marked position. */
    public void resetToMark() {
        pos = mark;
    }

    /**
     * Sets the absolute read position.
     *
     * @param newPos the new position (0-based)
     */
    public void position(int newPos) {
        this.pos = newPos;
    }

    /** @return number of bytes remaining to be read */
    public int remaining() {
        return limit - pos;
    }

    /** Reads a single signed byte. */
    public byte readByte() {
        return buf[pos++];
    }

    /** Reads an unsigned byte as an int in the range [0,255]. */
    public int readUnsignedByte() {
        return buf[pos++] & 0xFF;
    }

    /** Reads a 16-bit signed short in big-endian order. */
    public short readShort() {
        int ch1 = buf[pos++] & 0xFF;
        int ch2 = buf[pos++] & 0xFF;
        return (short) ((ch1 << 8) + ch2);
    }

    /** Reads a 16-bit unsigned short in big-endian order. */
    public int readUnsignedShort() {
        int ch1 = buf[pos++] & 0xFF;
        int ch2 = buf[pos++] & 0xFF;
        return (ch1 << 8) + ch2;
    }

    /** Reads a 32-bit signed integer in big-endian order. */
    public int readInt() {
        int ch1 = buf[pos++] & 0xFF;
        int ch2 = buf[pos++] & 0xFF;
        int ch3 = buf[pos++] & 0xFF;
        int ch4 = buf[pos++] & 0xFF;
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
    }

    /** Reads a 64-bit signed long in big-endian order. */
    public long readLong() {
        return ((long) readInt() << 32) + (readInt() & 0xFFFFFFFFL);
    }

    /** Reads a 32-bit float value. */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /** Reads a 64-bit double value. */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads {@code len} bytes from the buffer into a new array.
     *
     * @param len number of bytes to read
     * @return the read byte array
     */
    public byte[] readBytes(int len) {
        byte[] res = new byte[len];
        System.arraycopy(buf, pos, res, 0, len);
        pos += len;
        return res;
    }

    /**
     * Reads a UTF-8 string whose length prefix is encoded as
     * {@code byte}, {@code short}, or {@code int}.
     *
     * @param lenType the type used for length encoding
     * @return the decoded string
     */
    public String readUtf(Class<?> lenType) {
        int len = 0;
        if (lenType == byte.class) len = readUnsignedByte();
        else if (lenType == short.class) len = readUnsignedShort();
        else if (lenType == int.class) len = readInt();
        return readUtf(len);
    }

    /**
     * Reads a UTF-8 string of the specified length.
     *
     * @param len number of bytes to read
     * @return the decoded string
     */
    public String readUtf(int len) {
        String s = new String(buf, pos, len, UTF_8);
        pos += len;
        return s;
    }

    /**
     * Dumps the entire buffer in hexadecimal format (for debugging/logging).
     *
     * @return a formatted hexadecimal dump of the buffer
     */
    public String dumpHex() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            sb.append(String.format("%02X ", buf[i]));
            if ((i + 1) % 16 == 0) sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Exposes the underlying data as a read-only {@link InputStream}.
     * The returned stream shares the same cursor position.
     *
     * @return an {@link InputStream} view over this buffer
     */
    public InputStream asInputStream() {
        return new InputStream() {
            @Override
            public int read() {
                return (pos < limit) ? (buf[pos++] & 0xFF) : -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (pos >= limit) return -1;
                int available = Math.min(len, limit - pos);
                System.arraycopy(buf, pos, b, off, available);
                pos += available;
                return available;
            }

            @Override
            public int available() {
                return limit - pos;
            }
        };
    }
}
