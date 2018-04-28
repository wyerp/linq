package com.bestvike.linq.impl;

import com.bestvike.collections.generic.IEqualityComparer;
import com.bestvike.function.Func1;
import com.bestvike.function.Func2;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.exception.Errors;

/**
 * Created by 许崇雷 on 2017/7/12.
 */
public final class GroupedEnumerable2<TSource, TKey, TElement, TResult> implements IEnumerable<TResult> {
    private final IEnumerable<TSource> source;
    private final Func1<TSource, TKey> keySelector;
    private final Func1<TSource, TElement> elementSelector;
    private final IEqualityComparer<TKey> comparer;
    private final Func2<TKey, IEnumerable<TElement>, TResult> resultSelector;

    public GroupedEnumerable2(IEnumerable<TSource> source, Func1<TSource, TKey> keySelector, Func1<TSource, TElement> elementSelector, Func2<TKey, IEnumerable<TElement>, TResult> resultSelector, IEqualityComparer<TKey> comparer) {
        if (source == null)
            throw Errors.argumentNull("source");
        if (keySelector == null)
            throw Errors.argumentNull("keySelector");
        if (elementSelector == null)
            throw Errors.argumentNull("elementSelector");
        if (resultSelector == null)
            throw Errors.argumentNull("resultSelector");
        this.source = source;
        this.keySelector = keySelector;
        this.elementSelector = elementSelector;
        this.comparer = comparer;
        this.resultSelector = resultSelector;
    }

    @Override
    public IEnumerator<TResult> enumerator() {
        Lookup<TKey, TElement> lookup = Lookup.create(this.source, this.keySelector, this.elementSelector, this.comparer);
        return lookup.applyResultSelector(this.resultSelector).enumerator();
    }
}