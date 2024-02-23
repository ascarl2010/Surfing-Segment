package com.talang.surfing.segment.dic;

import com.talang.surfing.segment.core.DictType;
import com.talang.surfing.segment.mongo.MongoUtil;
import com.google.common.collect.Maps;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.elasticsearch.SpecialPermission;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: wangwanbao
 * @create: 2021-03-09 16:00
 **/
@Log4j2
public class DictionaryMongodb extends Dictionary {

    private static final int CHECK_WORD_LENGTH = 200000;

    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    protected DictionaryMongodb(SegmentConfig segmentConfig) {
        super(segmentConfig);
    }

    public static synchronized void initial(SegmentConfig segmentConfig) {
        if (singleton == null) {
            synchronized (Dictionary.class) {
                if (singleton == null) {
                    singleton = new DictionaryMongodb(segmentConfig);
                    try {
                        //加载主词典
                        loadData(segmentConfig);
                        //加载同义词典
                        loadSynoData(segmentConfig);
                        pool.scheduleAtFixedRate(new ReloadDict(segmentConfig), 600, segmentConfig.getReloadPeriodSeconds(), TimeUnit.SECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //加载同义词典
    public static void loadSynoData(SegmentConfig segmentConfig) {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            int pageSize = 1000;
            MongoClient client = null;
            try {
                client = MongoUtil.getClient(segmentConfig);
                MongoCollection<Document> collection = MongoUtil.getCollection(client, segmentConfig.getDatabase(), "kl_dict_syno");
                long total = MongoUtil.getTotal(collection);
                long pageTotal = (total + pageSize - 1) / pageSize;
                //量词词典
                for (int i = 1; i <= pageTotal + 1; i++) {
                    MongoCursor<Document> documents = MongoUtil.loadDoument(collection, i, pageSize);
                    while (documents.hasNext()) {
                        Document dict = documents.next();
                        String value = dict.getString("value").trim().toLowerCase();
                        int code = dict.getInteger("code");
                        DictType dictType = DictType.getDictTypeByCode(code);
                        if (dictType == DictType.SN) {
                            addSnSynos(value.split(","), getSingleton().synonyms);
                        } else {
                            addSpecialWord(value.split(","), getSingleton().mainDict, getSingleton().synonyms, dictType);
                        }
                    }
                }
                log.info("finish load syno dict");
                return null;
            } catch (Exception e) {
                logger.error("load dict from mongo occur exception : {} ", e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
                if (null != client) {
                    client.close();
                }
            }
        });
    }

    public static void loadData(SegmentConfig segmentConfig) throws Exception {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            int pageSize = 1000;
            MongoClient client = null;
            try {
                client = MongoUtil.getClient(segmentConfig);
                MongoCollection<Document> collection = MongoUtil.getCollection(client, segmentConfig.getDatabase(), segmentConfig.getCollection());
                long total = MongoUtil.getTotal(collection);
                long pageTotal = (total + pageSize - 1) / pageSize;
                int found = 0;
                //主词典
                DictSegment mainDict = new DictSegment((char) 0);
                //量词词典
                DictSegment quantifierDict = new DictSegment((char) 0);
                Map<String, String> synonyms = Maps.newHashMap();
                for (int i = 1; i <= pageTotal + 1; i++) {
                    MongoCursor<Document> documents = MongoUtil.loadDoument(collection, i, pageSize);
                    while (documents.hasNext()) {
                        Document dict = documents.next();
                        String value = dict.getString("value").trim().toLowerCase();
                        int code = dict.getInteger("code");
                        DictType dictType = DictType.getDictTypeByCode(code);
                        if (dictType.getCode() == DictType.QUANTIFIER.getCode()) {
                            quantifierDict.fillSegment(value.toLowerCase().trim().toCharArray(), dictType);
                        } else {
                            if (value.contains("/")) {
                                addSpecialWord(value.split("/"), mainDict, synonyms, dictType);
                            } else if (value.contains(",")) {
                                addSpecialWord(value.split(","), mainDict, synonyms, dictType);
                            } else {
                                mainDict.fillSegment(value.toCharArray(), dictType);
                            }
                        }
                        found = found + 1;
                    }
                }
                if (found > CHECK_WORD_LENGTH) {
                    logger.info("load dict from mongo {} ", found);
                    getSingleton().setMainDict(mainDict);
                    getSingleton().setQuantifierDict(quantifierDict);
                    getSingleton().synonyms = synonyms;
                }
                return null;
            } catch (Exception e) {
                logger.error("load dict from mongo occur exception : {} ", e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
                if (null != client) {
                    client.close();
                }
            }
        });
    }


}
