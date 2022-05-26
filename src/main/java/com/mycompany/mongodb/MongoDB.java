package com.mycompany.mongodb;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;

/**
 *
 * @author Alvasrey
 */
public class MongoDB {

    public static void main(String[] args) throws ParseException {
              
        // Crear un cliente de MongoDB y conectar el mismo, utilizando la IP y el puerto
        try (MongoClient mongoClient = MongoClients.create("mongodb://172.16.28.170:27017")) {
            //------------- Obtener objeto de una base datos -------------//
            MongoDatabase database = mongoClient.getDatabase("compras");
            //------------- Obtener objeto de un Collection -------------//
            MongoCollection<Document> collection = database.getCollection("OrdenCompra");
            //------------- Obtener Cantidad Documentos de la colección -------------//
            long countDocuments = collection.countDocuments();
            System.out.println("\n\n\nCantidad Documentos Colección OrdenCompra==> " + countDocuments + "\n\n");
            
            CRUDModel crudModel = new CRUDModel();
            crudModel.listadoTotalVendidoPorCliente(database);
        }
    }

}



