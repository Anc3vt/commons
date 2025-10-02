package com.ancevt.util.runtime;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SilentTest {

    @Test
    void testSleepDoesNotThrowWhenNotInterrupted() {
        assertDoesNotThrow(() -> Silent.sleep(10));
    }

    @Test
    void testSleepThrowsWhenInterrupted() {
        Thread.currentThread().interrupt(); // пометим тред
        try {
            assertThrows(RuntimeException.class, () -> Silent.sleep(1));
        } finally {
            // сбросим флаг, чтобы не мешать другим тестам
            Thread.interrupted();
        }
    }

    @Test
    void testCloseQuietlyIgnoresException() {
        Closeable c = () -> { throw new IOException("fail"); };
        assertDoesNotThrow(() -> Silent.closeQuietly(c));
    }

    @Test
    void testFlushQuietlyIgnoresException() {
        Flushable f = () -> { throw new IOException("fail"); };
        assertDoesNotThrow(() -> Silent.flushQuietly(f));
    }

    @Test
    void testReadAllBytesOrNullReadsSuccessfully() {
        byte[] data = "hello".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        assertArrayEquals(data, Silent.readAllBytesOrNull(in));
    }

    @Test
    void testReadAllBytesOrNullReturnsNullOnError() {
        InputStream in = new InputStream() {
            @Override public int read() throws IOException {
                throw new IOException("fail");
            }
        };
        assertNull(Silent.readAllBytesOrNull(in));
    }

    @Test
    void testCopyQuietlyCopiesData() throws Exception {
        byte[] data = "abc".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Silent.copyQuietly(in, out);

        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    void testCopyQuietlyIgnoresError() {
        InputStream in = new InputStream() {
            @Override public int read() throws IOException {
                throw new IOException("fail");
            }
        };
        OutputStream out = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> Silent.copyQuietly(in, out));
    }

    @Test
    void testGetOrNullReturnsValue() {
        String result = Silent.getOrNull(() -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void testGetOrNullReturnsNullOnException() {
        String result = Silent.getOrNull(() -> { throw new RuntimeException("fail"); });
        assertNull(result);
    }

    @Test
    void testRunQuietlyRunsWithoutException() {
        Silent.runQuietly(() -> {});
    }

    @Test
    void testRunQuietlySwallowsException() {
        assertDoesNotThrow(() -> Silent.runQuietly(() -> { throw new RuntimeException("fail"); }));
    }

    @Test
    void testRunUncheckedThrowsRuntimeException() {
        assertThrows(RuntimeException.class, () -> Silent.runUnchecked(() -> { throw new RuntimeException("fail"); }));
    }

    @Test
    void testSwallowIgnoresThrowable() {
        assertDoesNotThrow(() -> Silent.swallow(() -> { throw new Error("serious"); }));
    }
}
