package com.talang.surfing.segment.ambiguity;


import com.talang.surfing.segment.core.Context;
import com.talang.surfing.segment.core.Lexeme;

/**
 * 消歧接口
 */
public interface IAmbiguity {

    public void isKeep(Lexeme beforeLexeme, Lexeme lexeme, Lexeme nextLexeme, Context context);

}
