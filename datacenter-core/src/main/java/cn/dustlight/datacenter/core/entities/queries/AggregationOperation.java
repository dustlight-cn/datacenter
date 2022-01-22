package cn.dustlight.datacenter.core.entities.queries;

public enum AggregationOperation {
    /**
     * 平均值
     */
    AVG,
    /**
     * 求和
     */
    SUM,
    /**
     * 计数
     */
    COUNT,
    /**
     * 最大值
     */
    MAX,
    /**
     * 最小值
     */
    MIN,
    /**
     * term
     */
    TERM,
    /**
     * 直方图
     */
    HISTOGRAM,
    /**
     * 时间直方图
     */
    DATE_HISTOGRAM,
}
