package com.protocapture.project;

import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.google.ar.sceneform.ux.ArFragment;


//https://github.com/google-ar/sceneform-android-sdk/issues/573
public class MyArFragment extends ArFragment {
    @Override
    protected void onWindowFocusChanged(boolean hasFocus) {
        FragmentActivity activity = getActivity();
        if (hasFocus && activity != null) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }
}
