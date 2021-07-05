/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean;

import au.gov.aims.ereefs.NotImplementedException;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * FullTreeSet is a generic Java object which can be used as a
 * direct replacement for a TreeSet.
 * It's just like a TreeSet, but it allows duplicates.
 *
 * @param <T> Class of the elements in the tree.
 */
public class FullTreeSet<T extends Comparable<T>> implements SortedSet<T> {
    private SortedMap<T, Counter> dataMap;
    private long size;

    public FullTreeSet() {
        this(null);
    }

    public FullTreeSet(Comparator<? super T> comparator) {
        this.dataMap = new TreeMap<T, Counter>(comparator);
    }

    @Override
    public Comparator<? super T> comparator() {
        return this.dataMap.comparator();
    }

    @Override
    public boolean add(T t) {
        Counter counter = this.dataMap.get(t);
        if (counter == null) {
            counter = new Counter(1);
            this.dataMap.put(t, counter);
        } else {
            counter.count++;
        }

        this.size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        Counter counter = this.dataMap.get(o);
        if (counter == null) {
            return false;
        }

        if (--counter.count == 0) {
            this.dataMap.remove(o);
        }

        this.size--;
        return true;
    }

    @Override
    public T first() {
        return this.dataMap.firstKey();
    }

    @Override
    public T last() {
        return this.dataMap.lastKey();
    }

    public T get(long index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.realSize());
        }

        int i=0;
        for (Map.Entry<T, Counter> dataEntry : this.dataMap.entrySet()) {
            Counter cnt = dataEntry.getValue();
            i += cnt.count;
            if (i > index) {
                return dataEntry.getKey();
            }
        }

        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.realSize());
    }

    @Override
    public int size() {
        return (int)this.size;
    }
    public long realSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.dataMap.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.dataMap.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            this.add(t);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (T key : this.dataMap.keySet()) {
            if (!c.contains(key)) {
                Counter counter = this.dataMap.remove(key);
                if (counter != null) {
                    this.size -= counter.count;
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object key : c) {
            Counter counter = this.dataMap.remove(key);
            if (counter != null) {
                this.size -= counter.count;
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        this.dataMap.clear();
        this.size = 0;
    }


    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new NotImplementedException();
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new NotImplementedException();
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new NotImplementedException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new NotImplementedException();
    }

    @Override
    public Object[] toArray() {
        throw new NotImplementedException();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new NotImplementedException();
    }

    private class Counter {
        public long count;
        public Counter(int initialCount) {
            this.count = initialCount;
        }
    }
}
