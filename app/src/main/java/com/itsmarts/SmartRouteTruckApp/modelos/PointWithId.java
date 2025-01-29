package com.itsmarts.SmartRouteTruckApp.modelos;

import com.here.sdk.mapview.MapMarker;

public class PointWithId {
    public int id;
    public MapMarker mapMarker;
    public String name;
    public int id_estado;
    public int id_municipio;
    public int[] truckSpecIds;
    public Boolean label;
    public Boolean visibility;
    public Boolean status;

    public PointWithId(int id, MapMarker mapMarker, String name, int id_estado, int id_municipio, int[] truckSpecIds,Boolean label,Boolean visibility, Boolean status) {
        this.id = id;
        this.mapMarker = mapMarker;
        this.name = name;
        this.id_estado = id_estado;
        this.id_municipio = id_municipio;
        this.truckSpecIds = truckSpecIds;
        this.label = label;
        this.visibility = visibility;
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

    public Boolean visibility() {
        return visibility;
    }
    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
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

    public int id_estado() {
        return id_estado;
    }

    public void setId_estado(int id_estado) {
        this.id_estado = id_estado;
    }

    public int id_municipio() {
        return id_municipio;
    }

    public void setId_municipio(int id_municipio) {
        this.id_municipio = id_municipio;
    }

    public int[] truckSpecIds() {
        return truckSpecIds;
    }

    public void setTruckSpecIds(int[] truckSpecIds) {
        this.truckSpecIds = truckSpecIds;
    }
}
