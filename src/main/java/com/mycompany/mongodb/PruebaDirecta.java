package com.mycompany.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.function.Consumer;

public class PruebaDirecta {
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://user01:abcd1234@cluster0.qxejann.mongodb.net/";
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase("ventas");
        MongoCollection<Document> ordenCompra = database.getCollection("OrdenCompra");
        System.out.println(ordenCompra.countDocuments());
        Bson condicionNumeroOrden = Filters.gte("numeroOrden", 219);
        FindIterable<Document> registrosOrden = ordenCompra.find(condicionNumeroOrden).
                sort(new Document("numeroOrden",1)).limit(10);
        Consumer<Document> consumer = document -> {
            System.out.println(document.toJson());
        };
        registrosOrden.forEach(consumer);
    }
}
