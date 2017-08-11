package com.bestvike.plinq;

/// <summary>
/// The query execution mode is a hint that specifies how the system should handle
/// performance trade-offs when parallelizing queries.
/// </summary>
public enum ParallelExecutionMode {
    /// <summary>
    /// By default, the system will use algorithms for queries
    /// that are ripe for parallelism and will avoid algorithms with high
    /// overheads that will likely result in slow downs for parallel execution.
    /// </summary>
    Default,

    /// <summary>
    /// Parallelize the entire query, even if that means using high-overhead algorithms.
    /// </summary>
    ForceParallelism;
}