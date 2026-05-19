package com.cube.storm.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.cube.storm.UiSettings;
import com.cube.storm.ui.data.ContentSize;
import com.cube.storm.ui.lib.helper.ImageHelper;
import com.cube.storm.ui.lib.listener.ImageLoadingListener;
import com.cube.storm.ui.model.property.AnimationFrame;
import com.cube.storm.ui.model.property.AnimationImageProperty;
import com.cube.storm.ui.model.property.ImageProperty;
import com.cube.storm.ui.model.property.SpotlightImageProperty;
import com.cube.storm.ui.model.property.TextProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Augmented image view which uses Glide to display images and handles Storm animations.
 * <p/>
 *
 * @author Tim Matthews
 * @Project LightningUi
 */
public class ImageView extends androidx.appcompat.widget.AppCompatImageView
{
	/**
	 * Listener alerted whenever the animation frame changes.
	 */
	public interface OnAnimationFrameChangeListener
	{
		public void onAnimationFrameChange(ImageView imageView, int frameIndex, AnimationImageProperty frame);
		public void onAnimationFrameChange(ImageView imageView, int frameIndex, SpotlightImageProperty frame);
	}

	/**
	 * Handler used to control animations. Null until an animation is explicitly
	 */
	private Handler animator = null;

	/**
	 * Runnable used to update the current frame. Null until an animation is explicitly
	 */
	private Runnable displayNextFrame = null;

	public ImageView(Context context)
	{
		super(context);
	}

	public ImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ImageView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@Override protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();

		if (animator != null)
		{
			animator.removeCallbacks(displayNextFrame);
		}
	}

	@Override protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();

		if (animator != null)
		{
			animator.post(displayNextFrame);
		}
	}

	/**
	 * Alternately display each of a sequence of Storm animation frames.
	 *
	 * @param frames
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 */
	public void populate(@Nullable final AnimationImageProperty frames)
	{
		populate(frames, null);
	}

	/**
	 * Alternately display each of a sequence of Storm animation frames.
	 *
	 * @param frames
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 */
	public void populate(@Nullable final AnimationImageProperty frames, @Nullable final TextProperty accessibilityLabel)
	{
		populate(frames, accessibilityLabel, null);
	}

	/**
	 * Alternately display each of a sequence of Storm animation frames, alerting an optional listener whenever the
	 * frame changes.
	 *
	 * @param imageProperty If null, the current image is cleared and all pending animation tasks are cancelled.
	 * @param listener
	 */
	public void populate(@Nullable final AnimationImageProperty imageProperty, @Nullable final TextProperty accessibilityLabel, @Nullable final OnAnimationFrameChangeListener listener)
	{
		// Cancel all current loading tasks
		populate((ArrayList<ImageProperty>)null);

		if (imageProperty != null)
		{
			if (animator == null)
			{
				animator = new Handler();
			}

			displayNextFrame = new Runnable()
			{
				private int frameIndex = 0;

				@Override public void run()
				{
					AnimationFrame frame = imageProperty.getFrames().get(frameIndex % imageProperty.getFrames().size());
					populateFrame(frame.getImage(), accessibilityLabel, null, null);

					if (listener != null)
					{
						listener.onAnimationFrameChange(ImageView.this, frameIndex % imageProperty.getFrames().size(), imageProperty);
					}

					++frameIndex;

					if (imageProperty.getFrames().size() > 1)
					{
						animator.postDelayed(this, frame.getDelay());
					}
				}
			};
		}
	}

	/**
	 * Alternately display each of a sequence of Storm spotlight frames, updating a text view to display the spotlight
	 * text whenever it changes.
	 *
	 * @param frames
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 * @param textView
	 * 		{@link TextView#populate(com.cube.storm.ui.model.property.TextProperty, com.cube.storm.ui.model.property.LinkProperty)}
	 * 		will be called with the relevant details each time the spotlight changes.
	 */
	public void populate(@Nullable final List<? extends SpotlightImageProperty> frames, final @NonNull TextView textView)
	{
		populate(frames, new OnAnimationFrameChangeListener()
		{
			@Override public void onAnimationFrameChange(ImageView imageView, int frameIndex, AnimationImageProperty frame)
			{
			}

			@Override public void onAnimationFrameChange(ImageView imageView, int frameIndex, SpotlightImageProperty frame)
			{
				textView.populate(frame.getText(), frame.getLink());
			}
		});
	}

	/**
	 * Alternately display each of a sequence of Storm spotlight frames, updating a text view to display the spotlight
	 * text whenever it changes.
	 *
	 * @param frames
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 * @param listener
	 * 		The listener for frame change
	 */
	public void populate(@Nullable final List<? extends SpotlightImageProperty> frames, @Nullable final OnAnimationFrameChangeListener listener)
	{
		// Cancel all current loading tasks
		populate((ArrayList<ImageProperty>)null);

		if (frames != null)
		{
			if (animator == null)
			{
				animator = new Handler();
			}

			displayNextFrame = new Runnable()
			{
				private int frameIndex = 0;

				@Override public void run()
				{
					SpotlightImageProperty frame = frames.get(frameIndex % frames.size());
					populateFrame(frame.getImage(), frame.getAccessibilityLabel(), null, null);

					if (listener != null)
					{
						listener.onAnimationFrameChange(ImageView.this, frameIndex % frames.size(), frames.get(frameIndex % frames.size()));
					}

					++frameIndex;

					if (frames.size() > 1)
					{
						animator.postDelayed(this, frame.getDelay());
					}
				}
			};
		}
	}

	/**
	 * Load and display the specified image asynchronously.
	 *
	 * @param image
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 */
	public void populate(@Nullable final ArrayList<ImageProperty> image)
	{
		populate(image, null);
	}

	/**
	 * Load and display the specified image asynchronously.
	 *
	 * @param image
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 */
	public void populate(@Nullable final ArrayList<ImageProperty> image, @Nullable final TextProperty accessibilityLabel)
	{
		populate(image, accessibilityLabel, null);
	}

	/**
	 * Load and display the specified image asynchronously, with an optional progress bar visible until the image is
	 * loaded.
	 *
	 * @param image
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 * @param progress
	 */
	public void populate(@Nullable final ArrayList<ImageProperty> image, @Nullable final TextProperty accessibilityLabel, @Nullable final ProgressBar progress)
	{
		populate(image, accessibilityLabel, progress, null);
	}

	/**
	 * Load and display the specified image asynchronously, with an optional progress bar visible until the image is
	 * loaded.
	 *
	 * @param image
	 * 		If null, the current image is cleared and all pending animation tasks are cancelled.
	 * @param progress
	 * @param listener
	 *      If provided then the listener will receive callbacks at each stage of the image frame being loaded
	 */
	public void populate(@Nullable final ArrayList<ImageProperty> image, @Nullable final TextProperty accessibilityLabel, @Nullable final ProgressBar progress, @Nullable final ImageLoadingListener listener)
	{
		// If image size isnt calculated yet, wait till it has
		if (getWidth() == 0 && getHeight() == 0 && image != null && getVisibility() != GONE && UiSettings.getInstance().getContentSize() == ContentSize.AUTO)
		{
			getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
			{
				@Override public boolean onPreDraw()
				{
					getViewTreeObserver().removeOnPreDrawListener(this);

					if (getWidth() == 0 && getHeight() == 0)
					{
						// fuck android
						populateFrame(image, accessibilityLabel, progress, listener);
						return false;
					}

					populate(image, accessibilityLabel, progress, listener);
					return false;
				}
			});

			return;
		}

		// The user is explicitly setting a static image so cancel the animation task
		if (animator != null && displayNextFrame != null)
		{
			animator.removeCallbacks(displayNextFrame);
		}

		populateFrame(image, accessibilityLabel, progress, listener);
	}

	/**
	 * Used internally by all other image population tasks.
	 *
	 * @param image
	 * @param progress
	 */
	private void populateFrame(
		@Nullable final ArrayList<ImageProperty> image,
		@Nullable TextProperty accessibilityLabel,
		@Nullable final ProgressBar progress,
		@Nullable final ImageLoadingListener listener
	)
	{
		Glide.with(this).clear(this);

		if (image != null && image.size() > 0)
		{
			if (accessibilityLabel == null)
			{
				// no explicit accessibility label provided. See if the images themselves have one...
				// we will choose an image to take the label from arbitrarily
				// all the images have the same label anyway for all Storm apps using the LegacyImageViewProcessor
				accessibilityLabel = image.get(0).getAccessibilityLabel();
			}

			// Set accessibility label (content description) on images
			String accessibilityLabelText = UiSettings.getInstance().getTextProcessor().process(accessibilityLabel);
			if (!TextUtils.isEmpty(accessibilityLabelText))
			{
				setContentDescription(accessibilityLabelText);
				setFocusable(true);
			}
			else
			{
				setContentDescription(null);
				setFocusable(false);
			}

			if (listener != null)
			{
				listener.onLoadingStarted(null, this);
			}

			if (animator == null)
			{
				setVisibility(View.INVISIBLE);
			}

			if (progress != null)
			{
				progress.setVisibility(View.VISIBLE);
			}

			ImageHelper.displayImage(this, image, new ImageLoadingListener()
			{
				@Override public void onLoadingFailed(String imageUri, View view, @Nullable Exception e)
				{
					if (listener != null)
					{
						listener.onLoadingFailed(imageUri, view, e);
					}

					setVisibility(View.GONE);
					if (progress != null)
					{
						progress.setVisibility(View.GONE);
					}
				}

				@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					if (listener != null)
					{
						listener.onLoadingComplete(imageUri, view, loadedImage);
					}

					setVisibility(View.VISIBLE);
					if (progress != null)
					{
						progress.setVisibility(View.GONE);
					}
				}
			});
		}
		else
		{
			setImageBitmap(null);
			setVisibility(View.GONE);
			setContentDescription(null);
			setFocusable(false);
		}
	}
}
