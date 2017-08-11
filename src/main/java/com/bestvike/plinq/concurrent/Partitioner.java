package com.bestvike.plinq.concurrent;

import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.exception.NotSupportedException;

import java.util.List;

/**
 * Created by 许崇雷 on 2017/8/7.
 */
/// <summary>表示将数据源拆分为多个分区的特定方式。</summary>
/// <typeparam name="TSource">集合中元素的类型。</typeparam>
public abstract class Partitioner<TSource> {
    /// <summary>获取是否可以动态创建更多的分区。</summary>
    /// <returns>
    ///   true <see cref="T:System.Collections.Concurrent.Partitioner`1" /> 动态可创建分区，则根据请求; false <see cref="T:System.Collections.Concurrent.Partitioner`1" /> 只能以静态方式分配分区。
    /// </returns>
    public boolean isSupportsDynamicPartitions() {
        return false;
    }

    /// <summary>创建新的分区程序实例。</summary>
    protected Partitioner() {
    }

    /// <summary>分区到给定数目的分区的基础集合。</summary>
    /// <param name="partitionCount">若要创建的分区数。</param>
    /// <returns>
    ///   一个列表包含 <paramref name="partitionCount" /> 枚举器。
    /// </returns>
    public abstract List<IEnumerator<TSource>> GetPartitions(int partitionCount);

    /// <summary>创建可以分区成可变数目的分区的基础集合的对象。</summary>
    /// <returns>一个对象，可以在基础数据源上创建分区。</returns>
    /// <exception cref="T:System.NotSupportedException">
    ///   基类不支持动态分区。
    ///    您必须在派生类中实现它。
    /// </exception>
    public IEnumerable<TSource> GetDynamicPartitions() {
        throw new NotSupportedException("Dynamic partitions are not supported by this partitioner.");
    }
}