package com.talang.surfing.segment.dic;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: wangwanbao
 * @create: 2021-03-09 14:29
 **/
@Data
@Builder
public class SegmentConfig {

    private String user;

    private String password;

    private int port;

    private String host;

    private String database;

    private String collection;

    private String path;

    private String method;

    //多长时间重新加载一次词典
    private int reloadPeriodSeconds;
}
