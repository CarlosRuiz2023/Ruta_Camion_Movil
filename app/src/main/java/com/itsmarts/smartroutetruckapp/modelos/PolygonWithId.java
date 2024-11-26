package com.itsmarts.smartroutetruckapp.modelos;

import com.here.sdk.mapview.MapPolygon;

public class PolygonWithId {
    public int id;
    public int idRuta;
    public MapPolygon polygon;
    public String name;
    public String estado;
    public String municipio;
    public int[] truckSpecIds;
    public Boolean label;
    public Boolean peligrosa;
    public Boolean status;

    public PolygonWithId(int id,int idRuta, MapPolygon polygon,String name,String estado,String municipio,int[] truckSpecIds,Boolean label,Boolean peligrosa,Boolean status) {
        this.id = id;
        this.idRuta = idRuta;
        this.polygon = polygon;
        this.name = name;
        this.estado = estado;
        this.municipio = municipio;
        this.truckSpecIds = truckSpecIds;
        this.label = label;
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

    public int[] truckSpecIds() {
        return truckSpecIds;
    }

    public void setTruckSpecIds(int[] truckSpecIds) {
        this.truckSpecIds = truckSpecIds;
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

    public int idRuta() {
        return idRuta;
    }

    public void setIdRuta(int idRuta) {
        this.idRuta = idRuta;
    }
    public Boolean peligrosa() {
        return peligrosa;
    }
    public void setPeligrosa(Boolean peligrosa) {
        this.peligrosa = peligrosa;
    }
}
