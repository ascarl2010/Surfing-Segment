package com.talang.surfing.segment.core;


import com.talang.surfing.segment.util.SegmentException;

public interface ISegmenter {

    void analyzer(Context context) throws SegmentException;

    void reset();

    String getName();

}
