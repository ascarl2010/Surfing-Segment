package org.elasticsearch.plugin.analysis.wwb;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;


public class AnalysisIkPlugin extends Plugin implements AnalysisPlugin {

	public static String PLUGIN_NAME = "analysis-wwb";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();
        extra.put("surfing_most", SurfingTokenizerFactory::getIkSmartTokenizerFactory);
        extra.put("surfing_max", SurfingTokenizerFactory::getIkTokenizerFactory);
        extra.put("surfing_distinct", SurfingTokenizerFactory::getBwcTokenizerDistinctFactory);
        extra.put("surfing_split", SurfingSplitTokenizerFactory::getSplitTokenizerFactory);
        extra.put("surfing_lowercase", LowercaseTokenizerFactory::getLowercaseTokenizerFactory);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();
        extra.put("surfing_most", SurfingAnalyzerProvider::getIkSmartAnalyzerProvider);
        extra.put("surfing_max", SurfingAnalyzerProvider::getIkAnalyzerProvider);
        extra.put("surfing_distinct", SurfingAnalyzerProvider::getBwcAnalyzerDistinctProvider);
        extra.put("surfing_split", SurfingSplitAnalyzerProvider::getSplitAnalyzerProvider);
        extra.put("surfing_lowercase", LowercaseAnalyzerProvider::getSplitAnalyzerProvider);
        return extra;
    }

}
