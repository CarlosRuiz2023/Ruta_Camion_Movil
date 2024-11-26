package com.itsmarts.smartroutetruckapp.modelos;

import android.graphics.Bitmap;

public class TruckSpec {
    public int id;
    public String name;
    public double toneladas;
    public double altura;
    public double ancho;
    public double largo;
    public Bitmap imagen;

    public TruckSpec(int id, String name, double toneladas, double altura, double ancho, double largo,Bitmap imagen) {
        this.id = id;
        this.name = name;
        this.toneladas = toneladas;
        this.altura = altura;
        this.ancho = ancho;
        this.largo = largo;
        this.imagen = imagen;
    }

    public double altura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double ancho() {
        return ancho;
    }

    public void setAncho(double ancho) {
        this.ancho = ancho;
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double largo() {
        return largo;
    }

    public void setLargo(double largo) {
        this.largo = largo;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double toneladas() {
        return toneladas;
    }

    public void setToneladas(double toneladas) {
        this.toneladas = toneladas;
    }
    public String toString() {
        return name;
    }

    public Bitmap getImagen() {
        return imagen;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }
}
