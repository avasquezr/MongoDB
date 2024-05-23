package com.mycompany.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.text.ParseException;
import org.bson.Document;

/**
 *
 * @author Alvasrey
 */
public class MongoDB {

    public static void main(String[] args) throws ParseException {

        String connectionString = "mongodb+srv://alvasrey:1234@cluster0.qxejann.mongodb.net/?retryWrites=true&w=majority";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        // Crear un cliente de MongoDB y conectar el mismo, utilizando la IP y el puerto
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            //------------- Obtener objeto de una base datos -------------//
            MongoDatabase database = mongoClient.getDatabase("ventas");
            //------------- Obtener objeto de un Collection -------------//
            MongoCollection<Document> collection = database.getCollection("OrdenCompra");
            //------------- Obtener Cantidad Documentos de la colección -------------//
            long countDocuments = collection.countDocuments();
            System.out.println("\n\n\nCantidad Documentos Colección OrdenCompra==> " + countDocuments + "\n\n");
            
            CRUDModel crudModel = new CRUDModel();
            crudModel.insertarVariosDocumentosAuditoria(database);
            //crudModel.listadoTotalVendidoPorCliente(database);
            crudModel.consultarAuditoriaFiltro(database);
        }
    }

}



