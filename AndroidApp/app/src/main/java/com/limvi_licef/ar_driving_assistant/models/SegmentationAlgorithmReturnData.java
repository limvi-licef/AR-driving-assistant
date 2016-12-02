package com.limvi_licef.ar_driving_assistant.models;

import java.util.List;

/*
 * Data Structure to return both the significant extrema and the processed data from the segmentation algorithm
 */
public class SegmentationAlgorithmReturnData {
    public ExtremaStats extremaStats;
    public List<TimestampedDouble> monotoneValues;

    public SegmentationAlgorithmReturnData(ExtremaStats extremaStats, List<TimestampedDouble> monotoneValues) {
        this.extremaStats = extremaStats;
        this.monotoneValues = monotoneValues;
    }
}
