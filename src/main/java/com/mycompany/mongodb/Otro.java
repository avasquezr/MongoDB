/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mongodb;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 *
 * @author Alvasrey
 */
public class Otro {

    public static void main(String[] args) throws ParseException {
        //IP y puerto del cluste Mongo al que se desea conectar.
        MongoClient mongoClient = MongoClients.create("mongodb://192.168.1.103:27017");

        //------------- Obtener objeto de una base datos -------------//
        MongoDatabase database = mongoClient.getDatabase("compras");

        //------------- Obtener objeto de un Collection -------------//
        MongoCollection<Document> collection = database.getCollection("Ordenes");

        Bson condicionCliente = Filters.eq("CodigoCliente", "SAVEA");
        Bson condicionEmpleado = Filters.eq("CodigoEmpleado", 6);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        Bson fechaInicial = Filters.gte("FechaOrden", sdf.parse("01/06/2016 00:00:00"));
        Bson fechaFinal = Filters.lte("FechaOrden", sdf.parse("30/06/2016 23:59:59"));

        Bson filtros[] = {Filters.gte("FechaOrden", sdf.parse("01/06/2016 00:00:00")),
            Filters.lte("FechaOrden", sdf.parse("30/06/2016 23:59:59")),
            Filters.gte("CobroTransporte", 50), Filters.lte("CobroTransporte", 80)};

        Document fieldsMostrar = new Document();
        fieldsMostrar.append("_id", 0).append("CodigoOrden", 1).append("CodigoCliente", 1);

        FindIterable<Document> find = collection.find(Filters.and(filtros)).projection(fieldsMostrar);

        for (Document document : find) {
            System.out.println("document===> " + document.toJson());
        }


        //------------- Crear una lista con las Etapas a utilizar en el Aggregate  -------------//
        List<Document> listadoEtapas = new ArrayList<>();

        //{$project:{CodigoOrden:"$CodigoOrden", CodigoCliente:"$CodigoCliente", FechaOrden:"$FechaOrden",annio:{$year:"$FechaOrden"}}},
        //----------------------------- Proyectar Atributos Para aplicar Condición ----------------------------//
        Document opcionesProject = new Document("CodigoOrden", "$CodigoOrden")
                .append("CodigoCliente", "$CodigoCliente")
                .append("FechaOrden", "$FechaOrden")
                .append("annio", new Document("$year", "$FechaOrden"));

        listadoEtapas.add(
                new Document("$project", opcionesProject));

        //----------------------------- Filtrar las Ordenes que se desea Procesar -----------------------------//
        //{$match:{"CodigoCliente":{$regex:/^V/}, "annio":2015}},
        Document condicionesMatch = new Document("CodigoCliente", new Document("$regex", Pattern.compile("^V")))
                .append("annio", 2015);
        Document match = new Document("$match", condicionesMatch);

        listadoEtapas.add(match);

        //----------------------------- Hacer Join Con la Colección del detalle -----------------------------//
        //{$lookup:{from:"DetalleOrdenes",localField:"CodigoOrden",foreignField:"CodigoOrden", as:"Detalles"}},
        Document opcionesLookup = new Document("from", "DetalleOrdenes")
                .append("localField", "CodigoOrden")
                .append("foreignField", "CodigoOrden")
                .append("as", "Detalles");
        Document lookup = new Document("$lookup", opcionesLookup);

        listadoEtapas.add(lookup);
        //-------------- UnWind --------------//
        Document unWind = new Document("$unwind", "$Detalles");

        listadoEtapas.add(unWind);

        //-------------- Determinar el Total Orden y Descuento Agrupado por los Datos Salida --------------//
        //------------ Restar a 1 - descuento -----------------------//
        //totalOrden:{$sum:{$multiply:["$Detalles.Cantidad","$Detalles.PrecioUninidad",{$subtract:[1,"$Detalles.Descuento"]}]}},
        //{$subtract:[1,"$Detalles.Descuento"]}
        List<Object> restarUnoMenosDescuento = new ArrayList<>();

        restarUnoMenosDescuento.add(
                1);
        restarUnoMenosDescuento.add(
                "$Detalles.Descuento");

        Document unoMenosDescuento = new Document("$subtract", restarUnoMenosDescuento);

        //------------ Obtener Total Detalle = Cantidad * Precio * ( 1 - Descuento ) ------------
        //{$multiply:["$Detalles.Cantidad","$Detalles.PrecioUninidad",{$subtract:[1,"$Detalles.Descuento"]}]}
        List<Object> multiplicarCantidadPrecioUnoMenosDescuento = new ArrayList<>();

        multiplicarCantidadPrecioUnoMenosDescuento.add(
                "$Detalles.PrecioUninidad");
        multiplicarCantidadPrecioUnoMenosDescuento.add(
                "$Detalles.Cantidad");
        multiplicarCantidadPrecioUnoMenosDescuento.add(unoMenosDescuento);

        Document multiplicar = new Document("$multiply", multiplicarCantidadPrecioUnoMenosDescuento);
        Document sumaTotalOrden = new Document("$sum", multiplicar);

        //------------- Obtener Total Descuento -------------//
        //totalDescuento:{$sum:{$multiply:["$Detalles.Cantidad","$Detalles.PrecioUninidad","$Detalles.Descuento"]}}
        List<Object> multiplicarPrecioCantidadDescuento = new ArrayList<>();

        multiplicarPrecioCantidadDescuento.add(
                "$Detalles.PrecioUninidad");
        multiplicarPrecioCantidadDescuento.add(
                "$Detalles.Cantidad");
        multiplicarPrecioCantidadDescuento.add(
                "$Detalles.Descuento");

        Document multiplicarParaDescuento = new Document("$multiply", multiplicarPrecioCantidadDescuento);
        Document sumaTotalDescuento = new Document("$sum", multiplicarParaDescuento);

        // _id:{CodigoOrden:"$CodigoOrden", CodigoCliente:"$CodigoCliente", FechaOrden:"$FechaOrden"},
        Document _id = new Document("CodigoOrden", "$CodigoOrden")
                .append("CodigoCliente", "$CodigoCliente")
                .append("FechaOrden", "$FechaOrden");

        Document grupo = new Document("_id", _id)
                .append("totalOrden", sumaTotalOrden)
                .append("totalDescuento", sumaTotalDescuento);

        listadoEtapas.add(
                new Document("$group", grupo));

        //-------------- Proyectar Solo los Campos Solicitados ---------------//
        opcionesProject = new Document("CodigoOrden", "$_id.CodigoOrden")
                .append("CodigoCliente", "$_id.CodigoCliente")
                .append("FechaOrden", "$_id.FechaOrden")
                .append("TotalOrden", "$totalOrden")
                .append("TotalDescuento", "$totalDescuento")
                .append("_id", 0);

        listadoEtapas.add(
                new Document("$project", opcionesProject));

        for (Document listadoEtapa : listadoEtapas) {
            System.out.println("etapa==> " + listadoEtapa.toJson());
        }

        AggregateIterable<Document> resultadoConsulta = collection.aggregate(listadoEtapas);

        resultadoConsulta.forEach(
                (var documento) -> {
                    System.out.println(documento.toJson());
                }
        );

    }

    public static void insertarEspacios() {
        for (int i = 0; i < 5; i++) {
            System.out.println("          ");

        }
    }
}
