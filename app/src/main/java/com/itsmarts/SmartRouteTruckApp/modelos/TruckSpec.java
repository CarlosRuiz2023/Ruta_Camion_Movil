package com.itsmarts.SmartRouteTruckApp.modelos;

import android.graphics.Bitmap;

public class TruckSpec {
    public int id;
    public String name;
    public int toneladas;
    public int altura;
    public int ancho;
    public int largo;
    public Bitmap imagen;

    public TruckSpec(int id, String name, int toneladas, int altura, int ancho, int largo,Bitmap imagen) {
        this.id = id;
        this.name = name;
        this.toneladas = toneladas;
        this.altura = altura;
        this.ancho = ancho;
        this.largo = largo;
        this.imagen = imagen;
    }

    public int altura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    public int ancho() {
        return ancho;
    }

    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int largo() {
        return largo;
    }

    public void setLargo(int largo) {
        this.largo = largo;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int toneladas() {
        return toneladas;
    }

    public void setToneladas(int toneladas) {
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
