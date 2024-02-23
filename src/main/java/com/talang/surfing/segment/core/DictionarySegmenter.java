package com.talang.surfing.segment.core;


import cn.hutool.core.util.ObjectUtil;
import com.talang.surfing.segment.dic.Hit;
import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import com.talang.surfing.segment.util.Constant;
import com.talang.surfing.segment.util.SegmentException;
import com.talang.surfing.segment.dic.Dictionary;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DictionarySegmenter implements ISegmenter {

    private List<Hit> tmpHits = Lists.newArrayList();

    private Logger logger = ESPluginLoggerFactory.getLogger(DictionarySegmenter.class.getName());
    @Override
    public void analyzer(Context context) throws SegmentException {
        if (!this.tmpHits.isEmpty()) {
            Hit[] tmpArray = this.tmpHits.toArray(new Hit[0]);
            for (Hit hit : tmpArray) {
                hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
                if (hit.isMatch()) {
                    Lexeme lexeme = new Lexeme(hit.getBegin(), hit.getEnd(), ObjectUtil.cloneByStream(hit.getDictTypes()), context.getInput(), getName());
                    context.addLexeme(lexeme);
                    if (!hit.isPrefix()) {
                        this.tmpHits.remove(hit);
                    }
                } else if (hit.isUnmatch()) {
                    this.tmpHits.remove(hit);
                }
            }
        }

        Hit searchHit = Dictionary.getSingleton().matchHit(Constant.MAIN_DICT, context.getSegmentBuff(), context.getCursor(), 1);
        if (searchHit.isMatch()) {
            Lexeme lexeme = new Lexeme(searchHit.getBegin(), searchHit.getEnd(), ObjectUtil.cloneByStream(searchHit.getDictTypes()), context.getInput(), getName());
            context.addLexeme(lexeme);
            if (searchHit.isPrefix()) {
                this.tmpHits.add(searchHit);
            }
        } else if (searchHit.isPrefix()) {
            this.tmpHits.add(searchHit);
        }

    }

    @Override
    public void reset() {
        tmpHits.clear();
    }

    @Override
    public String getName() {
        return "dict";
    }
}
