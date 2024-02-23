package com.talang.surfing.segment.ambiguity;

import com.talang.surfing.segment.core.Context;
import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.core.Lexeme;
import com.talang.surfing.segment.util.CharacterUtil;
import com.google.common.collect.Sets;

import java.util.Set;


// 3m 1436的情况，3和m需要被品牌词3m去重。
public class SnAmbiguity extends AbstractAmbiguity implements IAmbiguity {
    //算法识别的sn
    private DictType dictType = DictType.SN;
    //词典sn
    private DictType dictSnType = DictType.SN_DICT;

    @Override
    public void isKeep(Lexeme beforeLexeme, Lexeme lexeme, Lexeme nextLexeme, Context context) {
        if (!lexeme.getDictTypes().contains(DictType.SN)) {
            return;
        }

        if (nextLexeme != null && nextLexeme.getDictTypes().contains(DictType.BRAND)) {
            //被下一个sn完全包含
            if (lexeme.getBegin() == nextLexeme.getBegin() && lexeme.getEnd() <= nextLexeme.getEnd()) {
                removeDictType(lexeme);
            }
        }

        if (beforeLexeme != null && beforeLexeme.getDictTypes().contains(DictType.BRAND)) {
            //被下一个sn完全包含
            if (beforeLexeme.getBegin() <= lexeme.getBegin() && beforeLexeme.getEnd() >= lexeme.getEnd()) {
                removeDictType(lexeme);
            }
        }

    }


    private boolean calSubEngNumber(Lexeme lexeme, Context context) {
        String input = context.getInput();
        char[] chars = input.toCharArray();

        //如果品牌是以数字、英文开头，且前一个字符也是数字或英文，则移除型号
        int start = lexeme.getBegin();
        if (start > 0) {
            char beforeChar = chars[start - 1];
            char startChar = chars[lexeme.getBegin()];
            if (CharacterUtil.isDigitalEnglish(beforeChar) && CharacterUtil.isDigitalEnglish(startChar)) {
                removeDictType(lexeme);
                return true;
            }
        }

        int end = lexeme.getEnd();
        //如果end不是最后一个字符
        if (end < chars.length - 1) {
            char afterChar = chars[end + 1];
            char endChar = chars[end];

            //数字结束，下一个字符是字母的，保留
            if (CharacterUtil.isDigital(endChar) && CharacterUtil.isEnglish(afterChar)) {
                return false;
            }

            if (CharacterUtil.isDigitalEnglish(endChar) && CharacterUtil.isDigitalEnglish(afterChar)) {
                removeDictType(lexeme);
                return true;
            }
        }
        return false;
    }

    private boolean calSubSn(Lexeme lexeme, Context context) {
        Set<Lexeme> lexemes = Sets.newHashSet();
        if (context.getDictTypeSetMap().containsKey(dictType)) {
            lexemes.addAll(context.getDictTypeSetMap().get(dictType));
        }

        if (context.getDictTypeSetMap().containsKey(dictSnType)) {
            lexemes.addAll(context.getDictTypeSetMap().get(dictSnType));
        }

        if (null != lexemes) {
            for (Lexeme exist : lexemes) {
                if (exist.getBegin() <= lexeme.getBegin() && exist.getEnd() >= lexeme.getEnd()) {
                    removeDictType(lexeme);
                    return true;
                }
            }
        }
        return false;
    }

    public void removeDictType(Lexeme lexeme) {
        if (lexeme.getDictTypes().contains(dictType)) {
            removeDictType(lexeme, dictType);
        }

        if (lexeme.getDictTypes().contains(dictSnType)) {
            removeDictType(lexeme, dictSnType);
        }
    }
}
