package com.cube.storm.ui.lib.helper;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cube.storm.UiSettings;
import com.cube.storm.ui.data.ContentSize;
import com.cube.storm.ui.lib.listener.ImageLoadingListener;
import com.cube.storm.ui.model.property.ImageProperty;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 * @project Storm
 */
public class ImageHelper
{
	public static void displayImage(@NonNull final ImageView image, @Nullable List<ImageProperty> images)
	{
		displayImage(image, images, new ImageLoadingListener()
		{
			@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				if (loadedImage != null)
				{
					image.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public static void displayImage(@NonNull final ImageView image, @Nullable List<ImageProperty> images, @Nullable final ImageLoadingListener listener)
	{
		if (images == null)
		{
			return;
		}

		int width = image.getWidth();
		int height = image.getHeight();
		final String src = ImageHelper.getImageSrc(images, width, height);

		if (src == null)
		{
			if (listener != null)
			{
				listener.onLoadingFailed(null, image, null);
			}
			return;
		}

		Glide.with(image.getContext())
			.asBitmap()
			.load(src)
			.listener(new RequestListener<Bitmap>()
			{
				@Override
				public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
				{
					if (listener != null)
					{
						listener.onLoadingFailed(src, image, e);
					}
					return false;
				}

				@Override
				public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
				{
					if (listener != null)
					{
						listener.onLoadingComplete(src, image, resource);
					}
					return false;
				}
			})
			.into(image);
	}

	@Nullable
	public static String getImageSrc(@Nullable List<? extends ImageProperty> images)
	{
		return getImageSrc(images, 0, 0);
	}

	@Nullable
	public static String getImageSrc(@Nullable List<? extends ImageProperty> images, int width, int height)
	{
		ImageProperty imageProperty = getImageProperty(images, width, height);

		if (imageProperty != null)
		{
			return imageProperty.getSrc().getDestination();
		}

		return null;
	}

	@Nullable
	public static ImageProperty getImageProperty(@Nullable List<? extends ImageProperty> images)
	{
		return getImageProperty(images, 0, 0);
	}

	@Nullable
	public static ImageProperty getImageProperty(@Nullable List<? extends ImageProperty> images, int width, int height)
	{
		if (images == null || images.size() == 0)
		{
			return null;
		}

		Collections.sort(images, new ImagePropertyComparator());

		if ((width == 0 && height == 0) || UiSettings.getInstance().getContentSize() != ContentSize.AUTO)
		{
			if (UiSettings.getInstance().getContentSize() == ContentSize.SMALL)
			{
				return images.get(0);
			}
			else if (UiSettings.getInstance().getContentSize() == ContentSize.LARGE)
			{
				return images.get(Math.max(images.size() - 2, 0));
			}
			else if (UiSettings.getInstance().getContentSize() == ContentSize.XLARGE)
			{
				return images.get(images.size() - 1);
			}

			return images.get((int)Math.min(Math.ceil((double)images.size() / 2d), images.size() - 1));
		}
		else
		{
			int closest = -1;
			for (int index = 0, count = images.size(); index < count; index++)
			{
				int imageWidth = images.get(index).getDimensions().getWidth();
				int imageHeight = images.get(index).getDimensions().getHeight();

				if ((width == 0 || width >= imageWidth) && (height == 0 || height >= imageHeight))
				{
					closest = index;
				}
			}

			if (closest == -1)
			{
				// return image based on content size instead if an image couldnt be matched
				return getImageProperty(images, 0, 0);
			}

			return images.get(closest);
		}
	}

	private static class ImagePropertyComparator implements Comparator<ImageProperty>
	{
		@Override public int compare(ImageProperty lhs, ImageProperty rhs)
		{
			long totalArea = lhs.getDimensions().getHeight() * lhs.getDimensions().getWidth();
			return Long.valueOf(totalArea).compareTo((long)(rhs.getDimensions().getHeight() * rhs.getDimensions().getWidth()));
		}
	}
}
