package com.itsmarts.smartroutetruckapp.activitys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.api.ApiService;
import com.itsmarts.smartroutetruckapp.api.RetrofitClient;
import com.itsmarts.smartroutetruckapp.clases.CredentialsManager;
import com.itsmarts.smartroutetruckapp.fragments.ErrorDialogFragment;
import com.itsmarts.smartroutetruckapp.helpers.Internet;
import com.itsmarts.smartroutetruckapp.modelos.LoginRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioSesionActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageView ivTogglePassword;
    private boolean passwordVisible = false;
    private CredentialsManager credentialsManager;
    private static final String TAG = "InicioSesionActivity";
    private LinearLayout llLoadingSesion;
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
        llLoadingSesion = findViewById(R.id.llLoadingSesion);
        etUsername.setText("example@gmail.com");
        etPassword.setText("123456");
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
                    /*if (credentialsManager.validateCredentials(username, password)) {
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
                    }*/
                    llLoadingSesion.setVisibility(View.VISIBLE);
                    // Create a LoginRequest object with the provided username and password
                    if(Internet.isNetworkConnected()){
                        LoginRequest loginRequest = new LoginRequest(username, password);

                        // Use Retrofit to make the POST request
                        ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
                        Call<ResponseBody> call = apiService.loguearse(loginRequest);

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    try {
                                        // Obtener el JSON como string
                                        String jsonResponse = response.body().string();
                                        // Convierte la respuesta en un objeto JSON
                                        JSONObject jsonObject = new JSONObject(jsonResponse);
                                        // Verifica si la operación fue exitosa
                                        boolean success = jsonObject.getBoolean("success");
                                        JSONObject resultObject = null;
                                        if (success) {
                                            // Obtén el objeto "result"
                                            resultObject = jsonObject.getJSONObject("result");
                                            String token = resultObject.getString("token");
                                            JSONArray usuario = resultObject.getJSONArray("usuario");
                                            int id_usuario = usuario.getJSONObject(0).getInt("id_usuario");
                                            String nombres = usuario.getJSONObject(0).getString("nombres");
                                            String apellido_paterno = usuario.getJSONObject(0).getString("apellido_paterno");
                                            String apellido_materno = usuario.getJSONObject(0).getString("apellido_materno");
                                            String correo = usuario.getJSONObject(0).getString("correo");
                                            String telefono = usuario.getJSONObject(0).getString("telefono");

                                            JSONObject rolUsuario = usuario.getJSONObject(0).getJSONObject("rolUsuario");
                                            int id_rol = rolUsuario.getInt("id_rol");
                                            if (id_rol == 6) {
                                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                                // Store the token
                                                editor.putString("token", token);

                                                // Store user information
                                                editor.putInt("id_usuario", id_usuario);
                                                editor.putString("nombres", nombres);
                                                editor.putString("apellido_paterno", apellido_paterno);
                                                editor.putString("apellido_materno", apellido_materno);
                                                editor.putString("correo", correo);
                                                editor.putString("telefono", telefono);
                                                editor.putInt("id_rol", id_rol);
                                                editor.putBoolean("isLoggedIn", true); // Update login state

                                                // Commit the changes
                                                editor.apply();
                                                Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                // Obtén el objeto "result"
                                                Toast.makeText(getApplicationContext(), "Usuario logueado con exito", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                // Handle login failure (e.g., display error message)
                                                llLoadingSesion.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(), "Solo puede acceder un operador a la Aplicacion", Toast.LENGTH_SHORT).show();
                                                // Use Retrofit to make the POST request
                                                ApiService apiService = RetrofitClient.getInstance(token).create(ApiService.class);
                                                Call<ResponseBody> call1 = apiService.desloguearse();

                                                call1.enqueue(new Callback<ResponseBody>() {
                                                    @Override
                                                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                                        Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                                                    }
                                                });
                                            }
                                        }
                                        Log.d("Retrofit", "Logueo exitoso.");
                                    } catch (Exception e) {
                                        llLoadingSesion.setVisibility(View.GONE);
                                        Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                                    }
                                } else {
                                    llLoadingSesion.setVisibility(View.GONE);
                                    showInvalidCredentialsDialog();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                            }
                        });
                    }else{
                        DialogFragment errorDialog = new ErrorDialogFragment();
                        errorDialog.show(getSupportFragmentManager(), "errorDialog");
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
    // Método para mostrar un diálogo de error
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