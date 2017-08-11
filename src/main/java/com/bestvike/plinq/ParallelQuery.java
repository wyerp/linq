//package com.bestvike.plinq;
//
//import com.bestvike.linq.IEnumerable;
//
///**
// * Created by 许崇雷 on 2017/8/7.
// */
///// <summary>
///// Represents a parallel sequence.
///// </summary>
//public class ParallelQuery<TSource> implements IEnumerable<TSource>
//    {
//            internal ParallelQuery(QuerySettings settings)
//            : base(settings)
//            {
//            }
//
//            internal sealed override ParallelQuery<TCastTo> Cast<TCastTo>()
//        {
//        return ParallelEnumerable.Select<TSource, TCastTo>(this, elem => (TCastTo)(object)elem);
//        }
//
//        internal sealed override ParallelQuery<TCastTo> OfType<TCastTo>()
//        {
//        // @PERF: Currently defined in terms of other operators. This isn't the most performant
//        //      solution (because it results in two operators) but is simple to implement.
//        return this
//        .Where<TSource>(elem => elem is TCastTo)
//        .Select<TSource, TCastTo>(elem => (TCastTo)(object)elem);
//        }
//
//        internal override IEnumerator GetEnumeratorUntyped()
//        {
//        return ((IEnumerable<TSource>)this).GetEnumerator();
//        }
//
///// <summary>
///// Returns an enumerator that iterates through the sequence.
///// </summary>
///// <returns>An enumerator that iterates through the sequence.</returns>
//public virtual IEnumerator<TSource> GetEnumerator()
//        {
//        Contract.Assert(false, "The derived class must override this method.");
//        throw new NotSupportedException();
//        }
//        }