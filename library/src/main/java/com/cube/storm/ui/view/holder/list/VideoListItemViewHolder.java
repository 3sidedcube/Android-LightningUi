package com.cube.storm.ui.view.holder.list;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.activity.VideoPlayerActivity;
import com.cube.storm.ui.lib.EventHook;
import com.cube.storm.ui.model.descriptor.VideoPageDescriptor;
import com.cube.storm.ui.model.list.VideoListItem;
import com.cube.storm.ui.model.property.VideoProperty;
import com.cube.storm.ui.view.ImageView;
import com.cube.storm.ui.view.Populator;
import com.cube.storm.ui.view.holder.ViewHolder;
import com.cube.storm.ui.view.holder.ViewHolderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * View holder for {@link com.cube.storm.ui.model.list.VideoListItem} in the adapter
 *
 * @author Alan Le Fournis
 * @Project LightningUi
 */
public class VideoListItemViewHolder extends ViewHolder<VideoListItem>
{
	public static class Factory extends ViewHolderFactory
	{
		@Override public VideoListItemViewHolder createViewHolder(ViewGroup parent)
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item_view, parent, false);
			return new VideoListItemViewHolder(view);
		}
	}

	protected ImageView image;
	protected TextView durationTime;
	protected ProgressBar progress;
	protected LinearLayout embeddedLinksContainer;
	protected VideoListItem model;

	public VideoListItemViewHolder(View view)
	{
		super(view);

		image = (ImageView)view.findViewById(R.id.image);
		durationTime = (TextView)view.findViewById(R.id.duration_time);
		progress = (ProgressBar)view.findViewById(R.id.progress);
		embeddedLinksContainer = (LinearLayout)view.findViewById(R.id.embedded_links_container);
	}

	@Override public void populateView(final VideoListItem model)
	{
		this.model = model;
		image.populate(model.getImage(), null, progress);

		if (model.getDuration() > 0)
		{
			String ms = String.format(Locale.US, "%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(model.getDuration()) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(model.getDuration()) % TimeUnit.MINUTES.toSeconds(1));
			durationTime.setText(ms);
			durationTime.setVisibility(View.VISIBLE);
		}

		Populator.populate(embeddedLinksContainer, model.getEmbeddedLinks());

		itemView.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onItemClick(v);
			}
		});
	}
	
	/**
	 * Method to call when the view is clicked
	 * @param view The clicked view
	 */
	protected void onItemClick(View view)
	{
		try
		{
			List<VideoProperty> videoList = new ArrayList<>(model.getVideos());
			
			if (videoList.isEmpty())
			{
				onVideoNotAvailable();
				return;
			}
			
			String defaultLanguageUri = UiSettings.getInstance().getDefaultLanguageUri();
			VideoProperty videoToShow = videoList.get(0);
			if (defaultLanguageUri != null)
			{
				// TODO: Some of this locale logic is duplicated with the language library - move to utils
				for (VideoProperty videoProperty : model.getVideos())
				{
					if (defaultLanguageUri.contains(videoProperty.getLocale()))
					{
						videoToShow = videoProperty;
						break;
					}
				}
			}
			
			for (EventHook eventHook : UiSettings.getInstance().getEventHooks())
			{
				eventHook.onViewLinkedClicked(itemView, model, videoToShow.getSrc());
			}
			
			VideoPageDescriptor pageDescriptor = new VideoPageDescriptor();
			pageDescriptor.setType("content");
			pageDescriptor.setSrc(videoToShow.getSrc().getDestination());
			
			Intent video = UiSettings.getInstance().getIntentFactory().getIntentForPageDescriptor(view.getContext(), pageDescriptor);
			
			if (video != null)
			{
				if (video.getComponent() != null && video.getComponent().getClassName().equals(VideoPlayerActivity.class.getName()))
				{
					video.putExtra(VideoPlayerActivity.EXTRA_VIDEO, videoToShow);
				}
				view.getContext().startActivity(video);
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "Error: " + e.getMessage());
			onVideoLoadFailed(e);
		}
	}
	
	/**
	 * Displays to the user that a video is not available
	 */
	protected void onVideoNotAvailable()
	{
		Toast.makeText(itemView.getContext(), "Video is not available", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Displays to the user that a video could not be loaded
	 */
	protected void onVideoLoadFailed(@SuppressWarnings("unused") Exception e)
	{
		Toast.makeText(itemView.getContext(), "Could not load video", Toast.LENGTH_LONG).show();
	}
}
