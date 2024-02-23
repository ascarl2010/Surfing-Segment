package com.talang.surfing.segment.util;

public enum SegmentMode {

    //distinct默认MOST模式
    MOST("most"), MAX("max"), DISTINCT("distinct");
    // 成员变量
    private String name;

    // 构造方法
    private SegmentMode(String name) {
        this.name = name;
    }

    // get set 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static SegmentMode getSegmentMode(String name) {
        for (SegmentMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return null;
    }

}
