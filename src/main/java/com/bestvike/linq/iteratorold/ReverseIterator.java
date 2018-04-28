package com.bestvike.linq.iteratorold;

import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.impl.collections.Buffer;
import com.bestvike.linq.iterator.AbstractIterator;

/**
 * Created by 许崇雷 on 2017/7/18.
 */
final class ReverseIterator<TSource> extends AbstractIterator<TSource> {
    private final IEnumerable<TSource> source;
    private Buffer<TSource> buffer;
    private int index;

    public ReverseIterator(IEnumerable<TSource> source) {
        this.source = source;
    }

    @Override
    public AbstractIterator<TSource> clone() {
        return new ReverseIterator<>(this.source);
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                this.buffer = new Buffer<>(this.source);
                this.index = this.buffer.getCount();
                this.state = 2;
            case 2:
                this.index--;
                if (this.index >= 0) {
                    this.current = this.buffer.getItems().get(this.index);
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
        this.buffer = null;
        super.close();
    }
}
