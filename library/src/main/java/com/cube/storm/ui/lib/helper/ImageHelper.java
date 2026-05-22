package com.cube.storm.ui.lib.helper;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cube.storm.UiSettings;
import com.cube.storm.ui.data.ContentSize;
import com.cube.storm.ui.model.property.ImageProperty;
import com.cube.storm.util.lib.resolver.Resolver;

import java.util.ArrayList;
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
		displayImage(image, images, null);
	}

	public static void displayImage(@NonNull final ImageView image, @Nullable final List<ImageProperty> images, @Nullable final RequestListener<Drawable> listener)
	{
		if (images != null && !images.isEmpty())
		{
			if (image.getVisibility() == View.GONE)
			{
				image.setVisibility(View.INVISIBLE);
			}

			// If image size isnt calculated yet, wait till it has
			if (image.getWidth() == 0 && image.getHeight() == 0 && image.getVisibility() != View.GONE && UiSettings.getInstance().getContentSize() == ContentSize.AUTO)
			{
				image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
				{
					@Override public boolean onPreDraw()
					{
						image.getViewTreeObserver().removeOnPreDrawListener(this);

						displayImageInternal(image, images, listener);
						return true;
					}
				});

				return;
			}

			displayImageInternal(image, images, listener);
		}
		else
		{
			Glide.with(image.getContext()).clear(image);
			image.setVisibility(View.GONE);
		}
	}

	private static void displayImageInternal(@NonNull final ImageView image, @Nullable List<ImageProperty> images, @Nullable final RequestListener<Drawable> listener)
	{
		String src = ImageHelper.getImageSrc(images, image.getWidth(), image.getHeight());
		if (!TextUtils.isEmpty(src))
		{
			String resolvedSrc = resolveUri(src);
			Glide.with(image.getContext())
			     .load(resolvedSrc)
			     .listener(new RequestListener<Drawable>()
			     {
				     @Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource)
				     {
					     if (listener != null)
					     {
						     listener.onLoadFailed(e, model, target, isFirstResource);
					     }
					     return false;
				     }

				     @Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
				     {
					     image.setVisibility(View.VISIBLE);
					     if (listener != null)
					     {
						     listener.onResourceReady(resource, model, target, dataSource, isFirstResource);
					     }
					     return false;
				     }
			     })
			     .into(image);
		}
	}

	public static String resolveUri(String uri)
	{
		if (TextUtils.isEmpty(uri)) return uri;

		Uri parsedUri = Uri.parse(uri);
		String scheme = parsedUri.getScheme();

		if (!TextUtils.isEmpty(scheme))
		{
			if (scheme.equalsIgnoreCase("assets"))
			{
				return resolveUri("file:///android_asset/" + uri.substring("assets://".length()));
			}

			Resolver resolver = UiSettings.getInstance().getUriResolvers().get(scheme.toLowerCase());
			if (resolver != null)
			{
				Uri resolvedUri = resolver.resolveUri(uri);
				if (resolvedUri != null)
				{
					String resolvedUriString = resolvedUri.toString();
					if (!resolvedUriString.equals(uri))
					{
						return resolveUri(resolvedUriString);
					}
				}
			}
		}

		return uri;
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

		if (imageProperty != null && imageProperty.getSrc() != null)
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

		List<? extends ImageProperty> sortedImages = new ArrayList<>(images);
		Collections.sort(sortedImages, new ImagePropertyComparator());

		if ((width == 0 && height == 0) || UiSettings.getInstance().getContentSize() != ContentSize.AUTO)
		{
			if (UiSettings.getInstance().getContentSize() == ContentSize.SMALL)
			{
				return sortedImages.get(0);
			}
			else if (UiSettings.getInstance().getContentSize() == ContentSize.LARGE)
			{
				return sortedImages.get(Math.max(sortedImages.size() - 2, 0));
			}
			else if (UiSettings.getInstance().getContentSize() == ContentSize.XLARGE)
			{
				return sortedImages.get(sortedImages.size() - 1);
			}

			return sortedImages.get((int)Math.min(Math.ceil((double)sortedImages.size() / 2d), sortedImages.size() - 1));
		}
		else
		{
			int closestIdx = -1;
			for (int index = 0, count = sortedImages.size(); index < count; index++)
			{
				ImageProperty.Dimensions dims = sortedImages.get(index).getDimensions();
				if (dims == null) continue;

				int imageWidth = dims.getWidth();
				int imageHeight = dims.getHeight();

				if ((width == 0 || width >= imageWidth) && (height == 0 || height >= imageHeight))
				{
					closestIdx = index;
				}
			}

			if (closestIdx == -1)
			{
				// return image based on content size instead if an image couldnt be matched
				return getImageProperty(sortedImages, 0, 0);
			}

			return sortedImages.get(closestIdx);
		}
	}

	private static class ImagePropertyComparator implements Comparator<ImageProperty>
	{
		@Override public int compare(ImageProperty lhs, ImageProperty rhs)
		{
			long lhsArea = 0;
			long rhsArea = 0;

			if (lhs.getDimensions() != null)
			{
				lhsArea = (long)lhs.getDimensions().getHeight() * lhs.getDimensions().getWidth();
			}

			if (rhs.getDimensions() != null)
			{
				rhsArea = (long)rhs.getDimensions().getHeight() * rhs.getDimensions().getWidth();
			}

			return Long.compare(lhsArea, rhsArea);
		}
	}
}
