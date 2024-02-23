package com.talang.surfing.segment.lucene;

import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;

public class LowercaseTokenizer extends Tokenizer {
    /**
     * Default read buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 256;

    private Logger logger = ESPluginLoggerFactory.getLogger("segment", LowercaseTokenizer.class);
    private boolean done = false;
    private int finalOffset;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    public LowercaseTokenizer() {
    }

    @Override
    public final boolean incrementToken() throws IOException {
        String text = "";
        if (!done) {
            clearAttributes();
            done = true;
            int valChar;
            StringBuilder stringBuilder = new StringBuilder();
            while ((valChar = input.read()) != -1) {
                stringBuilder.append((char) valChar);
            }
            text = stringBuilder.toString();
            termAtt.append(text.toLowerCase());
            logger.error(text.toLowerCase());
            termAtt.setLength(text.length());
            offsetAtt.setOffset(0, text.length());
            return true;
        }
        return false;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.done = false;
    }
}
