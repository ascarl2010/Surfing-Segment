package com.talang.surfing.segment.ambiguity;

import com.talang.surfing.segment.core.Context;
import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.core.Lexeme;

/**
 * @author: wangwanbao
 * @create: 2021-05-31 18:23
 * 主词典去重
 **/
public class MainAmbiguity extends  AbstractAmbiguity implements IAmbiguity {
    @Override
    public void isKeep(Lexeme beforeLexeme, Lexeme lexeme, Lexeme nextLexeme, Context context) {
        for (Lexeme loopLexeme : context.getSourceLexemes().values()) {
            if(contains(lexeme, loopLexeme)) {
                removeDictType(lexeme, DictType.MAIN);
            }
        }
    }
}
