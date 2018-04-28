package com.bestvike.linq.iteratorold;

import com.bestvike.collections.generic.Array;
import com.bestvike.function.Func1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.iterator.AbstractIterator;
import com.bestvike.linq.iterator.Iterator;
import com.bestvike.linq.util.LambdaUtils;

/**
 * Created by 许崇雷 on 2017/7/17.
 */
final class WhereArrayIterator<TSource> extends Iterator<TSource> {
    private final Array<TSource> source;
    private final Func1<TSource, Boolean> predicate;
    private int index;

    public WhereArrayIterator(Array<TSource> source, Func1<TSource, Boolean> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public AbstractIterator<TSource> clone() {
        return new WhereArrayIterator<>(this.source, this.predicate);
    }

    @Override
    public <TResult> IEnumerable<TResult> _select(Func1<TSource, TResult> selector) {
        return new WhereSelectArrayIterator<>(this.source, this.predicate, selector);
    }

    @Override
    public IEnumerable<TSource> _where(Func1<TSource, Boolean> predicate) {
        return new WhereArrayIterator<>(this.source, LambdaUtils.combinePredicates(this.predicate, predicate));
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                while (this.index < this.source.length()) {
                    TSource item = this.source.get(this.index);
                    this.index++;
                    if (this.predicate.apply(item)) {
                        this.current = item;
                        return true;
                    }
                }
                this.close();
                return false;
            default:
                return false;
        }
    }
}
