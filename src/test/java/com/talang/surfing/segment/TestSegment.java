package com.talang.surfing.segment;

import com.talang.surfing.segment.core.Lexeme;
import com.talang.surfing.segment.core.WwbSegmenter;
import com.talang.surfing.segment.dic.DictionaryFileSystem;
import com.talang.surfing.segment.dic.SegmentConfig;
import com.talang.surfing.segment.util.SegmentMode;

import java.io.StringReader;
import java.util.List;

public class TestSegment {

    public static void main(String[] args) {
        String input = "微卡固/VIKAGU 螺丝胶VG263B 威卡固螺纹锁高强度耐高温密封单组分M20强力胶水 50ml";
        SegmentConfig segmentConfig = SegmentConfig.builder().build();
        segmentConfig.setPath("dict");
        DictionaryFileSystem.initial(segmentConfig);
        StringReader stringReader = new StringReader(input);
        WwbSegmenter wwbSegmenter = new WwbSegmenter(stringReader, SegmentMode.MAX, true);
        List<Lexeme> result = wwbSegmenter.getResult();
        for (Lexeme l : result) {
            System.out.println(l);
        }
        System.out.println("--------");
    }
}

