package com.talang.surfing.segment.dic;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.google.common.collect.Maps;
import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.util.Constant;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author: wangwanbao
 * @create: 2021-03-10 16:10
 **/
public class DictionaryFileSystem extends Dictionary {
    protected DictionaryFileSystem(SegmentConfig segmentConfig) {
        super(segmentConfig);
    }

    public static synchronized void initial(SegmentConfig segmentConfig) {
        if (singleton == null) {
            synchronized (Dictionary.class) {
                if (singleton == null) {
                    //主词典
                    DictSegment mainDict = new DictSegment((char) 0);
                    singleton = new DictionaryFileSystem(segmentConfig);
                    //加载主词典
                    loadDict(segmentConfig, mainDict);
                    //加载同义词典
                    loadSynoDict(segmentConfig, getSingleton().mainDict);
                }
            }
        }
    }

    private static void loadSynoDict(SegmentConfig segmentConfig, DictSegment mainDict) {
        ClassPathResource resource = new ClassPathResource(segmentConfig.getPath());
        File[] dictFiles = new File(resource.getFile(), "syno").listFiles((dir, name) -> {
            return name.endsWith(Constant.ext);
        });
        for (File dictFile : dictFiles) {
            logger.info("load syno dict " + dictFile.getAbsolutePath());
            String dictName = FileUtil.mainName(dictFile);
            DictType dictType = DictType.getDictType(dictName);
            List<String> words = FileUtil.readLines(dictFile, "utf-8");
            for (String word : words) {
                if (StringUtils.isBlank(word)) {
                    continue;
                }
                if (dictType == DictType.SN) {
                    addSnSynos(word.split(","), getSingleton().synonyms);
                } else {
                    String[] items = word.split("[/,]");
                    addSpecialWord(items, mainDict, getSingleton().synonyms, dictType);
                }
            }
        }
    }

    private static void loadDict(SegmentConfig segmentConfig, DictSegment mainDict) {
        ClassPathResource resource = new ClassPathResource(segmentConfig.getPath());
        File[] dictFiles = new File(resource.getFile(), "main").listFiles((dir, name) -> {
            return name.endsWith(Constant.ext);
        });
        Map<String, String> synonyms = Maps.newHashMap();
        //加载主词典，目录下所有的.dic为扩展名的词典
        for (File dictFile : dictFiles) {
            logger.info("load dict " + dictFile.getAbsolutePath());
            String dictName = FileUtil.mainName(dictFile);
            DictType dictType = DictType.getDictType(dictName);
            List<String> words = FileUtil.readLines(dictFile, "utf-8");
            for (String word : words) {
                if (StringUtils.isBlank(word)) {
                    continue;
                }
                if (word.contains("/") || word.contains(",")) {
                    String[] items = word.split("[/,]");
                    addSpecialWord(items, mainDict, synonyms, dictType);
                } else {
                    mainDict.fillSegment(word.trim().toCharArray(), dictType);
                }
            }
        }
        getSingleton().synonyms = synonyms;
        getSingleton().setMainDict(mainDict);
    }

}
