package com.itsmarts.smartroutetruckapp.clases;

import java.util.HashMap;

public class CredentialsManager
{
    // Mapa de credenciales (usuario: contraseña)
    private HashMap<String, String> credentials;
    public CredentialsManager() {
        // Inicializar el mapa con credenciales predeterminadas
        credentials = new HashMap<>();
        credentials.put("admin", "Admin-12345");
        credentials.put("operador", "Operador-1234");
        credentials.put("monitorista", "Monitorista-123");
    }
    // Método para validar las credenciales
    public boolean validateCredentials(String username, String password) {
        // Verificar si el usuario existe y la contraseña coincide
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }
}
