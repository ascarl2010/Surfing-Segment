package com.talang.surfing.segment.mongo;

import com.talang.surfing.segment.dic.SegmentConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.connection.SocketSettings;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author: wangwanbao
 * @create: 2021-03-09 14:32
 **/
public class MongoUtil {

    //获得mongodb的collection
    public static MongoClient getClient(SegmentConfig config) {
        MongoCredential credential = MongoCredential.createCredential(config.getUser(), config.getDatabase(), config.getPassword().toCharArray());
        SocketSettings.Builder socketBuiler = SocketSettings.builder();
        socketBuiler.connectTimeout(3, TimeUnit.SECONDS);
        socketBuiler.readTimeout(3, TimeUnit.SECONDS);
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(config.getHost(), config.getPort()))))
                        .credential(credential)
                        .applyToSocketSettings(builder -> builder.applySettings(socketBuiler.build()))
                        .build());

        return mongoClient;
    }


    public static MongoCollection<Document> getCollection(MongoClient mongoClient, String database, String colleciton) {
        MongoCollection<Document> collection = mongoClient.getDatabase(database).getCollection(colleciton);
        return collection;
    }

    public static void insertOne(MongoCollection<Document> collection, Document document) {
        collection.insertOne(document);
    }

    public static long getTotal(MongoCollection<Document> collection) {
        return collection.countDocuments();
    }

    public static MongoCursor<Document> loadDoument(MongoCollection<Document> collection, int pageNo, int pageSize) {
        Bson orderBy = new BasicDBObject("_id", 1);
        MongoCursor<Document> iterator = collection.find().sort(orderBy).skip((pageNo - 1) * pageSize).limit(pageSize).iterator();
        return iterator;
    }
}
