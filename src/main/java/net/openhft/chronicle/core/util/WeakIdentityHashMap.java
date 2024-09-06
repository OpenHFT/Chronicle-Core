/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A hash map that uses weak references for its keys and compares keys using identity equality.
 * This class is based on the concept of a {@link WeakHashMap} but uses identity equality (via {@code ==})
 * instead of the default equality (via {@code equals}).
 * <p>
 * Keys in this map are held with {@link WeakReference}s, allowing them to be garbage-collected
 * when there are no strong references to the key elsewhere. When a key is collected, its entry
 * is removed from the map automatically.
 * <p>
 * This implementation is thread-safe, using a {@link ConcurrentHashMap} for storage.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class WeakIdentityHashMap<K, V> extends AbstractMap<K, V> {
    private final Map<WeakKey<K>, V> map;
    private final transient ReferenceQueue<K> queue = new ReferenceQueue<>();

    /**
     * Constructs a new, empty identity hash map with a default initial size of 16.
     */
    public WeakIdentityHashMap() {
        map = new ConcurrentHashMap<>(16);
    }

    /**
     * Retrieves the internal map and cleans up any entries whose keys have been garbage-collected.
     *
     * @return The underlying map with any collected keys removed.
     */
    private Map<WeakKey<K>, V> getMap() {
        // Clean up collected keys
        for (Reference<? extends K> ref; (ref = this.queue.poll()) != null; ) {
            map.remove(ref);
        }
        return map;
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public V get(Object key) {
        // Lookup using a WeakKey with the given key
        return getMap().get(new WeakKey<>(key, null));
    }

    @Override
    public V put(K key, V value) {
        // Insert the value with a WeakKey referencing the provided key
        return getMap().put(new WeakKey<>(key, queue), value);
    }

    @Override
    public V remove(Object key) {
        // Remove the entry associated with the WeakKey referencing the provided key
        return getMap().remove(new WeakKey<>(key, null));
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @NotNull
            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    private K next;
                    final Iterator<WeakKey<K>> iterator = getMap().keySet().iterator();

                    @Override
                    public void remove() {
                        iterator.remove();
                    }

                    @Override
                    public boolean hasNext() {
                        while (iterator.hasNext()) {
                            if ((next = iterator.next().get()) != null) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public K next() {
                        if (next == null && !hasNext()) {
                            throw new NoSuchElementException();
                        }
                        K ret = next;
                        next = null;
                        return ret;
                    }
                };
            }

            @Override
            public int size() {
                return getMap().keySet().size();
            }
        };
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @NotNull
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<WeakKey<K>, V>> iterator = getMap().entrySet().iterator();
                return new Iterator<Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }

                    @Override
                    public Entry<K, V> next() {
                        return new Entry<K, V>() {
                            final Entry<WeakKey<K>, V> entry = iterator.next();

                            @Override
                            public K getKey() {
                                return entry.getKey().get();
                            }

                            @Override
                            public V getValue() {
                                return entry.getValue();
                            }

                            @Override
                            public V setValue(V value) {
                                return null;
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return getMap().entrySet().size();
            }
        };
    }

    /**
     * A wrapper for keys that are stored in the map as weak references. The hash code is cached
     * when the key is created, so it does not change after the key is garbage-collected.
     *
     * @param <K> the type of the key
     */
    private static final class WeakKey<K> extends WeakReference<K> {
        private final int hash;

        /**
         * Creates a new {@code WeakKey} with a given key and a reference queue.
         *
         * @param key the key to wrap in a weak reference
         * @param q   the reference queue to register the key with, or null if no queue is needed
         */
        WeakKey(K key, ReferenceQueue<K> q) {
            super(key, q);
            hash = System.identityHashCode(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o instanceof WeakKey) {
                return super.get() == ((WeakKey<?>) o).get();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
