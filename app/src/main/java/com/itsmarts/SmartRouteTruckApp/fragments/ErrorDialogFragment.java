package com.itsmarts.SmartRouteTruckApp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sin Internet")
                .setMessage("Error de conexión. Por favor, verifica tu conexión a internet y vuelve a intentarlo.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle positive button click (e.g., dismiss the dialog)
                    dialog.dismiss();
                });
        return builder.create();
    }
}
