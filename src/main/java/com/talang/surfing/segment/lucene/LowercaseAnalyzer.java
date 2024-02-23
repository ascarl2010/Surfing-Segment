package com.talang.surfing.segment.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public class LowercaseAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String s) {
        Tokenizer locaseTokenizer = new LowercaseTokenizer();
        return new TokenStreamComponents(locaseTokenizer);
    }
}
