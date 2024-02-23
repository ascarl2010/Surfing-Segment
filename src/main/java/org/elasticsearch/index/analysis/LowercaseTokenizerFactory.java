package org.elasticsearch.index.analysis;

import com.talang.surfing.segment.lucene.BwcSplitTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class LowercaseTokenizerFactory extends AbstractTokenizerFactory {


    public LowercaseTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
    }

    @Override
    public Tokenizer create() {
        return new BwcSplitTokenizer();
    }

    public static LowercaseTokenizerFactory getLowercaseTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        LowercaseTokenizerFactory bwcLowerCaseFactory = new LowercaseTokenizerFactory(indexSettings, environment, name, settings);
        return bwcLowerCaseFactory;
    }
}
