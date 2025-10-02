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

package com.ancevt.util.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueTest {

    enum Mode {DEBUG, RELEASE}

    @Test
    void testAsInt() {
        assertEquals(42, new ObservableConfigValue("42").asInt(0));
        assertEquals(0, new ObservableConfigValue("oops").asInt(0));
    }

    @Test
    void testAsBoolean() {
        assertTrue(new ObservableConfigValue("true").asBoolean(false));
        assertTrue(new ObservableConfigValue("YES").asBoolean(false));
        assertTrue(new ObservableConfigValue("1").asBoolean(false));
        assertFalse(new ObservableConfigValue("false").asBoolean(true));
    }

    @Test
    void testAsEnum() {
        assertEquals(Mode.DEBUG, new ObservableConfigValue("debug").asEnum(Mode.class, Mode.RELEASE));
        assertEquals(Mode.RELEASE, new ObservableConfigValue("unknown").asEnum(Mode.class, Mode.RELEASE));
    }

    @Test
    void testAsUUID() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, new ObservableConfigValue(uuid.toString()).asUUID(null));
        assertNull(new ObservableConfigValue("invalid").asUUID(null));
    }

    @Test
    void testAsBigDecimal() {
        assertEquals(BigDecimal.TEN, new ObservableConfigValue("10").asBigDecimal(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, new ObservableConfigValue("oops").asBigDecimal(BigDecimal.ZERO));
    }

    @Test
    void testAsChar() {
        assertEquals('A', new ObservableConfigValue("ABC").asChar('X'));
        assertEquals('X', new ObservableConfigValue("").asChar('X'));
    }

    @Test
    void testAsPathAndURI() {
        assertEquals(Paths.get("/tmp"), new ObservableConfigValue("/tmp").asPath(null));
        assertEquals(URI.create("http://example.com"), new ObservableConfigValue("http://example.com").asURI(null));
    }

    @Test
    void testAsURL() throws Exception {
        String urlStr = "http://example.com";
        URL url = new URL(urlStr);
        assertEquals(url, new ObservableConfigValue(urlStr).asURL(null));
    }

    @Test
    void testAsOptional() {
        assertEquals(Optional.of("hello"), new ObservableConfigValue("hello").asOptional());
        assertEquals(Optional.empty(), new ObservableConfigValue("").asOptional());
        assertEquals(Optional.empty(), new ObservableConfigValue(null).asOptional());
    }

    @Test
    void testIsEmptyAndPresent() {
        assertTrue(new ObservableConfigValue("").isEmpty());
        assertTrue(new ObservableConfigValue(null).isEmpty());
        assertFalse(new ObservableConfigValue("data").isEmpty());

        assertFalse(new ObservableConfigValue("").isPresent());
        assertTrue(new ObservableConfigValue("data").isPresent());
    }

    @Test
    void testToStringEqualsValue() {
        assertEquals("test", new ObservableConfigValue("test").toString());
    }
}
