package org.elasticsearch.index.analysis;

import com.talang.surfing.segment.lucene.LowercaseAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class LowercaseAnalyzerProvider extends AbstractIndexAnalyzerProvider<LowercaseAnalyzer> {

    private LowercaseAnalyzer analyzer;

    public LowercaseAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.analyzer = new LowercaseAnalyzer();
    }


    @Override
    public LowercaseAnalyzer get() {
        return this.analyzer;
    }

    public static AnalyzerProvider<? extends Analyzer> getSplitAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        return new LowercaseAnalyzerProvider(indexSettings, environment, name, settings);
    }
}
