package org.elasticsearch.index.analysis;

import com.talang.surfing.segment.lucene.BwcSplitTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class SurfingSplitTokenizerFactory extends AbstractTokenizerFactory {


    public SurfingSplitTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
    }

    @Override
    public Tokenizer create() {
        return new BwcSplitTokenizer();
    }

    public static SurfingSplitTokenizerFactory getSplitTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        SurfingSplitTokenizerFactory surfingSplitTokenizerFactory = new SurfingSplitTokenizerFactory(indexSettings, environment, name, settings);
        return surfingSplitTokenizerFactory;
    }
}
