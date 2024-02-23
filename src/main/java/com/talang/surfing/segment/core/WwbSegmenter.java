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
package com.talang.surfing.segment.core;

import com.talang.surfing.segment.util.SegmentException;
import com.talang.surfing.segment.ambiguity.BrandAmbiguity;
import com.talang.surfing.segment.ambiguity.IAmbiguity;
import com.talang.surfing.segment.ambiguity.SnAmbiguity;
import com.talang.surfing.segment.util.CharacterUtil;
import com.talang.surfing.segment.util.SegmentMode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * IK分词器主类
 */
public final class WwbSegmenter {

    //字符窜reader
    private Reader input;
    //分词器上下文
    private Context context;

    private SegmentMode segmentMode;

    /**
     * IK分词器构造函数
     *
     * @param input
     */
    public WwbSegmenter(Reader input, SegmentMode segmentMode) {
        this.input = input;
        this.segmentMode = segmentMode;
        this.init(false);
    }

    /**
     * IK分词器构造函数
     *
     * @param input
     */
    public WwbSegmenter(Reader input, SegmentMode segmentMode, boolean lowcase) {
        this.input = input;
        this.segmentMode = segmentMode;
        this.init(lowcase);
    }

    /**
     * 初始化
     */
    private void init(boolean lowcase) {
        //初始化分词上下文
        this.context = new Context(lowcase);
    }


    /**
     * 分词，获取下一个词元
     *
     * @return Lexeme 词元对象
     * @throws java.io.IOException
     */
    public synchronized Lexeme next() throws IOException {
        Lexeme l = null;
        while ((l = context.getNextLexeme()) == null) {
            int available = context.fillBuffer(this.input);
            if (available <= 0) {
                //reader已经读完
                context.reset();
                return null;
            } else {
                doSegment();
            }
        }
        return l;
    }

    public List<Lexeme> getResult() {
        context.fillBuffer(this.input);
        doSegment();
        return context.getResults();
    }

    public Context getContext() {
        context.fillBuffer(this.input);
        doSegment();
        return context;
    }

    //消歧
    private void doIambiguity() {
        Collection<Lexeme> segmenteValues = context.getSourceLexemes().values();
        ArrayList<Lexeme> sortLexemes = new ArrayList(segmenteValues);
        Collections.sort(sortLexemes);

        for (int i = 0; i < sortLexemes.size(); i++) {
            Lexeme beforeLexeme = i == 0 ? null : sortLexemes.get(i - 1);
            Lexeme curLexeme = sortLexemes.get(i);
            Lexeme nextLexeme = i == sortLexemes.size() - 1 ? null : sortLexemes.get(i + 1);
            for (IAmbiguity iAmbiguity : loadAmbiguities()) {
                iAmbiguity.isKeep(beforeLexeme, curLexeme, nextLexeme, context);
            }
            if (curLexeme.getDictTypes().size() > 0) {
                context.addResult(curLexeme);
            }
        }
    }

    private void doSegment() {
        context.initCursor();
        List<ISegmenter> segmenters = loadSegments();
        do {
            for (ISegmenter segmenter : segmenters) {
                try {
                    segmenter.analyzer(context);
                } catch (SegmentException e) {
                    e.printStackTrace();
                }
            }
        } while (context.moveCursor());
//        context.addResults(context.getSourceLexemes().values(), true);
        doIambiguity();

        //最长分词模式, distinct默认使用最多分词模式
        if (segmentMode == SegmentMode.MAX) {
            doMaxSegment(context);
        }


        doUnMatch(context);

        //去除重复
        if (segmentMode == SegmentMode.DISTINCT) {
            Iterator<Lexeme> iterator = context.getResults().iterator();
            Set<String> addTexts = Sets.newHashSet();
            while (iterator.hasNext()) {
                Lexeme lexeme = iterator.next();
                String text = lexeme.getText();
                if (addTexts.contains(text)) {
                    iterator.remove();
                }
                addTexts.add(text);
            }
        }

        context.doContextGroup();

        Collections.sort(context.getResults());
    }


    //处理未匹配，未匹配只处理中文的情况
    private void doUnMatch(Context context) {
        char[] chars = context.getSegmentBuff();
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            char next = i > context.getAvailable() - 1 ? ' ' : chars[i + 1];
            boolean currentIsChinese = CharacterUtil.isChinese(current);
            if (!currentIsChinese) {
                continue;
            }
            boolean nextIsChinese = CharacterUtil.isChinese(next);
            boolean currentWithIn = false;
            boolean nextWithIn = false;
            for (Lexeme lexeme : context.getResults()) {
                if (currentWithIn == true && nextWithIn == true) {
                    break;
                }
                if (currentWithIn == false) {
                    if (i >= lexeme.getBegin() && i <= lexeme.getEnd()) {
                        currentWithIn = true;
                    }
                }
                if (nextIsChinese == true) {
                    if (nextWithIn == false) {
                        if (i + 1 >= lexeme.getBegin() && i + 1 <= lexeme.getEnd()) {
                            nextWithIn = true;
                        }
                    }
                }
            }
            //当前是中文且未被匹配上
            if (currentWithIn == false) {
                if (nextWithIn == false && nextIsChinese == true) {
                    Lexeme lexeme = new Lexeme(i, i + 1, Sets.newHashSet(DictType.UNKNOWN), context.getInput(), DictType.UNKNOWN.getName());
                    context.addResult(lexeme);
                } else {
                    Lexeme lexeme = new Lexeme(i, i, Sets.newHashSet(DictType.UNKNOWN), context.getInput(), DictType.UNKNOWN.getName());
                    context.addResult(lexeme);
                }
            }
        }
    }


    private void doMaxSegment(Context context) {
        List<Lexeme> toAddLexemes = Lists.newArrayList();
        for (int i = 0; i < context.getResults().size(); i++) {
            Lexeme current = context.getResults().get(i);
            Lexeme before = i == 0 ? null : context.getResults().get(i - 1);
            Lexeme next = i == context.getResults().size() - 1 ? null : context.getResults().get(i + 1);
            if (current.getDictTypes().contains(DictType.BRAND) || current.getDictTypes().contains(DictType.GOODS) || current.getDictTypes().contains(DictType.CAT)) {
                toAddLexemes.add(current);
                continue;
            }
            if (before != null) {
                if (before.getBegin() <= current.getBegin() && before.getEnd() >= current.getEnd()) {
                    continue;
                }
            }

            if (next != null) {
                if (next.getBegin() == current.getBegin() && next.getEnd() >= current.getEnd()) {
                    continue;
                }
            }
            toAddLexemes.add(current);
        }
        context.addResults(toAddLexemes, true);
    }

    private boolean contains(int begin, int end) {
        boolean contains = false;
        for (Lexeme lexeme : context.getResults()) {
            if (lexeme.getDictTypes().contains(DictType.UNKNOWN)) {
                continue;
            }
            if (begin >= lexeme.getBegin() && begin <= lexeme.getEnd()) {
                contains = true;
                break;
            }

            if (end >= lexeme.getEnd() && end <= lexeme.getEnd()) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private List<IAmbiguity> loadAmbiguities() {
        return Lists.newArrayList(new BrandAmbiguity(), new SnAmbiguity() /*, new MainAmbiguity()  new QuantiferAmbiguity()*/);
    }

    private List<ISegmenter> loadSegments() {
        return Lists.newArrayList(new SnSegmenter(), new DictionarySegmenter() /*,new QuantifierSegmenter(), new NumberSegmenter()*/);
    }

    /**
     * 重置分词器到初始状态
     *
     * @param input
     */
    public synchronized void reset(Reader input) {
        this.input = input;
        context.reset();
    }

    public static void main(String[] args) {
        System.out.println(CharacterUtil.isChinese('<'));
    }
}
