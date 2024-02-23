package com.talang.surfing.segment.core;

import cn.hutool.core.clone.CloneSupport;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.talang.surfing.segment.dic.Dictionary;
import com.talang.surfing.segment.util.CharacterUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Lexeme extends CloneSupport<Lexeme> implements Comparable<Lexeme>, Serializable, Cloneable {

    //起始位置
    private int begin;
    //长度
    private int end;
    //词元文本
    private String text;
    //所属词库编码
    private Set<DictType> dictTypes = Sets.newHashSet();
    //原始分词字符
    private String source;

    //sn分词的段数
    private int segmentLength;

    private boolean isPartSn;

    public Lexeme copyLexeme() {
        Lexeme lexeme = new Lexeme(getBegin(), getEnd(), getText());
        lexeme.setDictTypes(getDictTypes());
        lexeme.setSource(getSource());
        return lexeme;
    }

    public Lexeme(int begin, int end, String text) {
        this.begin = begin;
        this.end = end;
        this.text = text;
    }

    public Lexeme(int begin, int end, DictType dictType, String source, int groupId) {
        this(begin, end, Sets.newHashSet(dictType), source);
    }

    public Lexeme(int begin, int end, String text, int groupId, int segmentLength, DictType dictType, boolean isExtend) {
        this.begin = begin;
        this.end = end;
        this.text = text;
        this.segmentLength = segmentLength;
        this.dictTypes = Sets.newHashSet(dictType);
    }
    
    public Lexeme(int begin, int end, DictType dictType, String source) {
        this(begin, end, Sets.newHashSet(dictType), source);
    }

    public Lexeme(int begin, int end, Set<DictType> dictTypes, String source) {
        this.begin = begin;
        this.end = end;
        this.dictTypes = dictTypes;
        this.source = source;
        String lexemeText = StringUtils.substring(source, begin, end + 1);
        this.text = ignoreConnectStr(lexemeText);
        if (dictTypes.contains(DictType.SN)) {
            calSegmentLength(text);
        }
    }

    public Lexeme(int begin, int end, Set<DictType> dictTypes, String input, String dictName) {
        this(begin, end, dictTypes, input);
    }

    private void calSegmentLength(String text) {
        int lastCharType = CharacterUtil.OTHERS;
        int curSegmentLength = 0;
        for (char ch : text.toCharArray()) {
            int charType = CharacterUtil.identifyCharType(ch);
            if (lastCharType == CharacterUtil.CHAR_SPACE) {
                if (charType != CharacterUtil.CHAR_SPACE) {
                    curSegmentLength++;
                }
            } else {
                if (charType != CharacterUtil.CHAR_SPACE && charType != lastCharType) {
                    curSegmentLength++;
                }
            }
            lastCharType = charType;
        }
        setSegmentLength(curSegmentLength);
    }

    private String ignoreConnectStr(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            if (CharacterUtil.isConnectCharacter(ch)) {
                sb.append(' ');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public String toDictTypes() {
        return this.dictTypes.stream().map(t -> t.getName()).collect(Collectors.toSet()).toString();
    }

    @Override
    public String toString() {
        String standard = Dictionary.getSingleton().getSynoValue(getText());
        String toString = getText() + "  " + toDictTypes() + " " + getBegin() + "-" + getEnd();
        if(!getText().equals(standard)) {
            toString = toString + " 标准词：" + standard;
        }
        return toString;
    }


    public void addDictType(Set<DictType> dictTypes) {
        this.dictTypes.addAll(dictTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Lexeme)) {
            return false;
        }
        Lexeme lexeme = (Lexeme) o;
        return begin == lexeme.begin &&
                end == lexeme.end &&
                segmentLength == lexeme.segmentLength &&
                isPartSn == lexeme.isPartSn &&
                Objects.equal(text, lexeme.text) &&
                Objects.equal(dictTypes, lexeme.dictTypes) &&
                Objects.equal(source, lexeme.source);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(begin, end, text, dictTypes, source, segmentLength, isPartSn);
    }

    @Override
    public int compareTo(Lexeme o) {
        if (this.begin == o.begin) {
            if (this.end < o.end) {
                return -1;
            } else if (this.end == o.end) {
                return 0;
            } else {
                return 1;
            }
        } else if (this.begin < o.begin) {
            return -1;
        } else {
            return 1;
        }
    }
}
