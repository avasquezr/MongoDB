package com.mycompany.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;
import java.util.Arrays;

public class EjercicioMongoDB {
    public static void main(String[] args) throws Exception {
        DBUtils utils = new DBUtils("");
        MongoDatabase baseDatos = utils.getDatabase("ventas");

        if(1==1){
            MongoCollection<Document> ordenCompra = baseDatos.getCollection("OrdenCompra");
            System.out.println(ordenCompra.countDocuments());
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Bson[] condiciones = {Filters.gte("fecha", sdf.parse("2020-01-01")),
                Filters.lte("fecha", sdf.parse("2020-12-31")),
                Filters.eq("$usuario", "AVASQUEZ")};
        Bson match = Aggregates.match(Filters.and(condiciones));

        Bson group = Aggregates.group("$usuario", Accumulators.sum("CantidadAuditorias", 1),
                Accumulators.max("UltimaFechaModificacion", "$fechaModificacion"));
        Bson[] etapas = {match, group};

        MongoCollection<Document> auditoriaUsuario = baseDatos.getCollection("auditoriaUsuario");

        AggregateIterable<Document> aggregate = auditoriaUsuario.aggregate(Arrays.asList(etapas));
        aggregate.forEach(documento -> {
            System.out.println(documento);
        });
    }
}
