/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Projections.*;

/**
 *
 * @author avasquez
 */
public class CRUDModel {

    public void insertarDocumentoFactura(MongoDatabase database) {
        MongoCollection<Document> facturacion = database.getCollection("facturacion");

        // Preparar los documentos y subdocumentos que serán insertados.
        Document docFactura = new Document();
        Document docCliente = new Document();

        // Aquí se utilizará el método append(), el cual es sobrecargado y recibe
        // como parámetro un key y un objeto como valor.También recibe un Map.EntrySet<Key,Value>
        docFactura.append("codigoFactura", 1).append("fechaFactura", new Date());

        // Para insertar los datos del cliente, se utilizará un Map
        HashMap<String, Object> datosCliente = new HashMap<>();
        // ArrayList para insertar un listado de teléfonos
        List<String> telefonos = new ArrayList<>();
        telefonos.add("809-895-9841");
        telefonos.add("829-985-9852");

        datosCliente.put("codigoCliente", 1);
        datosCliente.put("nombre", "María Ramirez");
        datosCliente.put("telefonos", telefonos);

        // Insertar los datos del Map al documento cliente
        docCliente.putAll(datosCliente);
        // Insertar los datos del cliente en el documento factura
        docFactura.put("cliente", docCliente);
        // Insertar el documento factura a la colección facturacion
        facturacion.insertOne(docFactura);

    }

    public void insertarVariosDocumentosAuditoria(MongoDatabase database) {
        // Colección para almacenar auditoria de los cambios realizados por un Usuario
        MongoCollection<Document> auditoriaUsuario = database.getCollection("auditoriaUsuario");

        // Llenar una lista con los datos de la auditoría que serán insertados
        List<Document> listadoAuditoria = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            listadoAuditoria.add(new Document().append("_id", i)
                    .append("modificacion", "Cambios Factura No." + i)
                    .append("fechaModificacion", UtilsRandom.getFechaRandom())
                    .append("usuario", UtilsRandom.getUsuarioRandom()));

        }

        // Insertar la auditoría utilizando insertMany de la Clase Collection
        auditoriaUsuario.insertMany(listadoAuditoria);
    }

    public void consultarAuditoria(MongoDatabase database) {
        // Colección para almacenar auditoria de los cambios realizados por un Usuario
        MongoCollection<Document> auditoriaUsuario = database.getCollection("auditoriaUsuario");

        // Obtener un cursor con el resultado del find(), utilizado para consultar las auditorías.

        try (MongoCursor<Document> cursorAuditoria = auditoriaUsuario.find().iterator()) {
            // Recorrer cada documento del cursor, utilizando hasNext() para verificar si tiene
            // un próximo documento y next() para obtener el mismo.
            while (cursorAuditoria.hasNext()) {
                System.out.println(cursorAuditoria.next());
            }
        }
    }

    public void consultarAuditoriaFiltro(MongoDatabase database) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Colección para almacenar auditoria de los cambios realizados por un Usuario
        MongoCollection<Document> auditoriaUsuario = database.getCollection("auditoriaUsuario");

        // Obtener las modificaciones realizadas por el usuario AVASQUEZ entre el 01/03/2019 y 15/08/2019
        Bson[] condiciones = {Filters.gte("fechaModificacion", sdf.parse("2019-03-01")),
            Filters.lte("fechaModificacion", sdf.parse("2020-04-15")),
            Filters.eq("usuario", "AVASQUEZ")};
        // Utilizando el conector lógica and(), agregar las condiciones insertadas en el arreglo condiciones
        FindIterable<Document> consulta = auditoriaUsuario.find(Filters.and(condiciones))
                .sort(Sorts.ascending("fechaModificacion"));
        // Recorrer cada resultado y presentar el mismo en la pantalla.
        consulta.forEach(documento -> {
            System.out.println("Usuario: " + documento.getString("usuario")
                    + "\nModificación: " + documento.getString("modificacion")
                    + "\nFecha : " + documento.getDate("fechaModificacion")
                    + "\n-----------------------------------------------------------");
        });

    }

    public void listadoTotalVendidoPorCliente(MongoDatabase database) throws ParseException {

        // Obtener una referencia a la colección factura, la cual contiene los datos de las facturas a analizar
        MongoCollection<Document> factura = database.getCollection("factura");

        // Primero crear la etapa de Match para filtrar los documentos de los meses de Febrero y marzo del 2020
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Bson[] condiciones = {Filters.gte("fecha", sdf.parse("2019-02-01")),
                              Filters.lte("fecha", sdf.parse("2020-03-31"))};
        Bson match = Aggregates.match(Filters.and(condiciones));

        // Convertir la lista de documentos de Detalles a Inner Document
        Bson unwind = Aggregates.unwind("$Detalles");
        // Preparando los atributos que serán proyectados.
        Bson fields = Projections.fields(excludeId(), include("codigo"), include("cliente"),
                                            computed("year", new Document("$year", "$fecha")),
                                            computed("importe", "$Detalles.Importe"));
        Bson project = Aggregates.project(fields);

        // Agrupar el importe por Cliente y Año, utilizando la etapa de $group
        //1. Crear el ID del Grupo, ya que es compuesto:
        Document groupId = new Document("codigo", "$codigo").append("cliente", "$cliente")
                .append("annio", "$year");
        Bson group = Aggregates.group(groupId, Accumulators.sum("montoTotal", "$importe"));

        // Crear un arreglo con las etapas a aplicar al aggregate. Tomando en cuenta cual debe
        // ser enviada primero.
        Bson[] etapas = {match, unwind, project, group};
        AggregateIterable<Document> aggregate = factura.aggregate(Arrays.asList(etapas));
        aggregate.forEach(documento -> {
            System.out.println(documento);
        });
    }
}
