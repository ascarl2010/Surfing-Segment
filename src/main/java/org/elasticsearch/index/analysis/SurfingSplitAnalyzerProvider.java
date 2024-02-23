package org.elasticsearch.index.analysis;

import com.talang.surfing.segment.lucene.BwcSplitAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class SurfingSplitAnalyzerProvider extends AbstractIndexAnalyzerProvider<BwcSplitAnalyzer> {

    private BwcSplitAnalyzer analyzer;

    public SurfingSplitAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.analyzer = new BwcSplitAnalyzer();
    }


    @Override
    public BwcSplitAnalyzer get() {
        return this.analyzer;
    }

    public static AnalyzerProvider<? extends Analyzer> getSplitAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        return new SurfingSplitAnalyzerProvider(indexSettings, environment, name, settings);
    }
}
