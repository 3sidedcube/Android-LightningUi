package com.cube.storm.ui.lib.listener;

import android.graphics.Bitmap;
import android.view.View;
import androidx.annotation.Nullable;

public interface ImageLoadingListener
{
	default void onLoadingStarted(String imageUri, View view) {}
	default void onLoadingFailed(String imageUri, View view, @Nullable Exception e) {}
	default void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {}
}
