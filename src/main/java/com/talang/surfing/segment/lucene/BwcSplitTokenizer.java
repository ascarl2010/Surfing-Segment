package com.talang.surfing.segment.lucene;

import com.talang.surfing.segment.core.Lexeme;
import com.talang.surfing.segment.core.WwbSplitSegmenter;
import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

public class BwcSplitTokenizer extends Tokenizer {

    private WwbSplitSegmenter splitSegmenter;
    //词元文本属性
    private final CharTermAttribute termAtt;
    //词元位移属性
    private final OffsetAttribute offsetAtt;
    //词元分类属性（该属性分类参考org.wltea.analyzer.core.Lexeme中的分类常量）
    private final TypeAttribute typeAtt;
    //记录最后一个词元的结束位置
    private int endPosition;

    private int skippedPositions;

    private PositionIncrementAttribute posIncrAtt;

    private Logger logger = ESPluginLoggerFactory.getLogger("segment", BwcTokenizer.class);


    public BwcSplitTokenizer() {
        super();
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
        posIncrAtt = addAttribute(PositionIncrementAttribute.class);
        splitSegmenter = new WwbSplitSegmenter(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        //清除所有的词元属性
        clearAttributes();
        skippedPositions = 0;

        Lexeme nextLexeme = splitSegmenter.next();
        if (nextLexeme != null) {
            posIncrAtt.setPositionIncrement(skippedPositions + 1);

            //将Lexeme转成Attributes
            //设置词元文本
            termAtt.append(nextLexeme.getText());
            //设置词元长度
            termAtt.setLength(nextLexeme.getEnd() - nextLexeme.getBegin() + 1);
            //设置词元位移
            offsetAtt.setOffset(correctOffset(nextLexeme.getBegin()), correctOffset(nextLexeme.getEnd()));

            //记录分词的最后位置
            endPosition = nextLexeme.getEnd();
            //记录词元分类
            typeAtt.setType(nextLexeme.toDictTypes());

            logger.error("text:" + nextLexeme.getText() + " length:" + (nextLexeme.getEnd() - nextLexeme.getBegin() + 1) + " begin: " + nextLexeme.getBegin() + " end:" + nextLexeme.getEnd() + " type " + nextLexeme.toDictTypes());
            //返会true告知还有下个词元
            return true;
        }
        //返会false告知词元输出完毕
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.lucene.analysis.Tokenizer#reset(java.io.Reader)
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        splitSegmenter.reset(input);
        skippedPositions = 0;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        int finalOffset = correctOffset(this.endPosition);
        offsetAtt.setOffset(finalOffset, finalOffset);
        posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
    }
}
