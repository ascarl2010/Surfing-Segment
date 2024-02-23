package com.talang.surfing.segment.core;

import com.talang.surfing.segment.util.SegmentException;
import com.talang.surfing.segment.util.CharacterUtil;
import com.google.common.collect.Sets;

public class NumberSegmenter implements ISegmenter {

    private int start = -1;

    @Override
    public void analyzer(Context context) throws SegmentException {
//        System.out.println(context.getCurrentChar() + " : " + context.getCursor());
        if (start == -1) {
            if (CharacterUtil.isDigital(context.getCurrentChar())) {
                start = context.getCursor();
            }
        } else {
            if (!CharacterUtil.isDigital(context.getCurrentChar())) {
                Lexeme lexeme = new Lexeme(this.start, context.getCursor() -1, Sets.newHashSet(DictType.SN), context.getInput(), getName());
                context.addLexeme(lexeme);
                start = -1;
            }
        }

        if(context.isLastChar()) {
            if(start != -1) {
                Lexeme lexeme = new Lexeme(this.start, context.getCursor(), Sets.newHashSet(DictType.SN), context.getInput(), getName());
                context.addLexeme(lexeme);
                start = -1;
            }
        }
    }

    @Override
    public void reset() {
        this.start = -1;
    }

    @Override
    public String getName() {
        return DictType.SN.getName();
    }
}
