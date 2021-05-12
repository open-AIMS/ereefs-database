/*
 *  Copyright (C) 2017 Australian Institute of Marine Science
 *
 *  Contact: Gael Lafond <g.lafond@aims.gov.au>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
