package com.cube.storm.ui.lib.factory;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cube.storm.UiSettings;
import com.cube.storm.util.lib.resolver.Resolver;

import java.io.InputStream;

/**
 * Factory class used to resolve a file based on it's Uri
 *
 * @author Callum Taylor
 * @project LightningUi
 */
public abstract class FileFactory
{
	/**
	 * Loads a file from disk based on its Uri location
	 *
	 * @param fileUri The file Uri to resolve
	 *
	 * @return The file byte array, nor null
	 */
	@Nullable
	public InputStream loadFromUri(@NonNull Uri fileUri)
	{
		Resolver resolver = UiSettings.getInstance().getUriResolvers().get(fileUri.getScheme());

		if (resolver != null)
		{
			return resolver.resolveFile(fileUri);
		}

		return null;
	}
}
