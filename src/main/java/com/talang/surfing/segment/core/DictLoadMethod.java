package com.talang.surfing.segment.core;

/**
 * @author: wangwanbao
 * @create: 2021-03-10 17:06
 **/
public enum  DictLoadMethod {

    MONGODB("mongodb"), FILESYSTEM("filesystem");

    DictLoadMethod(String method) {
        this.method = method;
    }

    private String method;

    public String getMethod() {
        return method;
    }


}
