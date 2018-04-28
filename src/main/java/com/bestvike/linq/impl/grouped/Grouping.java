package com.bestvike.linq.impl.grouped;

import com.bestvike.collections.generic.Array;
import com.bestvike.collections.generic.IList;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.IGrouping;
import com.bestvike.linq.enumerator.ArrayEnumerator;
import com.bestvike.linq.exception.Errors;
import com.bestvike.linq.util.ArrayUtils;

import java.util.Collection;
import java.util.List;

/**
 * Created by 许崇雷 on 2018-04-28.
 */
public class Grouping<TKey, TElement> implements IGrouping<TKey, TElement>, IList<TElement> {
    private TKey key;
    private int hashCode;
    private Array<TElement> elements;
    private int count;
    private Grouping<TKey, TElement> hashNext;
    private Grouping<TKey, TElement> next;

    private Grouping() {
    }

    void add(TElement element) {
        if (this.elements.length() == this.count)
            this.elements.resize(Math.multiplyExact(this.count, 2));
        this.elements.set(this.count, element);
        this.count++;
    }

    private void trim() {
        if (this.elements.length() != this.count)
            this.elements.resize(this.count);
    }

    public IEnumerator<TElement> enumerator() {
        return new ArrayEnumerator<>(this.elements, 0, this.count);
    }

    @Override
    public TKey getKey() {
        return this.key;
    }

    @Override
    public TElement get(int index) {
        if (index < 0 || index >= this.count)
            throw Errors.argumentOutOfRange("index");
        return this.elements.get(index);
    }

    @Override
    public Collection<TElement> getCollection() {
        return this.elements.getCollection(0, this.count);
    }

    @Override
    public int _getCount() {
        return this.count;
    }

    @Override
    public boolean _contains(TElement item) {
        return this.elements._contains(item, 0, this.count);
    }

    @Override
    public void _copyTo(TElement[] array, int arrayIndex) {
        Array.copy(this.elements, 0, array, arrayIndex, this.count);
    }

    public int indexOf(TElement item) {
        return this.elements.indexOf(item, 0, this.count);
    }

    @Override
    public void _copyTo(Array<TElement> array, int arrayIndex) {
        Array.copy(this.elements, 0, array, arrayIndex, this.count);
    }

    @Override
    public TElement[] _toArray(Class<TElement> clazz) {
        TElement[] array = ArrayUtils.newInstance(clazz, this.count);
        Array.copy(this.elements, 0, array, 0, this.count);
        return array;
    }

    @Override
    public Array<TElement> _toArray() {
        Array<TElement> array = Array.create(this.count);
        Array.copy(this.elements, 0, array, 0, this.count);
        return array;
    }

    @Override
    public List<TElement> _toList() {
        return this.elements._toList(0, this.count);
    }
}
