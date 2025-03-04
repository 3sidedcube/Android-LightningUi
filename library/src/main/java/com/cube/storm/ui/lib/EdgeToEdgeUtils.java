package com.cube.storm.ui.lib;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EdgeToEdgeUtils {
    public static void addAllPaddings(View view)
    {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener()
        {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat windowInsets)
            {
                Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets systemBarAndCutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                v.setPadding(
                        systemBarAndCutoutInsets.left,
                        systemBarInsets.top,
                        systemBarAndCutoutInsets.right,
                        systemBarInsets.bottom
                );
                return WindowInsetsCompat.CONSUMED;
            }
        });
    }
}
