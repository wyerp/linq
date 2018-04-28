package com.bestvike.linq.impl;

import com.bestvike.collections.generic.Array;
import com.bestvike.collections.generic.EqualityComparer;
import com.bestvike.collections.generic.IEqualityComparer;
import com.bestvike.function.Func1;
import com.bestvike.function.Func2;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.IGrouping;
import com.bestvike.linq.IListEnumerable;
import com.bestvike.linq.ILookup;
import com.bestvike.linq.enumerable.EmptyEnumerable;
import com.bestvike.linq.enumerator.AbstractEnumerator;
import com.bestvike.linq.enumerator.ArrayEnumerator;
import com.bestvike.linq.exception.Errors;
import com.bestvike.linq.impl.partition.IIListProvider;
import com.bestvike.linq.iterator.AbstractIterator;
import com.bestvike.linq.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017/7/11.
 */
public final class Lookup<TKey, TElement> implements IIListProvider<IGrouping<TKey, TElement>>, ILookup<TKey, TElement> {
    private final IEqualityComparer<TKey> comparer;
    private Array<Grouping> groupings;
    private Grouping lastGrouping;
    private Grouping nullKeyGrouping;
    private int count;

    private Lookup(IEqualityComparer<TKey> comparer) {
        if (comparer == null)
            comparer = EqualityComparer.Default();
        this.comparer = comparer;
        this.groupings = Array.create(7);
    }

    public static <TKey, TElement> Lookup<TKey, TElement> create(IEnumerable<TElement> source, Func1<TElement, TKey> keySelector, IEqualityComparer<TKey> comparer) {
        if (source == null)
            throw Errors.argumentNull("source");
        if (keySelector == null)
            throw Errors.argumentNull("keySelector");
        Lookup<TKey, TElement> lookup = new Lookup<>(comparer);
        for (TElement item : source) {
            lookup.getGrouping(keySelector.apply(item), true).add(item);
        }
        return lookup;
    }

    public static <TSource, TKey, TElement> Lookup<TKey, TElement> create(IEnumerable<TSource> source, Func1<TSource, TKey> keySelector, Func1<TSource, TElement> elementSelector, IEqualityComparer<TKey> comparer) {
        if (source == null)
            throw Errors.argumentNull("source");
        if (keySelector == null)
            throw Errors.argumentNull("keySelector");
        if (elementSelector == null)
            throw Errors.argumentNull("elementSelector");
        Lookup<TKey, TElement> lookup = new Lookup<>(comparer);
        for (TSource item : source) {
            lookup.getGrouping(keySelector.apply(item), true).add(elementSelector.apply(item));
        }
        return lookup;
    }

    public static <TKey, TElement> Lookup<TKey, TElement> createForJoin(IEnumerable<TElement> source, Func1<TElement, TKey> keySelector, IEqualityComparer<TKey> comparer) {
        Lookup<TKey, TElement> lookup = new Lookup<>(comparer);
        for (TElement item : source) {
            TKey key = keySelector.apply(item);
            if (key != null)
                lookup.getGrouping(key, true).add(item);
        }
        return lookup;
    }

    public static <TKey, TElement> Lookup<TKey, TElement> createForFullJoin(IEnumerable<TElement> source, Func1<TElement, TKey> keySelector, IEqualityComparer<TKey> comparer) {
        Lookup<TKey, TElement> lookup = new Lookup<>(comparer);
        for (TElement item : source) {
            TKey key = keySelector.apply(item);
            if (key == null)
                lookup.getNullKeyGrouping().add(item);
            else
                lookup.getGrouping(key, true).add(item);
        }
        return lookup;
    }

    private void resize() {
        int newSize = Math.addExact(Math.multiplyExact(this.count, 2), 1);
        Array<Grouping> newGroupings = Array.create(newSize);
        Grouping g = this.lastGrouping;
        do {
            g = g.next;
            int index = g.hashCode % newSize;
            g.hashNext = newGroupings.get(index);
            newGroupings.set(index, g);
        } while (g != this.lastGrouping);
        this.groupings = newGroupings;
    }

    private int hashCode(TKey key) {
        //Microsoft DevDivBugs 171937. work around comparer implementations that throw when passed null
        return (key == null) ? 0 : this.comparer.hashCode(key) & 0x7FFFFFFF;
    }

    private Grouping createGrouping(TKey key, int hashCode) {
        if (this.count == this.groupings.length())
            this.resize();
        int index = hashCode % this.groupings.length();
        Grouping g = new Grouping();
        g.key = key;
        g.hashCode = hashCode;
        g.elements = Array.create(1);
        g.hashNext = this.groupings.get(index);
        this.groupings.set(index, g);
        if (this.lastGrouping == null) {
            g.next = g;
        } else {
            g.next = this.lastGrouping.next;
            this.lastGrouping.next = g;
        }
        this.lastGrouping = g;
        this.count++;
        return g;
    }

    private Grouping getNullKeyGrouping() {
        if (this.nullKeyGrouping == null)
            this.nullKeyGrouping = this.createGrouping(null, this.hashCode(null));
        return this.nullKeyGrouping;
    }

    private Grouping getGrouping(TKey key, boolean create) {
        int hashCode = this.hashCode(key);
        for (Grouping g = this.groupings.get(hashCode % this.groupings.length()); g != null; g = g.hashNext)
            if (g.hashCode == hashCode && this.comparer.equals(g.key, key) && g != this.nullKeyGrouping)
                return g;
        return create ? this.createGrouping(key, hashCode) : null;
    }

    public Grouping fetchGrouping(TKey key) {
        if (key == null)
            return null;
        Grouping g = this.getGrouping(key, false);
        if (g != null)
            g.fetched = true;
        return g;
    }

    public IEnumerable<TElement> fetch(TKey key) {
        Grouping grouping = this.fetchGrouping(key);
        return grouping == null ? EmptyEnumerable.Instance() : grouping;
    }

    public <TResult> IEnumerable<TResult> applyResultSelector(Func2<TKey, IEnumerable<TElement>, TResult> resultSelector) {
        return new ApplyResultSelector<>(resultSelector);
    }

    public IEnumerator<Grouping> unfetchedEnumerator() {
        return new UnfetchedLookupEnumerator();
    }

    @Override
    public IEnumerator<IGrouping<TKey, TElement>> enumerator() {
        return new LookupEnumerator();
    }

    @Override
    public IEnumerable<TElement> get(TKey key) {
        Grouping grouping = this.getGrouping(key, false);
        return grouping == null ? EmptyEnumerable.Instance() : grouping;
    }

    @Override
    public boolean containsKey(TKey key) {
        return this.getGrouping(key, false) != null;
    }

    @Override
    public int internalSize() {
        return this.count;
    }

    @Override
    public boolean internalContains(IGrouping<TKey, TElement> value) {
        for (IGrouping<TKey, TElement> g : this)
            if (Objects.equals(g, value)) return true;
        return false;
    }

    @Override
    public Array<IGrouping<TKey, TElement>> internalToArray() {
        Array<IGrouping<TKey, TElement>> array = Array.create(this.count);
        int index = 0;
        for (IGrouping<TKey, TElement> g : this)
            array.set(index++, g);
        return array;
    }

    @Override
    public IGrouping<TKey, TElement>[] internalToArray(Class<IGrouping<TKey, TElement>> clazz) {
        IGrouping<TKey, TElement>[] array = ArrayUtils.newInstance(clazz, this.count);
        int index = 0;
        for (IGrouping<TKey, TElement> g : this)
            array[index++] = g;
        return array;
    }

    @Override
    public List<IGrouping<TKey, TElement>> internalToList() {
        List<IGrouping<TKey, TElement>> list = new ArrayList<>(this.count);
        for (IGrouping<TKey, TElement> g : this)
            list.add(g);
        return list;
    }

    private final class LookupEnumerator extends AbstractEnumerator<IGrouping<TKey, TElement>> {
        private Grouping g;

        @Override
        public boolean moveNext() {
            do {
                switch (this.state) {
                    case 0:
                        this.g = Lookup.this.lastGrouping;
                        if (this.g == null) {
                            this.close();
                            return false;
                        }
                        this.state = 2;
                        break;
                    case 1:
                        if (this.g == Lookup.this.lastGrouping) {
                            this.close();
                            return false;
                        }
                        this.state = 2;
                    case 2:
                        this.g = this.g.next;
                        this.current = this.g;
                        this.state = 1;
                        return true;
                    default:
                        return false;
                }
            } while (true);
        }

        @Override
        public void close() {
            this.g = null;
            super.close();
        }
    }

    private final class UnfetchedLookupEnumerator extends AbstractEnumerator<Grouping> {
        private Grouping g;

        @Override
        public boolean moveNext() {
            do {
                switch (this.state) {
                    case 0:
                        this.g = Lookup.this.lastGrouping;
                        if (this.g == null) {
                            this.close();
                            return false;
                        }
                        this.state = 2;
                        break;
                    case 1:
                        if (this.g == Lookup.this.lastGrouping) {
                            this.close();
                            return false;
                        }
                        this.state = 2;
                    case 2:
                        this.state = 1;
                        this.g = this.g.next;
                        if (this.g.fetched)
                            break;
                        this.current = this.g;
                        return true;
                    default:
                        return false;
                }
            } while (true);
        }

        @Override
        public void close() {
            this.g = null;
            super.close();
        }
    }

    private final class ApplyResultSelector<TResult> extends AbstractIterator<TResult> {
        private final Func2<TKey, IEnumerable<TElement>, TResult> resultSelector;
        private IEnumerator<IGrouping<TKey, TElement>> enumerator;

        private ApplyResultSelector(Func2<TKey, IEnumerable<TElement>, TResult> resultSelector) {
            this.resultSelector = resultSelector;
        }

        @Override
        public AbstractIterator<TResult> clone() {
            return new ApplyResultSelector<>(this.resultSelector);
        }

        @Override
        public boolean moveNext() {
            switch (this.state) {
                case 1:
                    this.enumerator = Lookup.this.enumerator();
                    this.state = 2;
                case 2:
                    if (this.enumerator.moveNext()) {
                        IGrouping<TKey, TElement> grouping = this.enumerator.current();
                        this.current = this.resultSelector.apply(grouping.getKey(), grouping);
                        return true;
                    }
                    this.close();
                    return false;
                default:
                    return false;
            }
        }

        @Override
        public void close() {
            if (this.enumerator != null) {
                this.enumerator.close();
                this.enumerator = null;
            }
            super.close();
        }
    }

    public final class Grouping implements IListEnumerable<TElement>, IGrouping<TKey, TElement> {
        private TKey key;
        private int hashCode;
        private Array<TElement> elements;
        private int count;
        private Grouping hashNext;
        private Grouping next;
        private boolean fetched;

        private Grouping() {
        }

        private void add(TElement element) {
            if (this.elements.length() == this.count)
                this.elements.resize(Math.multiplyExact(this.count, 2));
            this.elements.set(this.count, element);
            this.count++;
        }

        @Override
        public IEnumerator<TElement> enumerator() {
            return new ArrayEnumerator<>(this.elements, 0, this.count);
        }

        @Override
        public TKey getKey() {
            return this.key;
        }

        @Override
        public TElement internalGet(int index) {
            if (index < 0 || index >= this.count)
                throw Errors.argumentOutOfRange("index");
            return this.elements.get(index);
        }

        @Override
        public int internalSize() {
            return this.count;
        }

        @Override
        public boolean internalContains(TElement value) {
            return this.elements.contains(value, 0, this.count);
        }

        @Override
        public Array<TElement> internalToArray() {
            Array<TElement> array = Array.create(this.count);
            Array.copy(this.elements, 0, array, 0, this.count);
            return array;
        }

        @Override
        public TElement[] internalToArray(Class<TElement> clazz) {
            TElement[] array = ArrayUtils.newInstance(clazz, this.count);
            Array.copy(this.elements, 0, array, 0, this.count);
            return array;
        }

        @Override
        public List<TElement> internalToList() {
            List<TElement> list = new ArrayList<>(this.count);
            for (int i = 0; i < this.count; i++)
                list.add(this.elements.get(i));
            return list;
        }
    }
}
