package com.itsmarts.SmartRouteTruckApp.modelos;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapMarker;

import java.io.File;
import java.util.Date;

public class Incidencia {
    public int id;
    public int id_tipo_incidencia;
    public int id_usuario;
    public int id_ruta;
    public File foto;
    public String comentarios;
    public String direccion;
    public MapMarker mapMarker;
    public Date fecha_hora;
    public Boolean status;

    public Incidencia(int id, int id_tipo_incidencia, int id_usuario, int id_ruta, File foto, String comentarios, String direccion, MapMarker mapMarker, Date fecha_hora,  Boolean status) {
        this.comentarios = comentarios;
        this.mapMarker = mapMarker;
        this.direccion = direccion;
        this.fecha_hora = fecha_hora;
        this.foto = foto;
        this.id = id;
        this.id_ruta = id_ruta;
        this.id_tipo_incidencia = id_tipo_incidencia;
        this.id_usuario = id_usuario;
        this.status = status;
    }

    public String comentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public MapMarker mapMarker() {
        return mapMarker;
    }
    public void setMapMarker(MapMarker mapMarker) {
        this.mapMarker = mapMarker;
    }

    public String direccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Date fecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(Date fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public File foto() {
        return foto;
    }

    public void setFoto(File foto) {
        this.foto = foto;
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int id_ruta() {
        return id_ruta;
    }

    public void setId_ruta(int id_ruta) {
        this.id_ruta = id_ruta;
    }

    public int id_tipo_incidencia() {
        return id_tipo_incidencia;
    }

    public void setId_tipo_incidencia(int id_tipo_incidencia) {
        this.id_tipo_incidencia = id_tipo_incidencia;
    }

    public int id_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Boolean status() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
