package com.cube.storm.ui.lib.glide;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.cube.storm.UiSettings;
import com.cube.storm.util.lib.resolver.Resolver;

import java.io.InputStream;

/**
 * Glide ModelLoader that delegates URI schemes registered in {@link UiSettings} (e.g. app://, assets://)
 * to their corresponding {@link Resolver} implementations.
 */
public class StormGlideModelLoader implements ModelLoader<String, InputStream>
{
	@Nullable
	@Override
	public LoadData<InputStream> buildLoadData(@NonNull String model, int width, int height, @NonNull Options options)
	{
		Uri uri = Uri.parse(model);
		String scheme = uri.getScheme();
		if (scheme != null && isStormScheme(scheme))
		{
			return new LoadData<>(new ObjectKey(model), new StormDataFetcher(uri));
		}
		return null;
	}

	@Override
	public boolean handles(@NonNull String model)
	{
		try
		{
			Uri uri = Uri.parse(model);
			String scheme = uri.getScheme();
			return scheme != null && isStormScheme(scheme);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private static boolean isStormScheme(String scheme)
	{
		try
		{
			return UiSettings.getInstance().getUriResolvers().containsKey(scheme);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private static class StormDataFetcher implements DataFetcher<InputStream>
	{
		private final Uri uri;
		private InputStream stream;

		StormDataFetcher(Uri uri)
		{
			this.uri = uri;
		}

		@Override
		public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback)
		{
			try
			{
				String scheme = uri.getScheme();
				Resolver resolver = UiSettings.getInstance().getUriResolvers().get(scheme);
				if (resolver != null)
				{
					stream = resolver.resolveFile(uri);
					if (stream != null)
					{
						callback.onDataReady(stream);
					}
					else
					{
						callback.onLoadFailed(new Exception("No stream for URI: " + uri));
					}
				}
				else
				{
					callback.onLoadFailed(new Exception("No resolver for scheme: " + scheme));
				}
			}
			catch (Exception e)
			{
				callback.onLoadFailed(e);
			}
		}

		@Override
		public void cleanup()
		{
			try
			{
				if (stream != null)
				{
					stream.close();
				}
			}
			catch (Exception ignored) {}
		}

		@Override
		public void cancel() {}

		@NonNull
		@Override
		public Class<InputStream> getDataClass()
		{
			return InputStream.class;
		}

		@NonNull
		@Override
		public DataSource getDataSource()
		{
			return DataSource.LOCAL;
		}
	}

	public static class Factory implements ModelLoaderFactory<String, InputStream>
	{
		@NonNull
		@Override
		public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory)
		{
			return new StormGlideModelLoader();
		}

		@Override
		public void teardown() {}
	}
}
