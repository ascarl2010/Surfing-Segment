package org.elasticsearch.index.analysis;

import com.talang.surfing.segment.util.SegmentMode;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import com.talang.surfing.segment.cfg.EsConfiguration;
import com.talang.surfing.segment.lucene.BwcTokenizer;

public class SurfingTokenizerFactory extends AbstractTokenizerFactory {
    private EsConfiguration configuration;

    public SurfingTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings, SegmentMode segmentMode) {
        super(indexSettings, settings, name);
        configuration = new EsConfiguration(env, settings, segmentMode);
    }

    public static SurfingTokenizerFactory getIkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new SurfingTokenizerFactory(indexSettings, env, name, settings, SegmentMode.MAX);
    }

    public static SurfingTokenizerFactory getIkSmartTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new SurfingTokenizerFactory(indexSettings, env, name, settings, SegmentMode.MOST);
    }

    public static SurfingTokenizerFactory getBwcTokenizerDistinctFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new SurfingTokenizerFactory(indexSettings, env, name, settings, SegmentMode.DISTINCT);
    }

    @Override
    public Tokenizer create() {
        return new BwcTokenizer(configuration);
    }
}
