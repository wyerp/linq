package com.bestvike.linq.iteratorold;

import com.bestvike.function.Func2;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.iterator.AbstractIterator;

/**
 * Created by 许崇雷 on 2017/7/16.
 */
final class SelectManyIterator2<TSource, TResult> extends AbstractIterator<TResult> {
    private final IEnumerable<TSource> source;
    private final Func2<TSource, Integer, IEnumerable<TResult>> selector;
    private IEnumerator<TSource> enumerator;
    private IEnumerator<TResult> selectorEnumerator;
    private int index;

    public SelectManyIterator2(IEnumerable<TSource> source, Func2<TSource, Integer, IEnumerable<TResult>> selector) {
        this.source = source;
        this.selector = selector;
    }

    @Override
    public AbstractIterator<TResult> clone() {
        return new SelectManyIterator2<>(this.source, this.selector);
    }

    @Override
    public boolean moveNext() {
        do {
            switch (this.state) {
                case 1:
                    this.index = -1;
                    this.enumerator = this.source.enumerator();
                    this.state = 2;
                case 2:
                    if (this.enumerator.moveNext()) {
                        TSource item = this.enumerator.current();
                        this.index = Math.addExact(this.index, 1);
                        this.selectorEnumerator = this.selector.apply(item, this.index).enumerator();
                        this.state = 3;
                        break;
                    }
                    this.close();
                    return false;
                case 3:
                    if (this.selectorEnumerator.moveNext()) {
                        this.current = this.selectorEnumerator.current();
                        return true;
                    }
                    this.selectorEnumerator.close();
                    this.state = 2;
                    break;
                default:
                    return false;
            }
        } while (true);
    }

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
        }
        if (this.selectorEnumerator != null) {
            this.selectorEnumerator.close();
            this.selectorEnumerator = null;
        }
        super.close();
    }
}
