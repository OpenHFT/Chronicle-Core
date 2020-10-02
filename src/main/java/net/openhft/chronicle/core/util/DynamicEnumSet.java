package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.pool.EnumCache;

import java.util.*;

public class DynamicEnumSet<E extends CoreDynamicEnum<E>> extends AbstractSet<E> implements Cloneable {
    final EnumCache<E> universe;

    /**
     * Bit vector representation of this set.  The ith bit of the jth
     * element of this array represents the  presence of universe[64*j +i]
     * in this set.
     */
    private long elementsArr[];
    private boolean allOf;
    // Redundant - maintained for performance
    private int size = 0, length = 0;

    protected DynamicEnumSet(Class<E> eClass) {
        this.universe = EnumCache.of(eClass);
        elementsArr = new long[(universe.size() + 63) >>> 6];
    }

    /**
     * Creates an empty enum set with the specified element type.
     *
     * @param <E>      The class of the elements in the set
     * @param universe the class object of the element type for this enum
     *                 set
     * @return An empty enum set of the specified type.
     * @throws NullPointerException if <tt>universe</tt> is null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> noneOf(Class<E> universe) {
        return new DynamicEnumSet<>(universe);
    }

    /**
     * Creates an enum set containing all of the elements in the specified
     * element type.
     *
     * @param <E>      The class of the elements in the set
     * @param universe the class object of the element type for this enum
     *                 set
     * @return An enum set containing all the elements in the specified type.
     * @throws NullPointerException if <tt>universe</tt> is null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> allOf(Class<E> universe) {
        DynamicEnumSet<E> result = noneOf(universe);
        result.allOf = true;
        return result;
    }

    /**
     * Creates an enum set with the same element type as the specified enum
     * set, initially containing the same elements (if any).
     *
     * @param <E> The class of the elements in the set
     * @param s   the enum set from which to initialize this enum set
     * @return A copy of the specified enum set.
     * @throws NullPointerException if <tt>s</tt> is null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> copyOf(DynamicEnumSet<E> s) {
        return s.clone();
    }

    /**
     * Creates an enum set initialized from the specified collection.  If
     * the specified collection is an <tt>DynamicEnumSet</tt> instance, this static
     * factory method behaves identically to {@link #copyOf(DynamicEnumSet)}.
     * Otherwise, the specified collection must contain at least one element
     * (in order to determine the new enum set's element type).
     *
     * @param <E> The class of the elements in the collection
     * @param c   the collection from which to initialize this enum set
     * @return An enum set initialized from the given collection.
     * @throws IllegalArgumentException if <tt>c</tt> is not an
     *                                  <tt>DynamicEnumSet</tt> instance and contains no elements
     * @throws NullPointerException     if <tt>c</tt> is null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof DynamicEnumSet) {
            return ((DynamicEnumSet<E>) c).clone();
        } else {
            if (c.isEmpty())
                throw new IllegalArgumentException("Collection is empty");
            Iterator<E> i = c.iterator();
            E first = i.next();
            DynamicEnumSet<E> result = DynamicEnumSet.of(first);
            while (i.hasNext())
                result.add(i.next());
            return result;
        }
    }

    /**
     * Creates an enum set with the same element type as the specified enum
     * set, initially containing all the elements of this type that are
     * <i>not</i> contained in the specified set.
     *
     * @param <E> The class of the elements in the enum set
     * @param s   the enum set from whose complement to initialize this enum set
     * @return The complement of the specified set in this set
     * @throws NullPointerException if <tt>s</tt> is null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> complementOf(DynamicEnumSet<E> s) {
        DynamicEnumSet<E> result = copyOf(s);
        result.complement();
        return result;
    }

    /**
     * Creates an enum set initially containing the specified element.
     * <p>
     * Overloadings of this method exist to initialize an enum set with
     * one through five elements.  A sixth overloading is provided that
     * uses the varargs feature.  This overloading may be used to create
     * an enum set initially containing an arbitrary number of elements, but
     * is likely to run slower than the overloadings that do not use varargs.
     *
     * @param <E> The class of the specified element and of the set
     * @param e   the element that this set is to contain initially
     * @return an enum set initially containing the specified element
     * @throws NullPointerException if <tt>e</tt> is null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> of(E e) {
        DynamicEnumSet<E> result = noneOf((Class) e.getClass());
        result.add(e);
        return result;
    }

    /**
     * Creates an enum set initially containing the specified elements.
     * <p>
     * Overloadings of this method exist to initialize an enum set with
     * one through five elements.  A sixth overloading is provided that
     * uses the varargs feature.  This overloading may be used to create
     * an enum set initially containing an arbitrary number of elements, but
     * is likely to run slower than the overloadings that do not use varargs.
     *
     * @param <E> The class of the parameter elements and of the set
     * @param e1  an element that this set is to contain initially
     * @param e2  another element that this set is to contain initially
     * @return an enum set initially containing the specified elements
     * @throws NullPointerException if any parameters are null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> of(E e1, E e2) {
        DynamicEnumSet<E> result = noneOf((Class) e1.getClass());
        result.add(e1);
        result.add(e2);
        return result;
    }

    /**
     * Creates an enum set initially containing the specified elements.
     * <p>
     * Overloadings of this method exist to initialize an enum set with
     * one through five elements.  A sixth overloading is provided that
     * uses the varargs feature.  This overloading may be used to create
     * an enum set initially containing an arbitrary number of elements, but
     * is likely to run slower than the overloadings that do not use varargs.
     *
     * @param <E> The class of the parameter elements and of the set
     * @param e1  an element that this set is to contain initially
     * @param e2  another element that this set is to contain initially
     * @param e3  another element that this set is to contain initially
     * @return an enum set initially containing the specified elements
     * @throws NullPointerException if any parameters are null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> of(E e1, E e2, E e3) {
        DynamicEnumSet<E> result = noneOf((Class) e1.getClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    /**
     * Creates an enum set initially containing the specified elements.
     * <p>
     * Overloadings of this method exist to initialize an enum set with
     * one through five elements.  A sixth overloading is provided that
     * uses the varargs feature.  This overloading may be used to create
     * an enum set initially containing an arbitrary number of elements, but
     * is likely to run slower than the overloadings that do not use varargs.
     *
     * @param <E> The class of the parameter elements and of the set
     * @param e1  an element that this set is to contain initially
     * @param e2  another element that this set is to contain initially
     * @param e3  another element that this set is to contain initially
     * @param e4  another element that this set is to contain initially
     * @return an enum set initially containing the specified elements
     * @throws NullPointerException if any parameters are null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> of(E e1, E e2, E e3, E e4) {
        DynamicEnumSet<E> result = noneOf((Class) e1.getClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    /**
     * Creates an enum set initially containing the specified elements.
     * <p>
     * Overloadings of this method exist to initialize an enum set with
     * one through five elements.  A sixth overloading is provided that
     * uses the varargs feature.  This overloading may be used to create
     * an enum set initially containing an arbitrary number of elements, but
     * is likely to run slower than the overloadings that do not use varargs.
     *
     * @param <E> The class of the parameter elements and of the set
     * @param e1  an element that this set is to contain initially
     * @param e2  another element that this set is to contain initially
     * @param e3  another element that this set is to contain initially
     * @param e4  another element that this set is to contain initially
     * @param e5  another element that this set is to contain initially
     * @return an enum set initially containing the specified elements
     * @throws NullPointerException if any parameters are null
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        DynamicEnumSet<E> result = noneOf((Class) e1.getClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

    /**
     * Creates an enum set initially containing the specified elements.
     * This factory, whose parameter list uses the varargs feature, may
     * be used to create an enum set initially containing an arbitrary
     * number of elements, but it is likely to run slower than the overloadings
     * that do not use varargs.
     *
     * @param <E>   The class of the parameter elements and of the set
     * @param first an element that the set is to contain initially
     * @param rest  the remaining elements the set is to contain initially
     * @return an enum set initially containing the specified elements
     * @throws NullPointerException if any of the specified elements are null,
     *                              or if <tt>rest</tt> is null
     */
    @SafeVarargs
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> of(E first, E... rest) {
        DynamicEnumSet<E> result = noneOf((Class) first.getClass());
        result.add(first);
        for (E e : rest)
            result.add(e);
        return result;
    }

    /**
     * Creates an enum set initially containing all of the elements in the
     * range defined by the two specified endpoints.  The returned set will
     * contain the endpoints themselves, which may be identical but must not
     * be out of order.
     *
     * @param <E>  The class of the parameter elements and of the set
     * @param from the first element in the range
     * @param to   the last element in the range
     * @return an enum set initially containing all of the elements in the
     * range defined by the two specified endpoints
     * @throws NullPointerException     if {@code from} or {@code to} are null
     * @throws IllegalArgumentException if {@code from.compareTo(to) > 0}
     */
    public static <E extends CoreDynamicEnum<E>> DynamicEnumSet<E> range(E from, E to) {
        if (from.ordinal() > to.ordinal())
            throw new IllegalArgumentException(from + " > " + to);
        DynamicEnumSet<E> result = noneOf((Class) from.getClass());
        result.addRange(from, to);
        return result;
    }

    /**
     * Returns a copy of this set.
     *
     * @return a copy of this set
     */
    @SuppressWarnings("unchecked")
    public DynamicEnumSet<E> clone() {
        try {
            DynamicEnumSet<E> clone = (DynamicEnumSet<E>) super.clone();
            clone.elementsArr = elementsArr.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Throws an exception if e is not of the correct type for this enum set.
     */
    final void typeCheck(E e) {
        Class<?> eClass = e.getClass();
        Class<?> type = universe.type();
        if (eClass != type && eClass.getSuperclass() != type)
            throw new ClassCastException(eClass + " != " + type);
    }

    void addRange(E from, E to) {
        int fromOordinal = from.ordinal();
        int toOrdinal = to.ordinal();
        addRange(fromOordinal, toOrdinal);
    }

    void addRange(int fromOordinal, int toOrdinal) {
        int fromIndex = fromOordinal >>> 6;
        int toIndex = toOrdinal >>> 6;

        if (fromIndex == toIndex) {
            elementsOr(fromIndex, ~0L >>> (fromOordinal - toOrdinal - 1)
                    << fromOordinal);
        } else {
            elementsOr(fromIndex, ~0L << fromOordinal);
            for (int i = fromIndex + 1; i < toIndex; i++)
                elementsOr(i, ~0L);
            elementsOr(toIndex, ~0L >>> (63 - toOrdinal));
        }
        size += toOrdinal - fromOordinal + 1;
    }

    void complement() {
        for (int i = 0; i < elementsArr.length; i++)
            elements(i, ~elements(i));
        if (elementsArr.length > 0)
            elementsAnd(elementsArr.length - 1, ~0L >>> -length);
        size = universe.size() - size();
        allOf = !allOf;
    }

    /**
     * Returns an iterator over the elements contained in this set.  The
     * iterator traverses the elements in their <i>natural order</i> (which is
     * the order in which the enum constants are declared). The returned
     * Iterator is a "weakly consistent" iterator that will never throw {@link
     * ConcurrentModificationException}.
     *
     * @return an iterator over the elements contained in this set
     */
    @Override
    public Iterator<E> iterator() {
        return new DynamicEnumSetIterator();
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set
     */
    public int size() {
        return (allOf ? universe.size() - length : 0) + size;
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param e element to be checked for containment in this collection
     * @return <tt>true</tt> if this set contains the specified element
     */
    public boolean contains(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        Class<?> type = universe.type();
        if (eClass != type && eClass.getSuperclass() != type)
            return false;

        int eOrdinal = ((Enum<?>) e).ordinal();
        return containsByOrdinal(eOrdinal);
    }

    protected boolean containsByOrdinal(int eOrdinal) {
        if (eOrdinal >= length)
            return allOf;
        return (elements(eOrdinal >>> 6) & (1L << eOrdinal)) != 0;
    }

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if the set changed as a result of the call
     * @throws NullPointerException if <tt>e</tt> is null
     */
    public boolean add(E e) {
        typeCheck(e);

        int eOrdinal = e.ordinal();
        ensureLength(eOrdinal + 1);
        int eWordNum = eOrdinal >>> 6;

        long oldElements = elements(eWordNum);
        elementsOr(eWordNum, 1L << eOrdinal);
        boolean result = (elements(eWordNum) != oldElements);
        if (result)
            size++;
        return result;
    }

    private void ensureLength(int i) {
        if (i > length) {
            ensureCapacity(i >>> 6);
            if (allOf)
                addRange(length, i);
            length = i;
        }
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param e element to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    public boolean remove(Object e) {
        if (e == null)
            return false;
        Class<?> eClass = e.getClass();
        Class<?> type = universe.type();
        if (eClass != type && eClass.getSuperclass() != type)
            return false;
        int eOrdinal = ((Enum<?>) e).ordinal();
        return removeByOrdinal(eOrdinal);
    }

    protected boolean removeByOrdinal(int eOrdinal) {
        int eWordNum = eOrdinal >>> 6;
        if (length <= eOrdinal && allOf) {
            if (eWordNum >= elementsArr.length)
                elementsArr = Arrays.copyOf(elementsArr, eWordNum);
            addRange(length, eOrdinal);
            length = eOrdinal + 1;
        }

        long oldElements = elements(eWordNum);
        elementsAnd(eWordNum, ~(1L << eOrdinal));
        boolean result = (elements(eWordNum) != oldElements);
        if (result)
            size--;
        return result;
    }

    // Modification Operations

    /**
     * Returns <tt>true</tt> if this set contains all of the elements
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements
     * in the specified collection
     * @throws NullPointerException if the specified collection is null
     */
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof DynamicEnumSet))
            return super.containsAll(c);

        DynamicEnumSet<?> es = (DynamicEnumSet<?>) c;
        if (es.universe != universe)
            return es.isEmpty();

        equalizeLength(es);

        for (int i = 0; i < elementsArr.length; i++)
            if ((es.elements(i) & ~elements(i)) != 0)
                return false;
        return true;
    }

    /**
     * Adds all of the elements in the specified collection to this set.
     *
     * @param c collection whose elements are to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws NullPointerException if the specified collection or any of
     *                              its elements are null
     */
    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof DynamicEnumSet))
            return super.addAll(c);

        DynamicEnumSet<?> es = (DynamicEnumSet) c;
        if (es.universe != universe) {
            if (es.isEmpty())
                return false;
            else
                throw new ClassCastException(
                        es.universe + " != " + universe);
        }
        equalizeLength(es);

        for (int i = 0; i < elementsArr.length; i++)
            elementsOr(i, es.elements(i));
        return recalculateSize();
    }

    // Bulk Operations

    /**
     * Removes from this set all of its elements that are contained in
     * the specified collection.
     *
     * @param c elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof DynamicEnumSet))
            return super.removeAll(c);

        DynamicEnumSet<?> es = (DynamicEnumSet<?>) c;
        if (es.universe != universe)
            return false;

        equalizeLength(es);
        for (int i = 0; i < elementsArr.length; i++)
            elementsAnd(i, ~es.elements(i));
        return recalculateSize();
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection.
     *
     * @param c elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof DynamicEnumSet))
            return super.retainAll(c);

        DynamicEnumSet<?> es = (DynamicEnumSet<?>) c;
        if (es.universe != universe) {
            boolean changed = (size != 0);
            clear();
            return changed;
        }

        equalizeLength(es);
        for (int i = 0; i < elementsArr.length; i++)
            elementsAnd(i, es.elements(i));
        return recalculateSize();
    }

    long elements(int i) {
        return elementsArr[i];
    }

    void elements(int i, long v) {
        ensureCapacity(i);
        elementsArr[i] = v;
    }

    void elementsAnd(int i, long v) {
        if (i >= elementsArr.length)
            return;
        elementsArr[i] &= v;
    }

    void elementsOr(int i, long v) {
        ensureCapacity(i);
        elementsArr[i] |= v;
    }

    private void ensureCapacity(int i) {
        if (i >= elementsArr.length)
            elementsArr = Arrays.copyOf(elementsArr, i + 1);
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        Arrays.fill(elementsArr, 0);
        size = 0;
    }

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    public boolean equals(Object o) {
        if (!(o instanceof DynamicEnumSet))
            return super.equals(o);

        DynamicEnumSet<?> es = (DynamicEnumSet<?>) o;
        if (es.universe != universe)
            return size == 0 && es.size == 0;
        equalizeLength(es);
        return Arrays.equals(es.elementsArr, elementsArr);
    }

    protected void equalizeLength(DynamicEnumSet<?> es) {
        ensureLength(es.length);
        es.ensureLength(length);
    }

    /**
     * Recalculates the size of the set.  Returns true if it's changed.
     */
    private boolean recalculateSize() {
        if (elementsArr.length > 0)
            elementsAnd(elementsArr.length - 1, ~0L >>> -length);
        int oldSize = size;
        size = 0;
        for (long elt : elementsArr)
            size += Long.bitCount(elt);

        return size != oldSize;
    }

    class DynamicEnumSetIterator implements Iterator<E> {
        int nextIndex = -1;
        int lastIndex = -1;

        public DynamicEnumSetIterator() {
            findNext();
        }

        @Override
        public boolean hasNext() {
            return nextIndex >= 0;
        }

        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastIndex = nextIndex;
            findNext();
            return universe.forIndex(lastIndex);
        }

        private void findNext() {
            assert nextIndex != Integer.MIN_VALUE;

            int size = universe.size();
            while (++nextIndex < size) {
                if (containsByOrdinal(nextIndex)) {
                    return;
                }
            }
            nextIndex = Integer.MIN_VALUE;
        }

        @Override
        public void remove() {
            if (lastIndex < 0)
                throw new IllegalStateException();
            removeByOrdinal(lastIndex);
        }
    }
}
