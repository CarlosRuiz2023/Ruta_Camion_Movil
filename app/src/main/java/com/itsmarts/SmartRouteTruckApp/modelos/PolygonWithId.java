package com.itsmarts.SmartRouteTruckApp.modelos;

import com.here.sdk.mapview.MapPolygon;

public class PolygonWithId {
    public int id;
    public MapPolygon polygon;
    public String name;
    public int id_estado;
    public int id_municipio;
    public int[] truckSpecIds;
    public Boolean label;
    public Boolean visibility;
    public Boolean peligrosa;
    public Boolean status;

    public PolygonWithId(int id, MapPolygon polygon,String name,int id_estado,int id_municipio,int[] truckSpecIds,Boolean label,Boolean visibility,Boolean peligrosa,Boolean status) {
        this.id = id;
        this.polygon = polygon;
        this.name = name;
        this.id_estado = id_estado;
        this.id_municipio = id_municipio;
        this.truckSpecIds = truckSpecIds;
        this.label = label;
        this.visibility = visibility;
        this.peligrosa = peligrosa;
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

    public MapPolygon polygon() {
        return polygon;
    }

    public void setPolygon(MapPolygon polygon) {
        this.polygon = polygon;
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

    public Boolean visibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public int[] truckSpecIds() {
        return truckSpecIds;
    }

    public void setTruckSpecIds(int[] truckSpecIds) {
        this.truckSpecIds = truckSpecIds;
    }

    public int id_estado() {
        return id_estado;
    }

    public void setIdEstado(int id_estado) {
        this.id_estado = id_estado;
    }

    public int id_municipio() {
        return id_municipio;
    }

    public void setIdMunicipio(int id_municipio) {
        this.id_municipio = id_municipio;
    }

    public Boolean peligrosa() {
        return peligrosa;
    }
    public void setPeligrosa(Boolean peligrosa) {
        this.peligrosa = peligrosa;
    }
}
