package com.ancevt.util.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ByteIOTest {

    // ----------------------------------------------------------
    // ByteOutput + ByteInput roundtrip
    // ----------------------------------------------------------

    @Test
    void testPrimitiveRoundtrip() {
        ByteOutput out = ByteOutput.newInstance();
        out.writeBoolean(true)
                .writeByte(0x12)
                .writeShort(0x1234)
                .writeInt(0x12345678)
                .writeLong(0x123456789ABCDEFL)
                .writeFloat(3.14f)
                .writeDouble(42.42)
                .writeUtfZ("Hello");

        ByteInput in = ByteInput.wrap(out);

        assertTrue(in.readBoolean());
        assertEquals(0x12, in.readUnsignedByte());
        assertEquals(0x1234, in.readUnsignedShort());
        assertEquals(0x12345678, in.readInt());
        assertEquals(0x123456789ABCDEFL, in.readLong());
        assertEquals(3.14f, in.readFloat(), 0.0001);
        assertEquals(42.42, in.readDouble(), 0.0001);
        assertEquals("Hello", in.readUtfZ());
        assertFalse(in.hasRemaining());
    }

    @Test
    void testVarIntAndVarLong() {
        int[] ints = {0, 1, 127, 128, 255, 300, 16384, Integer.MAX_VALUE};
        long[] longs = {0L, 1L, 127L, 128L, 16384L, Long.MAX_VALUE};

        ByteOutput out = ByteOutput.newInstance();
        for (int v : ints) out.writeVarInt(v);
        for (long v : longs) out.writeVarLong(v);

        ByteInput in = ByteInput.wrap(out);

        for (int v : ints) assertEquals(v, in.readVarInt());
        for (long v : longs) assertEquals(v, in.readVarLong());
    }

    @Test
    void testUtfWithLength() {
        ByteOutput out = ByteOutput.newInstance();
        out.writeUtf(byte.class, "A")
                .writeUtf(short.class, "Hello")
                .writeUtf(int.class, "Привет");

        ByteInput in = ByteInput.wrap(out);
        assertEquals("A", in.readUtf(byte.class));
        assertEquals("Hello", in.readUtf(short.class));
        assertEquals("Привет", in.readUtf(int.class));
    }

    @Test
    void testChecksum() {
        ByteOutput out = ByteOutput.newInstance();
        out.writeUtfZ("checksum-test");
        long crc = out.checksum();
        assertTrue(crc > 0);
    }

    @Test
    void testCompactAndReset() {
        ByteOutput out = ByteOutput.newInstance(4);
        for (int i = 0; i < 10000; i++) out.writeInt(i);
        assertTrue(out.size() > 0);
        int oldSize = out.toByteArray().length;
        out.compact();
        assertTrue(out.toByteArray().length <= oldSize);
        out.reset();
        assertEquals(0, out.size());
    }

    @Test
    void testPooled() {
        ByteOutput a = ByteOutput.pooled();
        a.writeUtfZ("one");
        ByteOutput b = ByteOutput.pooled();
        assertSame(a, b);
        assertEquals(0, b.size());
    }

    // ----------------------------------------------------------
    // ByteInput positioning and stream
    // ----------------------------------------------------------

    @Test
    void testPositionAndMark() throws IOException {
        ByteOutput out = ByteOutput.newInstance();
        out.writeInt(1).writeInt(2).writeInt(3);
        ByteInput in = ByteInput.wrap(out);

        assertEquals(0, in.position());
        assertEquals(12, in.remaining());

        in.mark();
        assertEquals(1, in.readInt());
        in.resetToMark();
        assertEquals(1, in.readInt()); // should reread same value

        // Stream view
        InputStream is = in.asInputStream();
        byte[] all = new byte[in.remaining()];
        int total = 0, read;
        while ((read = is.read(all, total, all.length - total)) != -1) {
            total += read;
        }
        assertEquals(all.length, total);
    }

    // ----------------------------------------------------------
    // InputStreamFork
    // ----------------------------------------------------------

    @Test
    void testForkBasic() throws IOException {
        byte[] data = "fork-test".getBytes(StandardCharsets.UTF_8);
        InputStream src = new ByteArrayInputStream(data);
        InputStreamFork fork = InputStreamFork.fork(src, 2);

        InputStream left = fork.left();
        InputStream right = fork.right();

        byte[] a = readAll(left);
        byte[] b = readAll(right);

        assertArrayEquals(data, a);
        assertArrayEquals(data, b);
        assertEquals(2, fork.size());
        assertNotNull(fork.getBuffer());
        fork.close();
    }

    @Test
    void testForkLazy() throws IOException {
        byte[] data = "lazy".getBytes(StandardCharsets.UTF_8);
        InputStream src = new ByteArrayInputStream(data);
        InputStreamFork fork = InputStreamFork.forkLazy(src, 3, 4);

        List<InputStream> ins = fork.getInputStreams();
        assertEquals(3, ins.size());

        for (InputStream in : ins) {
            byte[] read = readAll(in);
            assertArrayEquals(data, read);
        }

        assertNull(fork.getBuffer());
        fork.close();
    }

    @Test
    void testForkErrors() {
        assertThrows(IllegalArgumentException.class, () ->
                InputStreamFork.fork(new ByteArrayInputStream(new byte[0]), 0));
        assertThrows(IllegalArgumentException.class, () ->
                InputStreamFork.forkLazy(new ByteArrayInputStream(new byte[0]), 0, 0));
    }

    @Test
    void testDumpHex() {
        ByteOutput out = ByteOutput.newInstance();
        out.writeUtfZ("abc");
        String hex = out.dumpHex();
        assertTrue(hex.contains("61")); // 'a'
        assertTrue(hex.contains("62")); // 'b'
        assertTrue(hex.contains("63")); // 'c'

        ByteInput in = ByteInput.wrap(out);
        String hexIn = in.dumpHex();
        assertTrue(hexIn.contains("61"));
    }

    // ----------------------------------------------------------
    // Helper for Java 8 (no readAllBytes)
    // ----------------------------------------------------------

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }
}
