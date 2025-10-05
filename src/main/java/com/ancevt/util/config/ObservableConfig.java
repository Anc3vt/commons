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

import java.util.*;

/**
 * A lightweight and dynamic key-value configuration store.
 * <p>
 * {@code ObservableConfig} allows storing, retrieving, and updating configuration properties at runtime.
 * It also supports listening to changes in configuration via {@link ChangeListener}.
 * <p>
 * All values are stored as {@code String}. The class is not thread-safe by design.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Key-value storage using {@code String} keys and values</li>
 *     <li>Supports registering listeners to track changes</li>
 *     <li>Change suppression mechanism to batch updates</li>
 *     <li>Immutable access to the internal map</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ObservableConfig config = new ObservableConfig();
 * config.put("host", "localhost");
 * config.put("port", "8080");
 *
 * config.addChangeListener((key, oldValue, newValue) -> {
 *     System.out.println("Changed: " + key + " from " + oldValue + " to " + newValue);
 * });
 *
 * config.put("host", "127.0.0.1");
 * }</pre>
 *
 */
public class ObservableConfig {

    private final Map<String, String> map = new HashMap<>();
    private final Set<ChangeListener> changeListeners = new LinkedHashSet<>();
    private boolean suppressEvents = false;

    /**
     * Creates an empty configuration store.
     */
    public ObservableConfig() {
    }

    /**
     * Creates a configuration store and populates it with the given map.
     *
     * @param source the initial configuration map
     */
    public ObservableConfig(HashMap<String, String> source) {
        map.putAll(source);
    }

    /**
     * Enables or disables event notifications for changes.
     * When set to {@code true}, no {@link ChangeListener}s will be triggered until re-enabled.
     *
     * @param suppress {@code true} to suppress change notifications; {@code false} to enable them
     */
    public void setSuppressEvents(boolean suppress) {
        suppressEvents = suppress;
    }

    /**
     * Returns whether change event notifications are currently suppressed.
     *
     * @return {@code true} if change notifications are suppressed; {@code false} otherwise
     */
    public boolean isSuppressEvents() {
        return suppressEvents;
    }

    /**
     * Checks whether the configuration contains the given key.
     *
     * @param key the configuration key
     * @return {@code true} if the key exists; {@code false} otherwise
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * Inserts all entries from the given source map into this configuration.
     * Values will be converted to strings using {@link String#valueOf(Object)}.
     * Triggers change listeners for changed values.
     *
     * @param source the source map
     */
    public void putAll(Map<?, ?> source) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());
            put(key, value);
        }
    }

    /**
     * Sets a value for the given key.
     * If the key already exists and the value is different, listeners are notified.
     *
     * @param key   the configuration key
     * @param value the new value
     */
    public void put(String key, String value) {
        String oldValue = map.get(key);
        if (!Objects.equals(oldValue, value)) {
            map.put(key, value);
            notifyChanged(key, oldValue, value);
        }
    }

    /**
     * Removes the key from the configuration if it exists.
     * Triggers listeners if the key was present.
     *
     * @param key the key to remove
     */
    public void remove(String key) {
        String oldValue = map.remove(key);
        if (oldValue != null) {
            notifyChanged(key, oldValue, null);
        }
    }

    /**
     * Clears all configuration entries.
     * Triggers listeners for each key that was removed.
     */
    public void clear() {
        for (String key : new HashSet<>(map.keySet())) {
            remove(key);
        }
    }

    /**
     * Returns the value associated with the given key, or {@code null} if not present.
     *
     * @param key the key to look up
     * @return the associated value or {@code null}
     */
    public String getAsString(String key) {
        return map.get(key);
    }

    /**
     * Returns the value associated with the given key, or the default value if not present.
     *
     * @param key          the key to look up
     * @param defaultValue the default value
     * @return the associated value or the default
     */
    public String getAsString(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Returns a {@link ObservableConfigValue} wrapper for the given key.
     * Provides type-safe conversion methods.
     *
     * @param key the key to look up
     * @return a {@link ObservableConfigValue} instance
     */
    public ObservableConfigValue get(String key) {
        return new ObservableConfigValue(map.get(key));
    }

    /**
     * Returns an unmodifiable view of the current configuration.
     *
     * @return the configuration map
     */
    public Map<String, String> toMap() {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(map);
        }
    }

    private void notifyChanged(String key, String oldValue, String newValue) {
        if (suppressEvents) return;

        for (ChangeListener listener : changeListeners) {
            listener.onChange(key, oldValue, newValue);
        }
    }

    /**
     * Registers a {@link ChangeListener} to receive change notifications.
     *
     * @param changeListener the listener to register
     */
    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    /**
     * Unregisters a {@link ChangeListener}.
     *
     * @param changeListener the listener to remove
     */
    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    /**
     * Removes all registered {@link ChangeListener}s.
     */
    public void removeAllChangeListeners() {
        changeListeners.clear();
    }

    /**
     * A functional interface for listening to changes in the configuration.
     */
    @FunctionalInterface
    public interface ChangeListener {

        /**
         * Called when a configuration value changes.
         *
         * @param key      the key that was changed
         * @param oldValue the previous value (may be {@code null})
         * @param newValue the new value (may be {@code null} if removed)
         */
        void onChange(String key, String oldValue, String newValue);
    }
}
