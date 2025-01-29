package com.itsmarts.SmartRouteTruckApp.modelos;
public class HistorialRequest {
    private int id_ruta;
    private int id_usuario;

    public HistorialRequest(int id_ruta, int id_usuario) {
        this.id_ruta = id_ruta;
        this.id_usuario = id_usuario;
    }

    public int id_ruta() {
        return id_ruta;
    }

    public void setId_ruta(int id_ruta) {
        this.id_ruta = id_ruta;
    }

    public int id_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }
}
