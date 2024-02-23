package com.talang.surfing.segment.core;


import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import com.talang.surfing.segment.util.CharacterUtil;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

@Data
public class Context {

    private Logger logger = ESPluginLoggerFactory.getLogger(Context.class.getName());
    //分词输入的的字符串
    private String input;
    //字符窜读取缓冲
    private char[] segmentBuff;
    //字符类型数组
    private int[] charTypes;
    //当前缓冲区位置指针
    private int cursor;
    //可处理字符串长度
    private int available;

    private List<String> showNames;

    private Map<String, Lexeme> sourceLexemes;

    private LinkedList<Lexeme> results = new LinkedList<>();

    private Map<DictType, Set<Lexeme>> dictTypeSetMap = Maps.newHashMap();

    private boolean lowcase;

    public Context(boolean lowcase) {
        this();
        this.lowcase = lowcase;
    }

    public Context() {
        this.segmentBuff = new char[4096];
        this.charTypes = new int[4096];
        this.results = new LinkedList<>();
        this.sourceLexemes = Maps.newHashMap();
    }

    public Set<Lexeme> getLexemeByType(DictType dictType) {
        if (dictType == DictType.SN || dictType == DictType.SN_DICT) {
            Set<Lexeme> snLexemes = dictTypeSetMap.getOrDefault(DictType.SN, new HashSet<>());
            Set<Lexeme> snDictLexemes = dictTypeSetMap.getOrDefault(DictType.SN_DICT, new HashSet<>());
            snLexemes.addAll(snDictLexemes);
            return snLexemes;
        } else {
            return dictTypeSetMap.getOrDefault(dictType, new HashSet<>());
        }

    }

    public void reset() {
        this.input = null;
        this.cursor = 0;
        this.available = 0;
        this.segmentBuff = new char[4096];
        this.charTypes = new int[4096];
        this.results.clear();
        this.sourceLexemes.clear();
        this.getDictTypeSetMap().clear();
    }

    public void addResults(Collection<Lexeme> lexemes, boolean clear) {
        if (clear) {
            results.clear();
        }
        for (Lexeme lexeme : lexemes) {
            addResult(lexeme);
        }
    }

    public boolean isLastChar() {
        return this.cursor == this.input.length() - 1;
    }

    public void addResult(Lexeme lexeme) {
        Set<DictType> dictTypes = lexeme.getDictTypes();
        if (dictTypes.isEmpty()) {
            return;
        }

        if (isLowcase()) {
            lexeme.setText(lexeme.getText());
        }
        this.results.add(lexeme);
    }


    public int fillBuffer(Reader reader) {
        int readCount = 0;
        //首次读取reader
        try {
            readCount = reader.read(segmentBuff);
        } catch (IOException e) {
            logger.error("fillBuffer error,", e);
        }
        if (readCount != -1) {
            this.setInput(String.copyValueOf(segmentBuff, 0, readCount));
            //记录最后一次从Reader中读入的可用字符长度
            this.available = readCount;
            //重置当前指针
            this.cursor = 0;
        }
        return readCount;
    }

    char getCurrentChar() {
        return this.segmentBuff[this.cursor];
    }


    public void addLexeme(Lexeme l) {
        String key = String.format(l.getText() + l.getBegin() + l.getEnd());
        if (l.getBegin() > l.getEnd()) {
            return;
        }
        if (isLowcase()) {
            l.setText(l.getText().toLowerCase());
        }
        Lexeme exist = sourceLexemes.get(key);
        if (null != exist) {
            exist.addDictType(l.getDictTypes());
        } else {
            sourceLexemes.put(key, l);
        }
    }


    public void initCursor() {
        this.cursor = 0;
        this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
        this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
    }

    /**
     * 指针+1
     * 成功返回 true； 指针已经到了buff尾部，不能前进，返回false
     * 并处理当前字符
     */
    public boolean moveCursor() {
        if (this.cursor < this.available - 1) {
            this.cursor++;
            this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
            this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
            return true;
        } else {
            return false;
        }
    }

    public Lexeme getNextLexeme() {
        return results.pollFirst();
    }

    public boolean conditionConnectChar() {
        return this.cursor > 0 && this.cursor < this.available && CharacterUtil.isDigital(this.getSegmentBuff()[this.cursor - 1]) && CharacterUtil.isDigital(this.getSegmentBuff()[this.cursor + 1]);
    }

    public void doContextGroup() {
        for(Lexeme result : getResults()) {
            for (DictType dictType : result.getDictTypes()) {
                Set<Lexeme> lexemeSet = dictTypeSetMap.get(dictType);
                if (null == lexemeSet) {
                    lexemeSet = new HashSet<>();
                    dictTypeSetMap.put(dictType, lexemeSet);
                }
                lexemeSet.add(result);
            }
        }
    }
}
