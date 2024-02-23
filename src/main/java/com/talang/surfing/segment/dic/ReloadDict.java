package com.talang.surfing.segment.dic;

/**
 * @author: wangwanbao
 * @create: 2021-03-09 18:56
 **/
public class ReloadDict implements Runnable {

    private final SegmentConfig segmentConfig;

    public ReloadDict(SegmentConfig segmentConfig) {
        this.segmentConfig = segmentConfig;
    }

    @Override
    public void run() {
        try {
            //仅加载同义词典
            DictionaryMongodb.loadSynoData(segmentConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
