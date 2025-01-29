package com.itsmarts.SmartRouteTruckApp.clases;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Configuration;

import com.airbnb.lottie.LottieAnimationView;

public class AnimatorNew {
    public void beginAnimation(final LottieAnimationView imageView, final int lightModeAnimation, final int darkModeAnimation) {
        int currentNightMode = imageView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Modo oscuro
            imageView.setAnimation(darkModeAnimation);
        } else {
            // Modo claro
            imageView.setAnimation(lightModeAnimation);
        }

        imageView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Aquí detectamos el final de la animación
                super.onAnimationEnd(animation);
                // Requerimos que se repita la animación una vez más
                imageView.setRepeatCount(1);
                imageView.playAnimation();
            }
        });
        imageView.playAnimation();
    }
}
