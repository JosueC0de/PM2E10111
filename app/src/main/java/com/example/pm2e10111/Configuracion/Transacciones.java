package com.example.pm2e10111.Configuracion;

public class Transacciones {

    public static final String NameDatabase = "PM2ExamenCuentas";

    public static final String tablaContactos = "contactos";

    public static final String id = "id";
    public static final String pais = "pais";
    public static final String nombre = "nombre";
    public static final String telefono = "telefono";
    public static final String nota = "nota";
    public static final String imagen = "imagen";


    public static final String CreateTableContactos = "CREATE TABLE " + tablaContactos + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "pais TEXT, " +
            "nombre TEXT, " +
            "telefono TEXT, " +
            "nota TEXT, " +
            "imagen BLOB )";

    public static final String DropTableContactos = "DROP TABLE IF EXISTS " + tablaContactos;

    public static final String SelectTableContactos = "SELECT * FROM " + tablaContactos;
}
