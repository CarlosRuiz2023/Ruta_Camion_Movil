package com.itsmarts.smartroutetruckapp.modelos;
public class RecuperarContraseniaRequest {
    private String correo;

    public RecuperarContraseniaRequest(String correo) {
        this.correo = correo;
    }

    public String correo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
