package com.bestvike.linq.iteratorold;

import com.bestvike.function.Func1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.iterator.AbstractIterator;

/**
 * Created by 许崇雷 on 2017/7/16.
 */
final class TakeWhileIterator<TSource> extends AbstractIterator<TSource> {
    private final IEnumerable<TSource> source;
    private final Func1<TSource, Boolean> predicate;
    private IEnumerator<TSource> enumerator;

    public TakeWhileIterator(IEnumerable<TSource> source, Func1<TSource, Boolean> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public AbstractIterator<TSource> clone() {
        return new TakeWhileIterator<>(this.source, this.predicate);
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                this.enumerator = this.source.enumerator();
                this.state = 2;
            case 2:
                if (this.enumerator.moveNext()) {
                    TSource item = this.enumerator.current();
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

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
        }
        super.close();
    }
}
