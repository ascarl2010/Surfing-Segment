/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package com.talang.surfing.segment.dic;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import com.talang.surfing.segment.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 词典管理类,单子模式
 */
public abstract class Dictionary {

    /*
     * 词典单子实例
     */
    protected static Dictionary singleton;

    public DictSegment mainDict = new DictSegment((char) 0);

    //量词词典
    protected DictSegment quantifierDict = new DictSegment((char) 0);

    private Map<String, DictSegment> dictSegments = Maps.newHashMap();

    public Map<String, String> synonyms = Maps.newHashMap();

    protected static Logger logger = ESPluginLoggerFactory.getLogger("Dictionary", Dictionary.class);

    protected SegmentConfig segmentConfig;

    protected Dictionary(SegmentConfig segmentConfig) {
        this.segmentConfig = segmentConfig;
    }

    public void setMainDict(DictSegment dict) {
        this.mainDict = dict;
        dictSegments.put(Constant.MAIN_DICT, dict);
    }

    public void setQuantifierDict(DictSegment dict) {
        this.quantifierDict = dict;
        dictSegments.put(Constant.QUANTIFIER, dict);
    }

    /**
     * 获取词典单子实例
     *
     * @return Dictionary 单例对象
     */
    public static Dictionary getSingleton() {
        if (singleton == null) {
            throw new IllegalStateException("ik dict has not been initialized yet, please call initial method first.");
        }
        return singleton;
    }

    public Hit matchWithHit(char[] charArray, int currentIndex, Hit matchHit) {
        DictSegment ds = matchHit.getMatchedDictSegment();
        return ds.match(charArray, currentIndex, 1, matchHit);
    }


    public Hit matchHit(String dictSegmentName, char[] charArray, int begin, int legnth) {
        DictSegment curDictSegment = dictSegments.get(dictSegmentName);
        return curDictSegment.match(charArray, begin, legnth);
    }

    public Set<String> getSynoymSet(String standard) {
        if (synonyms.containsKey(standard)) {
            return Arrays.stream(synonyms.get(standard).split(",")).collect(Collectors.toSet());
        } else {
            return Sets.newHashSet(standard);
        }
    }

    public Set<String> getSynomSnExceptCurrent(String standard) {
        Set<String> results = getSynoymSet(standard);
        return results.stream().filter(sn -> !sn.equals(standard) && !normalizeSn(sn).equals(standard)).collect(Collectors.toSet());
    }

    public String getSynoValue(String text) {
        if(!synonyms.containsKey(text)) {
            return text;
        }
        return synonyms.get(text).split(",")[0];
    }

    private static String normalizeSn(String sn) {
        sn = sn/*.toLowerCase()*/.trim();
        sn = sn.replaceAll("-", " ");
        sn = sn.replaceAll("\\*", " ");
        return sn;
    }

    protected static void addSnSynos(String[] items, Map<String, String> synonyms) {
        Set<String> synonymsSet = Arrays.stream(items)/*.map(sn -> normalizeSn(sn))*/.collect(Collectors.toSet());
        String synoyStr = StringUtils.join(synonymsSet, ",");
        for (String syno : synonymsSet) {
            synonyms.put(normalizeSn(syno), synoyStr);
        }
    }

    protected static void addSpecialWord(String[] items, DictSegment mainDict, Map<String, String> synonyms, DictType dictType) {
        String synoWord = Arrays.stream(items).map(item -> item.toLowerCase().trim()).collect(Collectors.joining(","));
        for (String item : items) {
            item = item.trim().toLowerCase();
            synonyms.put(item, synoWord);
            mainDict.fillSegment(item.toCharArray(), dictType);
        }
    }

}
