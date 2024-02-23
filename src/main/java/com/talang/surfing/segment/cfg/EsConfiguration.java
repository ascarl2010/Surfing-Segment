/**
 *
 */
package com.talang.surfing.segment.cfg;

import com.talang.surfing.segment.dic.DictionaryMongodb;
import com.talang.surfing.segment.help.ESPluginLoggerFactory;
import com.talang.surfing.segment.dic.DictionaryFileSystem;
import com.talang.surfing.segment.dic.SegmentConfig;
import com.talang.surfing.segment.util.SegmentMode;
import lombok.Data;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.analysis.wwb.AnalysisIkPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

@Data
public class EsConfiguration {

    private Environment environment;
    private Settings settings;
    private SegmentMode segmentMode;

    private static boolean loaded = false;

    private static SegmentConfig segmentConfig;


    private Logger logger = ESPluginLoggerFactory.getLogger("Configuration", EsConfiguration.class);

    /** Environment是整个es的环境，Settings是当前索引的setting，
     一些个性设置可以设置在settings里
     可以从settings.get从设置中取配置信息
     **/
    @Inject
    public EsConfiguration(Environment env, Settings settings, SegmentMode segmentMode) {
        this.environment = env;
        this.settings = settings;
        this.segmentMode = segmentMode;
        Path confPath = environment.configFile().resolve(AnalysisIkPlugin.PLUGIN_NAME);
        if (EsConfiguration.segmentConfig == null) {
            EsConfiguration.segmentConfig = getSegmentconfig(env);
        }
        synchronized (EsConfiguration.class) {
            if(!loaded) {
                if (EsConfiguration.segmentConfig != null) {
                    if (EsConfiguration.segmentConfig.getMethod().equals("mongo")) {
                        logger.info("load dict from mongodb");
                        DictionaryMongodb.initial(EsConfiguration.segmentConfig);
                    } else if (EsConfiguration.segmentConfig.getMethod().equals("file")) {
                        logger.info("load dict from fileSystem");
                        DictionaryFileSystem.initial(EsConfiguration.segmentConfig);
                    }
                }
                loaded = true;
            }
        }
    }

    //获取配置文件
    private SegmentConfig getSegmentconfig(Environment env) {
        String fileName = "surfing-analyzer.xml";
        Path configDir = env.configFile().resolve(AnalysisIkPlugin.PLUGIN_NAME);
        Path configFile = configDir.resolve(fileName);
        InputStream input = null;
        try {
            logger.info("try load config from {}", configFile);
            input = new FileInputStream(configFile.toFile());
        } catch (FileNotFoundException e) {
            configDir = PathUtils
                    .get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                            .getParent(), "config").toAbsolutePath();
            configFile = configDir.resolve(fileName);
            try {
                logger.info("try load config from {}", configFile);
                input = new FileInputStream(configFile.toFile());
            } catch (FileNotFoundException ex) {
                logger.error("wwb-analyzer", e);
            }
        }

        if (input != null) {
            Properties props = new Properties();
            try {
                props.loadFromXML(input);
                logger.info("username:{}, password:{}, database:{}, host:{}, collection:{}, port:{}, reloadPeriodSeconds:{}",
                        props.getProperty("user"), props.getProperty("password"), props.getProperty("database"),
                        props.getProperty("host"), props.get("collection"), props.getProperty("port"), props.getProperty("reload_period_seconds"));
                SegmentConfig segmentConfig =
                        SegmentConfig.builder().user(props.getProperty("user"))
                                .password(props.getProperty("password"))
                                .database(props.getProperty("database"))
                                .path(props.getProperty("path"))
                                .host(props.getProperty("host"))
                                .method(props.getProperty("method"))
                                .collection(props.getProperty("collection"))
                                .port(Integer.valueOf(props.getProperty("port")))
                                .reloadPeriodSeconds(Integer.valueOf(props.getProperty("reload_period_seconds")))
                                .build();

                return segmentConfig;
            } catch (Exception e) {
                logger.error("wwb-analyzer", e);
            }
        }
        return null;
    }

    public EsConfiguration() {
    }

    public Path getConfigInPluginDir() {
        return PathUtils
                .get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                        .getParent(), "config")
                .toAbsolutePath();
    }

}
