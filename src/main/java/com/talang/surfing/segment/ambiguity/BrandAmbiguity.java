package com.talang.surfing.segment.ambiguity;


import com.talang.surfing.segment.core.Context;
import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.core.Lexeme;
import com.talang.surfing.segment.util.CharacterUtil;

/**
 *
 */
public class BrandAmbiguity extends AbstractAmbiguity implements IAmbiguity {

    private DictType dictType = DictType.BRAND;

    @Override
    public void isKeep(Lexeme beforeLexeme, Lexeme lexeme, Lexeme nextLexeme, Context context) {
        if (lexeme.getDictTypes().contains(dictType)) {
            if (calSubEngNumber(lexeme, context)) return;
            if (calSubBrand(lexeme, context)) return;
        }
        return;
    }


    /**
     * * 规则1 ： 品牌如果开头或结果是英文，则临界区不应当是英文（防止从英文中取一半的情况）
     * * 举例：子弹型948GBM，这是个型号，其中GB是一个品牌，需要过滤掉
     *
     * @param lexeme
     * @param context
     * @return
     */
    private boolean calSubEngNumber(Lexeme lexeme, Context context) {
        String input = context.getInput();
        char[] chars = input.toCharArray();

        //如果品牌是以数字、英文开头，且前一个字符也是数字或英文，则移除品牌
        int start = lexeme.getBegin();
        if (start > 0) {
            char beforeChar = chars[start - 1];
            char startChar = chars[lexeme.getBegin()];
            if (CharacterUtil.isDigitalEnglish(beforeChar) && CharacterUtil.isDigitalEnglish(startChar)) {
                removeDictType(lexeme, dictType);
                return true;
            }
        }

        int end = lexeme.getEnd();
        //如果end不是最后一个字符
        if (end < chars.length - 1) {
            char afterChar = chars[end + 1];
            char endChar = chars[end];
            if (CharacterUtil.isDigitalEnglish(endChar) && CharacterUtil.isDigitalEnglish(afterChar)) {
                removeDictType(lexeme, dictType);
                return true;
            }
        }
        return false;
    }

    private boolean calSubBrand(Lexeme lexeme, Context context) {
        for (Lexeme loopLexeme : context.getSourceLexemes().values()) {
            if (!loopLexeme.getDictTypes().contains(DictType.BRAND)) {
                continue;
            }
            if (contains(lexeme, loopLexeme)) {
                removeDictType(lexeme, dictType);
            }
        }
        return false;
    }

}
