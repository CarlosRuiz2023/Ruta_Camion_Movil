package com.itsmarts.smartroutetruckapp.modelos;
public class LoginRequest {
    private String correo;
    private String contrasenia;

    public LoginRequest(String correo, String contrasenia) {
        this.correo = correo;
        this.contrasenia = contrasenia;
    }

    public String contrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String correo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
