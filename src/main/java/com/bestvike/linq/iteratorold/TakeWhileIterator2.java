package com.bestvike.linq.iteratorold;

import com.bestvike.function.Func2;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.iterator.AbstractIterator;

/**
 * Created by 许崇雷 on 2017/7/16.
 */
final class TakeWhileIterator2<TSource> extends AbstractIterator<TSource> {
    private final IEnumerable<TSource> source;
    private final Func2<TSource, Integer, Boolean> predicate;
    private IEnumerator<TSource> enumerator;
    private int index;

    public TakeWhileIterator2(IEnumerable<TSource> source, Func2<TSource, Integer, Boolean> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public AbstractIterator<TSource> clone() {
        return new TakeWhileIterator2<>(this.source, this.predicate);
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                this.index = -1;
                this.enumerator = this.source.enumerator();
                this.state = 2;
            case 2:
                if (this.enumerator.moveNext()) {
                    TSource item = this.enumerator.current();
                    this.index = Math.addExact(this.index, 1);
                    if (this.predicate.apply(item, this.index)) {
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

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
        }
        super.close();
    }
}
