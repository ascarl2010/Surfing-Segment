package org.elasticsearch.index.analysis;

import com.talang.surfing.segment.util.SegmentMode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import com.talang.surfing.segment.cfg.EsConfiguration;
import com.talang.surfing.segment.lucene.BwcAnalyzer;

public class SurfingAnalyzerProvider extends AbstractIndexAnalyzerProvider<BwcAnalyzer> {
    private final BwcAnalyzer analyzer;

    public SurfingAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings, SegmentMode segmentMode) {
        super(indexSettings, name, settings);

        EsConfiguration configuration=new EsConfiguration(env,settings, segmentMode);

        analyzer=new BwcAnalyzer(configuration);
    }

    public static SurfingAnalyzerProvider getIkSmartAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new SurfingAnalyzerProvider(indexSettings,env,name,settings, SegmentMode.MOST);
    }

    public static SurfingAnalyzerProvider getIkAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new SurfingAnalyzerProvider(indexSettings,env,name,settings,SegmentMode.MAX);
    }

    public static SurfingAnalyzerProvider getBwcAnalyzerDistinctProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new SurfingAnalyzerProvider(indexSettings,env,name,settings,SegmentMode.DISTINCT);
    }

    @Override public BwcAnalyzer get() {
        return this.analyzer;
    }
}
