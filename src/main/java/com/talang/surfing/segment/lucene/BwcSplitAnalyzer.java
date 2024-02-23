package com.talang.surfing.segment.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public class BwcSplitAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String field) {
        Tokenizer splitTokenizer = new BwcSplitTokenizer();
        return new TokenStreamComponents(splitTokenizer);
    }
}
