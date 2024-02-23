package com.talang.surfing.segment.ambiguity;


import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.core.Lexeme;

public abstract class AbstractAmbiguity {

    public void removeDictType(Lexeme lexeme, DictType dictType) {
        lexeme.getDictTypes().remove(dictType);
    }

    protected boolean contains(Lexeme lexeme, Lexeme loopLexeme) {
        //当前lexeme
        if (loopLexeme.getBegin() == lexeme.getBegin() && loopLexeme.getEnd() == lexeme.getEnd()) {
            return false;
        }
        if (lexeme.getBegin() >= loopLexeme.getBegin() && lexeme.getBegin() <= loopLexeme.getEnd() && lexeme.getEnd() >= loopLexeme.getBegin() && lexeme.getEnd() <= loopLexeme.getEnd()) {
            return true;
        }
        return false;
    }
}
