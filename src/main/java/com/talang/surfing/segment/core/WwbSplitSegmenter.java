package com.talang.surfing.segment.core;

import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.util.LinkedList;

public class WwbSplitSegmenter {

    private Reader input;
    private Context context;

    private Logger logger = ESPluginLoggerFactory.getLogger(WwbSplitSegmenter.class.getName());

    public WwbSplitSegmenter(Reader input) {
        this.input = input;
        this.context = new Context(true);
    }

    public synchronized Lexeme next() {
        Lexeme l = null;
        while ((l = context.getNextLexeme()) == null) {
            int avaliable = context.fillBuffer(this.input);
            if (avaliable <= 0) {
                context.reset();
                return null;
            } else {
                doSegment();
            }
        }
        return l;
    }

    private void doSegment() {
        String text = context.getInput();
        String[] items = text.split(",");
        LinkedList<Lexeme> lexemeList = new LinkedList<Lexeme>();
        int lastIndex = 0;
        for (String item : items) {
            if(StringUtils.isEmpty(item)) {
                continue;
            }
            int curIndex = StringUtils.indexOfIgnoreCase(text, item, lastIndex);
            Lexeme lexeme = new Lexeme(curIndex, curIndex + item.length() - 1, Sets.newHashSet(DictType.SPLIT), text, DictType.SPLIT.getName());
            lexemeList.add(lexeme);
            lastIndex = curIndex;
        }
        context.addResults(lexemeList, true);
    }

    public void reset(Reader input) {
        this.input = input;
        context.reset();
    }
}
