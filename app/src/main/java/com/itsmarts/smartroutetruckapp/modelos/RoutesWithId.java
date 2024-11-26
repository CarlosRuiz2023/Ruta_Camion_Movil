package com.itsmarts.smartroutetruckapp.modelos;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapPolyline;

import java.io.Serializable;
import java.util.Date;

public class RoutesWithId implements Serializable{
    public int id;
    public String name;
    public GeoCoordinates coordinatesInicio,coordinatesFin;
    public Date fecha_creacion,fecha_ultima_modificacion;
    public MapPolyline polyline;
    public int[] truckSpectIds;
    public int status;

    public RoutesWithId(int id, String name, GeoCoordinates coordinatesInicio, GeoCoordinates coordinatesFin, Date fecha_creacion, Date fecha_ultima_modificacion, MapPolyline polyline, int[] truckSpectIds, int status) {
        this.id = id;
        this.name = name;
        this.coordinatesInicio = coordinatesInicio;
        this.coordinatesFin = coordinatesFin;
        this.fecha_creacion = fecha_creacion;
        this.fecha_ultima_modificacion = fecha_ultima_modificacion;
        this.polyline = polyline;
        this.truckSpectIds = truckSpectIds;
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

    public GeoCoordinates coordinatesFin() {
        return coordinatesFin;
    }

    public void setCoordinatesFin(GeoCoordinates coordinatesFin) {
        this.coordinatesFin = coordinatesFin;
    }

    public GeoCoordinates coordinatesInicio() {
        return coordinatesInicio;
    }

    public void setCoordinatesInicio(GeoCoordinates coordinatesInicio) {
        this.coordinatesInicio = coordinatesInicio;
    }

    public Date fecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(Date fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public Date fecha_ultima_modificacion() {
        return fecha_ultima_modificacion;
    }

    public void setFecha_ultima_modificacion(Date fecha_ultima_modificacion) {
        this.fecha_ultima_modificacion = fecha_ultima_modificacion;
    }

    public MapPolyline polyline() {
        return polyline;
    }

    public void setPolyline(MapPolyline polyline) {
        this.polyline = polyline;
    }

    public int status() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int[] truckSpectIds() {
        return truckSpectIds;
    }

    public void setTruckSpectIds(int[] truckSpectIds) {
        this.truckSpectIds = truckSpectIds;
    }
    /*
     // Your existing class fields and constructor

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        // Write each field value to the parcel using appropriate methods
        // (e.g., dest.writeDouble(coordinatesInicio.latitude), etc.)
    }

    public static final Creator<RoutesWithId> CREATOR = new Creator<RoutesWithId>() {
        @Override
        public RoutesWithId createFromParcel(Parcel in) {
            return new RoutesWithId(in);
        }

        @Override
        public RoutesWithId[] newArray(int size) {
            return new RoutesWithId[size];
        }
    };

    private RoutesWithId(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        // Read each field value from the parcel (e.g., coordinatesInicio)
    }
     */
}