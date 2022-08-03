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

// Based WeakHashMap but using identity
public class WeakIdentityHashMap<K, V> extends AbstractMap<K, V> {
    private final Map<WeakKey<K>, V> map;
    private final transient ReferenceQueue<K> queue = new ReferenceQueue<>();

    /**
     * Constructs a new, empty identity hash map with a default initial
     * size (16).
     */
    public WeakIdentityHashMap() {
        map = new ConcurrentHashMap<>(16);
    }

    private Map<WeakKey<K>, V> getMap() {
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
        return getMap().get(new WeakKey<>(key, null));
    }

    @Override
    public V put(K key, V value) {
        return getMap().put(new WeakKey<>(key, queue), value);
    }

    @Override
    public V remove(Object key) {
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

    private static final class WeakKey<K> extends WeakReference<K> {
        private final int hash;

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