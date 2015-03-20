package com.cube.storm.ui.view.holder.list;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.lib.helper.ImageHelper;
import com.cube.storm.ui.model.list.SpotlightListItem;
import com.cube.storm.ui.view.ViewClickable;
import com.cube.storm.ui.view.holder.ViewHolder;
import com.cube.storm.ui.view.holder.ViewHolderFactory;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Holder for populating the Spotlight image at the top of a list view.
 * The image will cycle behind the text and also update the text fluidly with it.
 *
 * The animation will be triggered by a {@link java.util.Timer} thread that calls back with a
 * {@link java.util.TimerTask} that will use the delay set for the property and then be removed and
 * re-set each time the view is updated, which will allow for inconsistent delay timings.
 *
 * This means that timings are not based on the callback from the ViewPropertyAnimator but also being
 * managed asynchronously.
 *
 * @author Matt Allen
 * @project Storm
 */
public class SpotlightListItemViewHolder extends ViewHolder<SpotlightListItem> implements ViewClickable<SpotlightListItem>
{
	public static class Factory extends ViewHolderFactory
	{
		@Override public SpotlightListItemViewHolder createViewHolder(ViewGroup parent)
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spotlight_image_list_item_view, parent, false);
			return new SpotlightListItemViewHolder(view);
		}
	}

	private static final int MSG_UPDATE = 100;
	private ImageView image;
	private TextView text;

	private Timer timer;
	private Handler handler;
	private SpotlightListItem model;
	private int currentIndex = 0;

	public SpotlightListItemViewHolder(View view)
	{
		super(view);
		image = (ImageView)view.findViewById(R.id.image_view);
		image.setTag(getTimer());
		text = (TextView)view.findViewById(R.id.text_ticker);
	}

	@Override public void populateView(SpotlightListItem model)
	{
		if (this.model == null)
		{
			currentIndex = 0;

			this.model = model;

			handler = new Handler()
			{
				@Override public void handleMessage(Message msg)
				{
					if (msg.what == MSG_UPDATE)
					{
						updateView();
					}
				}
			};

			updateView();
		}
	}

	private void updateView()
	{
		if (model.getSpotlights() != null)
		{
			if (currentIndex >= model.getSpotlights().size())
			{
				currentIndex = 0;
			}

			ImageLoader.getInstance().displayImage(ImageHelper.getImageSrc(model.getSpotlights().get(currentIndex).getImage()), image);

			String content = UiSettings.getInstance().getTextProcessor().process(model.getSpotlights().get(currentIndex).getText().getContent());

			if (!TextUtils.isEmpty(content))
			{
				text.setText(content);
				text.setVisibility(View.VISIBLE);
			}
			else
			{
				text.setVisibility(View.GONE);
			}

			currentIndex++;
			if (currentIndex >= model.getSpotlights().size())
			{
				currentIndex = 0;
			}

			timer.schedule(new TimerTask()
			{
				@Override public void run()
				{
					handler.sendEmptyMessage(MSG_UPDATE);
				}
			}, model.getSpotlights().get(currentIndex).getDelay());
		}
	}

	protected Timer getTimer()
	{
		if (timer == null)
		{
			if (image.getTag() != null && image.getTag() instanceof Timer)
			{
				timer = (Timer)image.getTag();
			}
			else
			{
				timer = new Timer("Spotlight timer");
			}
		}

		return timer;
	}

	@Override public void onClick(@NonNull SpotlightListItem model, @NonNull View view)
	{
		// TODO Redo this with a standard OnClickListener interface
		if (model.getSpotlights() != null && model.getSpotlights().get(currentIndex).getLink() != null)
		{
			UiSettings.getInstance().getLinkHandler().handleLink(view.getContext(), model.getSpotlights().get(currentIndex).getLink());
		}
	}
}
