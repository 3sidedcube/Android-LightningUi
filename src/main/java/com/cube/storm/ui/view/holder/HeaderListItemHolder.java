package com.cube.storm.ui.view.holder;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.model.list.HeaderListItem;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * // TODO: Add class description
 *
 * @author Alan Le Fournis
 * @project Storm
 */
public class HeaderListItemHolder extends Holder<HeaderListItem>
{
	protected ImageView image;
	protected TextView title;
	protected TextView description;

	@Override public View createView(ViewGroup parent)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_list_item_view, parent, false);
		image = (ImageView)view.findViewById(R.id.image_view);
		title = (TextView)view.findViewById(R.id.title);
		description = (TextView)view.findViewById(R.id.description);

		return view;
	}

	@Override public void populateView(final HeaderListItem model)
	{
		image.setImageBitmap(null);
		title.setVisibility(View.GONE);
		description.setVisibility(View.GONE);

		if (model.getImage() != null)
		{
			UiSettings.getInstance().getImageLoader().displayImage(model.getImage().getSrc(), image, new SimpleImageLoadingListener()
			{
				@Override public void onLoadingStarted(String imageUri, View view)
				{
					image.setVisibility(View.INVISIBLE);
				}

				@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason)
				{
					if (!imageUri.equalsIgnoreCase(model.getImage().getFallbackSrc()))
					{
						UiSettings.getInstance().getImageLoader().displayImage(model.getImage().getFallbackSrc(), image, this);
					}

					image.setVisibility(View.VISIBLE);

				}

				@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					image.setVisibility(View.VISIBLE);
				}
			});
		}

		if (model.getTitle() != null)
		{
			title.setText(model.getTitle().getContent());
			title.setVisibility(View.VISIBLE);
		}

		if (model.getDescription() != null)
		{
			description.setText(model.getDescription().getContent());
			description.setVisibility(View.VISIBLE);
		}
	}
}