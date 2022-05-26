/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mongodb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author avasquez
 */
public class UtilsRandom {

    public static String getUsuarioRandom() {

        String[] usuarios = new String[]{"ASANTANA", "AVASQUEZ", "RVALDERA", "MCASTRO"};
        int indice = (int) Math.floor((Math.random() * 4));
        return usuarios[indice];
    }

    public static Date getFechaRandom() {
        int[] years = new int[]{2018, 2019, 2020, 2021};

        int indice = (int) Math.floor((Math.random() * 4));
        int year = years[indice];
        int mes = (int) Math.floor((Math.random() * 12)) + 1;
        int dia = (int) Math.floor((Math.random() * 30) + 1);
        if (mes == 2 && dia > 28) {
            dia = 28;
        }
        int hora = (int) Math.floor((Math.random() * 60) + 1);
        int minuto = (int) Math.floor((Math.random() * 60) + 1);
        int segundos = (int) Math.floor((Math.random() * 60) + 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(String.format("%d-%02d-%02d %02d:%02d:%02d", year, mes, dia, hora, minuto, segundos));
        } catch (ParseException ex) {
            Logger.getLogger(CRUDModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
