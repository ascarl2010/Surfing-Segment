package com.talang.surfing.segment.core;

public enum DictType {
    SN("sn", 0), BRAND("brand", 1), GOODS("goods", 2), MAIN("main", 3), CAT("cat", 4),
    QUANTIFIER("quantifier", 5), DIGITAL("digital", 6), SN_DICT("sn_dict", 7), SPLIT("split", 8), UNKNOWN("unkonwn", 9);
    private String name;

    private int code;

    private DictType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public int getCode() {
        return this.code;
    }

    public static DictType getDictType(String name) {
        for (DictType dt : values()) {
            if (dt.name.equals(name)) {
                return dt;
            }
        }
        return null;
    }

    public static DictType getDictTypeByCode(int code) {
        for (DictType dt : values()) {
            if (dt.code == code) {
                return dt;
            }
        }
        return null;
    }

    public static DictType getDictType(String name, DictType defaultDictType) {
        DictType returnType = getDictType(name);
        return null == returnType ? defaultDictType : returnType;
    }

}
