package com.bestvike.linq.iteratorold;

import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.iterator.AbstractIterator;

/**
 * Created by 许崇雷 on 2017/7/16.
 */
final class TakeIterator<TSource> extends AbstractIterator<TSource> {
    private final IEnumerable<TSource> source;
    private final int count;
    private IEnumerator<TSource> enumerator;
    private int counter;

    public TakeIterator(IEnumerable<TSource> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    public AbstractIterator<TSource> clone() {
        return new TakeIterator<>(this.source, this.count);
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                this.counter = this.count;
                if (this.counter <= 0) {
                    this.close();
                    return false;
                }
                this.enumerator = this.source.enumerator();
                this.state = 2;
            case 2:
                this.counter--;
                if (this.counter >= 0 && this.enumerator.moveNext()) {
                    this.current = this.enumerator.current();
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
