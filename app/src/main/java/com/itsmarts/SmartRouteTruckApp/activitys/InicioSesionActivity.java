package com.itsmarts.SmartRouteTruckApp.activitys;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.itsmarts.SmartRouteTruckApp.BuildConfig;
import com.itsmarts.SmartRouteTruckApp.MainActivity;
import com.itsmarts.SmartRouteTruckApp.R;
import com.itsmarts.SmartRouteTruckApp.api.ApiService;
import com.itsmarts.SmartRouteTruckApp.api.RetrofitClient;
import com.itsmarts.SmartRouteTruckApp.bd.DatabaseHelper;
import com.itsmarts.SmartRouteTruckApp.clases.CredentialsManager;
import com.itsmarts.SmartRouteTruckApp.clases.Logger;
import com.itsmarts.SmartRouteTruckApp.fragments.ErrorDialogFragment;
import com.itsmarts.SmartRouteTruckApp.helpers.Internet;
import com.itsmarts.SmartRouteTruckApp.helpers.Messages;
import com.itsmarts.SmartRouteTruckApp.modelos.LoginRequest;
import com.itsmarts.SmartRouteTruckApp.modelos.RecuperarContraseniaRequest;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private TextView forgotPasswordText, closeSesionText, versionText, tvBanner;
    private Logger logger;
    private SwitchCompat switchEnvironment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new Logger(getApplicationContext());
        // CHECAMOS LA CONEXION A INTERNET
        logger.checkInternetConnectionAndErrors();
        setContentView(R.layout.activity_inicio_sesion);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        llLoadingSesion = findViewById(R.id.llLoadingSesion);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        versionText = findViewById(R.id.versionText);
        tvBanner = findViewById(R.id.tvBanner);
        switchEnvironment = findViewById(R.id.switchEnvironment);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean desarrollo = sharedPreferences.getBoolean("desarrollo", false);
        if(desarrollo){
            switchEnvironment.setChecked(true);
            switchEnvironment.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.deepOrange_800)));
            tvBanner.setVisibility(View.VISIBLE);
        }
        /*if(!BuildConfig.PRODUCCION){
            tvBanner.setVisibility(View.VISIBLE);
        }*/
        try {
            versionText.setText("Versión: "+getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName);
        }catch (Exception e){
            versionText.setText("Versión: 1.0.0");
        }
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //Toast.makeText(getApplicationContext(), "En desarrollo", Toast.LENGTH_SHORT).show();
                    View dialogView = getLayoutInflater().inflate(R.layout.ventana_recuperar_contrasenia, null);

                    final Dialog dialogRecuperarContrasenia = new Dialog(InicioSesionActivity.this);
                    dialogRecuperarContrasenia.setContentView(dialogView);
                    dialogRecuperarContrasenia.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    EditText et_email = dialogView.findViewById(R.id.et_email);
                    Button btn_send = dialogView.findViewById(R.id.btn_send);
                    Button btn_cancelar = dialogView.findViewById(R.id.btn_cancelar);

                    btn_send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String email = et_email.getText().toString();

                            if (isValidEmail(email) ) {
                                RecuperarContraseniaRequest recuperarContraseniaRequest = new RecuperarContraseniaRequest(email);
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                boolean desarrollo = sharedPreferences.getBoolean("desarrollo", false);
                                // Use Retrofit to make the POST request
                                ApiService apiService = RetrofitClient.getInstance(null,desarrollo).create(ApiService.class);
                                Call<ResponseBody> call = apiService.recuperarContrasenia(recuperarContraseniaRequest);

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
                                                    dialogRecuperarContrasenia.dismiss();
                                                    Toast.makeText(getApplicationContext(), "Correo enviado con exito", Toast.LENGTH_SHORT).show();
                                                    logger.trackActivity(TAG,"Recuperar contraseña","El usuario solicito el cambio de contraseña para el correo: "+email);
                                                }
                                                Log.d("Retrofit", "Solicitud exitosa.");
                                            } catch (Exception e) {
                                                Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Correo no encontrado intente nuevamente", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                        Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btn_cancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogRecuperarContrasenia.dismiss();
                        }
                    });

                    dialogRecuperarContrasenia.show();
                } catch (Exception e) {
                    Log.e(TAG, "Error al mostrar el diálogo", e);
                }
            }
        });
        // Verificar si el usuario ya está logueado
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        String correo = preferences.getString("correo", "");
        if (isLoggedIn) {
            etUsername.setText(correo);
            etPassword.setText("");
            /*// Si el usuario ya ha iniciado sesión, redirigir a MainActivity
            Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Evitar que el usuario regrese a la pantalla de login al presionar 'atrás'*/
        }else{
            etUsername.setText("play_store@gmail.com");
            etPassword.setText("ItsMarts1*");
        }
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
                    llLoadingSesion.setVisibility(View.VISIBLE);
                    // Create a LoginRequest object with the provided username and password
                    if(Internet.isNetworkConnected()){
                        LoginRequest loginRequest = new LoginRequest(username, password);
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        boolean desarrollo = sharedPreferences.getBoolean("desarrollo", false);

                        // Use Retrofit to make the POST request
                        ApiService apiService = RetrofitClient.getInstance(null,desarrollo).create(ApiService.class);
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
                                                editor.putString("password", password);
                                                editor.putString("telefono", telefono);
                                                editor.putInt("id_rol", id_rol);
                                                editor.putBoolean("isLoggedIn", true); // Update login state
                                                editor.putBoolean("isLoggedSinConexion", false); // Update login state
                                                // Commit the changes
                                                editor.apply();
                                                Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                // Obtén el objeto "result"
                                                Toast.makeText(getApplicationContext(), "Usuario logueado con exito", Toast.LENGTH_SHORT).show();
                                                logger.trackActivity(TAG,"Inicio sesion","El usuario inicio sesion bajo el correo: "+username+" y la contraseña: "+password);
                                                //finish();
                                            } else {
                                                // Handle login failure (e.g., display error message)
                                                llLoadingSesion.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(), "Solo puede acceder un operador a la Aplicacion", Toast.LENGTH_SHORT).show();
                                                logger.trackActivity(TAG,"Inicio sesion invalido","El usuario inicio sesion bajo el correo: "+username+" y la contraseña: "+password+" dicho usuario no es operador");
                                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                                boolean desarrollo = sharedPreferences.getBoolean("desarrollo", false);
                                                // Use Retrofit to make the POST request
                                                ApiService apiService = RetrofitClient.getInstance(token,desarrollo).create(ApiService.class);
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
                                    if (response.code() == 405){
                                        Messages.showInvalidCredentialsDialog("Usuario Bloqueado","Demasiados intentos fallidos.\n Intente más tarde.",InicioSesionActivity.this);
                                    }else if (response.code() == 406){
                                        Dialog sesionActivaDialog = new Dialog(InicioSesionActivity.this);
                                        sesionActivaDialog.setContentView(R.layout.ventana_sesion_activa);
                                        sesionActivaDialog.setCancelable(false);
                                        sesionActivaDialog.setCanceledOnTouchOutside(false);

                                        Button btnCerrarSesion = sesionActivaDialog.findViewById(R.id.btnCerrarSesion);
                                        Button btnCancelar = sesionActivaDialog.findViewById(R.id.btnCancelar);

                                        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                llLoadingSesion.setVisibility(View.VISIBLE);
                                                // Create a LoginRequest object with the provided username and password
                                                if(Internet.isNetworkConnected()){
                                                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                                    int id_usuario = sharedPreferences.getInt("id_usuario", 0);
                                                    boolean desarrollo = sharedPreferences.getBoolean("desarrollo", false);
                                                    // Use Retrofit to make the POST request
                                                    ApiService apiService = RetrofitClient.getInstance(null,desarrollo).create(ApiService.class);
                                                    Call<ResponseBody> call1 = apiService.getDesloguearUsuario(id_usuario);

                                                    call1.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(@NonNull Call<ResponseBody> call1, @NonNull Response<ResponseBody> response) {
                                                            Toast.makeText(getApplicationContext(), "Sesion Cerrada con exito", Toast.LENGTH_SHORT).show();
                                                            logger.trackActivity(TAG,"Sesion cerrada","El usuario cerro la sesion del usuario: "+username);
                                                            llLoadingSesion.setVisibility(View.GONE);
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull Call<ResponseBody> call1, @NonNull Throwable t) {
                                                            Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                                                        }
                                                    });
                                                }else {
                                                    llLoadingSesion.setVisibility(View.GONE);
                                                    DialogFragment errorDialog = new ErrorDialogFragment();
                                                    errorDialog.show(getSupportFragmentManager(), "errorDialog");
                                                }
                                                sesionActivaDialog.dismiss();
                                            }
                                        });

                                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                sesionActivaDialog.dismiss();
                                            }
                                        });
                                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                        String correo = sharedPreferences.getString("correo", "");
                                        String password = sharedPreferences.getString("password", "");
                                        if( username.equalsIgnoreCase(correo) && password.equalsIgnoreCase(password)){
                                            btnCerrarSesion.setVisibility(View.VISIBLE);
                                        }
                                        sesionActivaDialog.show();
                                    }else if (response.code() == 400){
                                        Messages.showInvalidCredentialsDialog("Error de autenticación","Usuario o contraseña incorrectos.",InicioSesionActivity.this);
                                        logger.trackActivity(TAG,"Inicio sesion invalido","El usuario inicio sesion bajo el correo: "+username+" y la contraseña: "+password+" donde las credenciales no corresponden");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                llLoadingSesion.setVisibility(View.GONE);
                                Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                            }
                        });
                    }else{
                        llLoadingSesion.setVisibility(View.GONE);
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String correo = sharedPreferences.getString("correo", "ItsMarts1*");
                        String passwordSharedPreferences = sharedPreferences.getString("password", "ItsMarts1*");
                        if( username.equalsIgnoreCase(correo) && passwordSharedPreferences.equalsIgnoreCase(password)){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedSinConexion", true); // Update login state
                            editor.apply();
                            Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
                            startActivity(intent);
                            // Obtén el objeto "result"
                            Toast.makeText(getApplicationContext(), "Usuario logueado sin conexion con exito", Toast.LENGTH_SHORT).show();
                            logger.trackActivity(TAG,"Inicio sesion","El usuario inicio sesion bajo el correo: "+username+" y la contraseña: "+password+" sin conexion a internet");
                        }else{
                            DialogFragment errorDialog = new ErrorDialogFragment();
                            errorDialog.show(getSupportFragmentManager(), "errorDialog");
                            logger.trackActivity(TAG,"Inicio sesion invalido","El usuario inicio sesion bajo el correo: "+username+" y la contraseña: "+password+" sin exito y sin conexion a internet");
                        }
                    }
                }
            }
        });
        switchEnvironment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                if(switchEnvironment.isChecked()){
                    switchEnvironment.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.deepOrange_800)));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("desarrollo", true);
                    editor.apply();
                    tvBanner.setVisibility(View.VISIBLE);
                }else{
                    switchEnvironment.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("desarrollo", false);
                    editor.apply();
                    tvBanner.setVisibility(View.GONE);
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
    // Método para validar el formato de correo electrónico
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String emailPatternMX = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern) || email.matches(emailPatternMX);
    }
}