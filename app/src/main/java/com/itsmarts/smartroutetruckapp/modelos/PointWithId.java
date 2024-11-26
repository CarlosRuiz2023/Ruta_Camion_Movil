package com.itsmarts.smartroutetruckapp.modelos;

import com.here.sdk.mapview.MapMarker;

public class PointWithId {
    public int id;
    public int idRuta;
    public MapMarker mapMarker;
    public String name;
    public String estado;
    public String municipio;
    public int[] truckSpecIds;
    public Boolean label;
    public Boolean status;

    public PointWithId(int id, int idRuta, MapMarker mapMarker, String name, String estado, String municipio, int[] truckSpecIds,Boolean label, Boolean status) {
        this.id = id;
        this.idRuta = idRuta;
        this.mapMarker = mapMarker;
        this.name = name;
        this.estado = estado;
        this.municipio = municipio;
        this.truckSpecIds = truckSpecIds;
        this.label = label;
        this.status = status;
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MapMarker mapMarker() {
        return mapMarker;
    }

    public void setMapMarker(MapMarker mapMarker) {
        this.mapMarker = mapMarker;
    }

    public Boolean status() {
        return status;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean label() {
        return label;
    }

    public void setLabel(Boolean label) {
        this.label = label;
    }

    public String estado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String municipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public int[] truckSpecIds() {
        return truckSpecIds;
    }

    public void setTruckSpecIds(int[] truckSpecIds) {
        this.truckSpecIds = truckSpecIds;
    }

    public int idRuta() {
        return idRuta;
    }

    public void setIdRuta(int idRuta) {
        this.idRuta = idRuta;
    }
}
