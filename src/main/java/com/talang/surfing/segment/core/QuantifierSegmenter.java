package com.talang.surfing.segment.core;

import com.talang.surfing.segment.dic.Dictionary;
import com.talang.surfing.segment.dic.Hit;
import com.talang.surfing.segment.util.CharacterUtil;
import com.talang.surfing.segment.util.Constant;
import com.talang.surfing.segment.util.SegmentException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;

/**
 * 数词-数量词分词器
 */
public class QuantifierSegmenter implements ISegmenter {

    private List<Hit> tmpHits = Lists.newArrayList();

    //数字起始
    private int index = -1;

    //数字结束
    private int end = -1;

    @Override
    public void analyzer(Context context) throws SegmentException {
        boolean isDigital = CharacterUtil.isDigital(context.getCurrentChar());
        if (index == -1) {
            if (isDigital) {
                this.index = context.getCursor();
                this.end = context.getCursor();
            }
        } else { //前面有数字已匹配
            if (isDigital) { //如果当前字符是数字
                this.end = context.getCursor();
                return;
            } else {//如果当前字符不是数字，则需要判断是否是量词
                List<Hit> toRemoveHist = Lists.newArrayList();
                if (!this.tmpHits.isEmpty()) {
                    for (Hit hit : this.tmpHits) {
                        hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
                        if (hit.isMatch()) {
                            Lexeme lexeme = new Lexeme(this.index, hit.getEnd(), Sets.newHashSet(DictType.QUANTIFIER), context.getInput(), getName());
                            context.addLexeme(lexeme);
                        }
                        if (hit.isUnmatch()) {
                            toRemoveHist.add(hit);
                        }
                    }
                }

                this.tmpHits.removeAll(toRemoveHist);

                Hit searchHit = Dictionary.getSingleton().matchHit(Constant.QUANTIFIER, context.getSegmentBuff(), context.getCursor(), 1);
                //如果匹配不上，则将已经匹配上的数字增加分词，本次分词结束
                if (searchHit.isUnmatch()) {
                    Lexeme lexeme = new Lexeme(this.index, this.end, Sets.newHashSet(DictType.DIGITAL), context.getInput(), DictType.DIGITAL.name());
                    context.addLexeme(lexeme);
                    reset();
                    return;
                }
                //如果已经匹配成功，把数量词增加上下文
                if (searchHit.isMatch()) {
                    Lexeme lexeme = new Lexeme(this.index, searchHit.getEnd(), Sets.newHashSet(DictType.QUANTIFIER), context.getInput(), getName());
                    context.addLexeme(lexeme);
                }


                //如果是前缀增加前缀，加到临时前缀池中，先遍历已有的前缀
                if (searchHit.isPrefix()) {
                    tmpHits.add(searchHit);
                }
            }

        }

    }

    @Override
    public void reset() {
        this.tmpHits.clear();
        this.index = -1;
        this.end = -1;
    }

    @Override
    public String getName() {
        return "quantifier";
    }
}
