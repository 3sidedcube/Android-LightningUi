package com.cube.storm.ui.lib.resolver;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cube.storm.util.lib.resolver.Resolver;

import java.io.InputStream;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 * @project LightningUi
 */
public class AppResolver extends Resolver
{
	protected Context applicationContext;

	public AppResolver(Context context)
	{
		this.applicationContext = context.getApplicationContext();
	}

	@Nullable @Override public Uri resolveUri(@NonNull Uri uri)
	{
		if ("app".equalsIgnoreCase(uri.getScheme()) && "native".equalsIgnoreCase(uri.getHost()))
		{
			String[] uriParts = uri.toString().substring("app://native/".length()).split("/");

			String fileName = uriParts[uriParts.length - 1];
			int pos = fileName.lastIndexOf(".");

			if (pos > -1)
			{
				fileName = fileName.substring(0, pos);
			}

			fileName = fileName.toLowerCase().replaceAll("[^a-z_]", "");
			int res = applicationContext.getResources().getIdentifier(fileName, "drawable", applicationContext.getPackageName());

			if (res > 0)
			{
				return Uri.parse("drawable://" + res);
			}
		}

		return null;
	}

	@Nullable @Override public InputStream resolveFile(@NonNull Uri uri)
	{
		if ("app".equalsIgnoreCase(uri.getScheme()))
		{
			uri = resolveUri(uri);
		}

		if (uri != null)
		{
			int id = Integer.parseInt(uri.toString().substring("drawable://".length()));

			if (id > 0)
			{
				return applicationContext.getResources().openRawResource(id);
			}
		}

		return null;
	}
}
