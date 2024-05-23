package com.mycompany.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DBUtils {
    private static MongoClient mongoClient;

    public DBUtils(String parDatosConexion) {
        getMongoDBClient(parDatosConexion);
    }

    public MongoDatabase getDatabase(String parNombreDatabase) {
        MongoDatabase database = mongoClient.getDatabase(parNombreDatabase);
        return database;
    }

    private MongoClient getMongoDBClient(String parStringConexion) {
        String connectionString = "mongodb+srv://user01:abcd1234@cluster0.qxejann.mongodb.net/";
        if (parStringConexion != null && !parStringConexion.isEmpty()) {
            connectionString = parStringConexion;
        }
        /*ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

         */
        // Crear un cliente de MongoDB y conectar el mismo, utilizando la IP y el puerto
        MongoClient lmongoClient = MongoClients.create(connectionString);
        mongoClient = lmongoClient;
        return mongoClient;

    }
}
