package com.talang.surfing.segment.util;

public class SegmentException extends Exception {

    public SegmentException(String name) {
        super(name);
    }

    public SegmentException(String name, Throwable throwable) {
        super(name, throwable);
    }
}
