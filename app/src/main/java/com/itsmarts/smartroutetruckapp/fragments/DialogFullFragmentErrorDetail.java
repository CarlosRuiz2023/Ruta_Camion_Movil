package com.itsmarts.smartroutetruckapp.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.Logger;

public class DialogFullFragmentErrorDetail extends DialogFragment {
    public static final String TAG = "DialogFullFragmentErrorDetail";
    TextView TxtPresentacion, TxtError;
    ImageView ImgTelcel;
    MaterialButton BtnSalir;
    MaterialButton BtnSend;
    TextView TxtMensajeEnvio;

    private String error;

    public DialogFullFragmentErrorDetail(String error) {
        this.error = error;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_error, container, false);

        //INICIALIZA LOS COMPONENTES
        inicializeComponents(view);

        TxtError.setText(error);

        BtnSalir.setOnClickListener(v -> {
            dismiss();
            // Forzar la finalización de la aplicación
            //System.exit(0);
        });

        BtnSend.setOnClickListener(v -> {
            if (Logger.isNetworkAvailable()) {
                Logger.checkInternetConnectionAndErrors();
                dismiss();
            }else{
                TxtMensajeEnvio.setText(R.string.error_internet);
            }
        });

        return view;
    }

    private void inicializeComponents(View view) {
        TxtPresentacion = view.findViewById(R.id.TxtPresentacion);
        TxtError = view.findViewById(R.id.TxtError);
        ImgTelcel = view.findViewById(R.id.ImgTelcel);
        BtnSalir = view.findViewById(R.id.BtnSalir);
        BtnSend = view.findViewById(R.id.BtnSend);
        TxtMensajeEnvio = view.findViewById(R.id.TxtMensajeEnvio);
    }
}
