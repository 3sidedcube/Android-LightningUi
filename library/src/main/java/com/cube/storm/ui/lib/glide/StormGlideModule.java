package com.cube.storm.ui.lib.glide;

import android.content.Context;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.InputStream;

@GlideModule
public class StormGlideModule extends LibraryGlideModule
{
	@Override
	public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry)
	{
		registry.prepend(String.class, InputStream.class, new StormGlideModelLoader.Factory());
	}
}
