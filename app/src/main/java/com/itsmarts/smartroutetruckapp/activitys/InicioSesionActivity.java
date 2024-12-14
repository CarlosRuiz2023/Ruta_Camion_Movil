package com.itsmarts.smartroutetruckapp.activitys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.CredentialsManager;

public class InicioSesionActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageView ivTogglePassword;
    private boolean passwordVisible = false;
    private CredentialsManager credentialsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Verificar si el usuario ya está logueado
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        /*if (isLoggedIn) {
            // Si el usuario ya ha iniciado sesión, redirigir a MainActivity
            Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Evitar que el usuario regrese a la pantalla de login al presionar 'atrás'
        }*/
        setContentView(R.layout.activity_inicio_sesion);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        etUsername.setText("admin");
        etPassword.setText("Admin-12345");
        // Inicializar el administrador de credenciales
        credentialsManager = new CredentialsManager();
        // Capturar el layout principal
        View rootView = findViewById(R.id.loginLayout);
        // Configurar un listener para detectar toques fuera del EditText
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Ocultar el teclado
                hideKeyboard();
                return false;
            }
        });
        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordVisible) {
                    // Ocultar contraseña
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivTogglePassword.setImageResource(R.drawable.visibility_off);
                } else {
                    // Mostrar contraseña
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivTogglePassword.setImageResource(R.drawable.visibility);
                }
                passwordVisible = !passwordVisible;
                etPassword.setSelection(etPassword.getText().length());
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (username.isEmpty()) {
                    etUsername.setError(getString(R.string.error_vacio_username));
                    etUsername.requestFocus();
                } else if (password.isEmpty()) {
                    etPassword.setError(getString(R.string.error_vacio_password));
                    etPassword.requestFocus();
                } else {
                    if (credentialsManager.validateCredentials(username, password)) {
                        // Guardar estado de sesión en SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                        // Redirigir a MainActivity
                        Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();  // Evitar volver a la pantalla de login
                    } else {
                        // Credenciales incorrectas
                        showInvalidCredentialsDialog();
                    }
                }
            }
        });
    }
    // Método para ocultar el teclado
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void showInvalidCredentialsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_error_credenciales_titulo));
        builder.setMessage(getString(R.string.dialog_error_credenciale_mensaje));
        builder.setPositiveButton(getString(R.string.error_credenciale_acceptar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}