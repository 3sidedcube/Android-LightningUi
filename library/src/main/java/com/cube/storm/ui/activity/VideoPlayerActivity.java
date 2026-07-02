package com.cube.storm.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.cube.storm.UiSettings;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.cube.storm.ui.R;
import com.cube.storm.ui.lib.EdgeToEdgeUtils;
import com.cube.storm.ui.lib.handler.LinkHandler;
import com.cube.storm.ui.model.property.VideoProperty;
import com.cube.storm.util.lib.resolver.Resolver;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;

/**
 * Video player used to play videos from assets/file/http URI streams.
 *
 * @author Alan Le Fournis
 * @project LightningUi
 */
public class VideoPlayerActivity extends AppCompatActivity implements PlaybackPreparer, View.OnClickListener
{
	public static final String EXTRA_VIDEO = "extra_video";

	// Saved instance state keys.
	private static final String KEY_WINDOW = "window";
	private static final String KEY_POSITION = "position";
	private static final String KEY_AUTO_PLAY = "auto_play";
	private static final String KEY_URI = "playing_uri";
	private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";

	private PlayerView playerView;
	private YouTubePlayerView youTubePlayerView;
	private ProgressBar progressBar;

	/**
	 * Set once the video has been handed to the YouTube IFrame player so the ExoPlayer
	 * initialisation in {@link #initializePlayer()} is skipped on subsequent lifecycle callbacks.
	 */
	private boolean youTubePlayerActive;

	private DataSource.Factory dataSourceFactory;
	private SimpleExoPlayer player;
	private MediaSource mediaSource;
	private DefaultTrackSelector trackSelector;

	private Uri uri;
	private boolean startAutoPlay;
	private int startWindow;
	private long startPosition;
	private ImageButton closedCaptionsButton;
	private ImageView closeVideoButton;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "storm-video-player"));
		setContentView(R.layout.activity_video_player);

		EdgeToEdgeUtils.addAllPaddings(findViewById(R.id.root));

		closedCaptionsButton = findViewById(R.id.cc_button);
		closeVideoButton = findViewById(R.id.close_button);
		closedCaptionsButton.setOnClickListener(this);
		closeVideoButton.setOnClickListener(this);
		playerView = findViewById(R.id.player_view);
		youTubePlayerView = findViewById(R.id.youtube_player_view);
		progressBar = findViewById(R.id.progress);
		playerView.requestFocus();

		// The YouTube player observes the activity lifecycle so it pauses/releases the
		// underlying WebView player automatically.
		getLifecycle().addObserver(youTubePlayerView);

		// ARCFA-239 Don't hide video controls when screen reader is on
		AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
		if (am != null && am.isEnabled())
		{
			playerView.setControllerAutoShow(false);
			playerView.setControllerShowTimeoutMs(Integer.MAX_VALUE);
			playerView.showController();
		}

		if (uri == null
		    && getIntent().hasExtra(EXTRA_VIDEO)
		    && getIntent().getExtras().getSerializable(EXTRA_VIDEO) != null)
		{
			VideoProperty videoProperty = (VideoProperty) getIntent().getSerializableExtra(EXTRA_VIDEO);
			uri = Uri.parse(videoProperty.getSrc().getDestination());
		}

		trackSelector = new DefaultTrackSelector();

		if (savedInstanceState != null)
		{
			startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
			startWindow = savedInstanceState.getInt(KEY_WINDOW);
			startPosition = savedInstanceState.getLong(KEY_POSITION);
			String uriString = savedInstanceState.getString(KEY_URI);
			uri = uriString != null ? Uri.parse(uriString) : null;

			DefaultTrackSelector.Parameters initialTrackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
			if (initialTrackSelectorParameters != null)
			{
				trackSelector.setParameters(initialTrackSelectorParameters);
			}
		}
		else
		{

			trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder().build());
			clearStartPosition();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (Util.SDK_INT > 23)
		{
			initializePlayer();
			if (playerView != null)
			{
				playerView.onResume();
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (Util.SDK_INT <= 23 || player == null)
		{
			initializePlayer();
			if (playerView != null)
			{
				playerView.onResume();
			}
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (Util.SDK_INT <= 23)
		{
			if (playerView != null)
			{
				playerView.onPause();
			}
			releasePlayer();
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (Util.SDK_INT > 23)
		{
			if (playerView != null)
			{
				playerView.onPause();
			}
			releasePlayer();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		updateStartPosition();
		outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
		outState.putInt(KEY_WINDOW, startWindow);
		outState.putLong(KEY_POSITION, startPosition);
		outState.putString(KEY_URI, uri != null ? uri.toString() : "");
		outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelector.getParameters());
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// See whether the player view wants to handle media or DPAD keys events.
		return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
	}

	@Override
	public void preparePlayback()
	{
		player.retry();
	}

	private void initializePlayer()
	{
		// YouTube playback is delegated to the YouTubePlayerView, which manages its own
		// lifecycle via the observer registered in onCreate, so there is nothing more to do.
		if (youTubePlayerActive)
		{
			return;
		}

		if (player == null)
		{
			boolean isResolved = false;
			boolean isMediaSourceReady = false;
			boolean isYoutube = false;

			// Recursively attempt to resolve a uri
			// This recursion is to support Storm uri resolvers - usually we will only iterate once
			while (uri != null && uri.getScheme() != null && !isResolved)
			{
				switch (uri.getScheme())
				{
					case "assets":
					{
						uri = Uri.parse(uri.toString().replace("assets://", "asset:///"));
						isResolved = true;
						isMediaSourceReady = true;
						break;
					}
					case "file":
					{
						isResolved = true;
						isMediaSourceReady = true;
						break;
					}
					case "http":
					case "https":
					{
						isResolved = true;
						if (LinkHandler.isYoutubeVideo(uri))
						{
							isYoutube = true;
						}
						else
						{
							isMediaSourceReady = true;
						}
						break;
					}
					default:
					{
						Resolver resolver = UiSettings.getInstance().getUriResolvers().get(uri.getScheme());
						if (resolver != null)
						{
							String scheme = uri.getScheme();
							uri = resolver.resolveUri(uri);
							if (uri != null && scheme.equals(uri.getScheme()))
							{
								// avoid infinite recursion
								isResolved = true;
								isMediaSourceReady = true;
							}
						}
						else
						{
							// We're not sure what the uri is but may as well try it anyway
							isResolved = true;
							isMediaSourceReady = true;
						}
						break;
					}
				}
			}

			// YouTube videos are played through the official IFrame player rather than ExoPlayer
			if (isYoutube)
			{
				playYoutubeVideo(uri);
				return;
			}

			player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
			player.setPlayWhenReady(startAutoPlay);
			player.setRepeatMode(REPEAT_MODE_ONE);
			playerView.setPlayer(player);
			playerView.setUseController(true);
			playerView.setPlaybackPreparer(this);

			// When the video starts playing get whether or not it has a caption index
			player.addVideoListener(new VideoListener()
			{
				@Override
				public void onRenderedFirstFrame()
				{
					closedCaptionsButton.setVisibility(getCaptionRendererIndex() != null ? View.VISIBLE : View.GONE);
				}
			});

			if (isMediaSourceReady)
			{
				initialiseMediaSource();
			}
		}
	}

	/**
	 * Plays a YouTube video using the official IFrame player API (wrapped by
	 * {@link YouTubePlayerView}). This replaces the previous approach of scraping the raw
	 * stream url, which relied on an unmaintained library with a hardcoded user agent.
	 *
	 * @param uri The YouTube watch uri to play
	 */
	private void playYoutubeVideo(Uri uri)
	{
		final String videoId = extractYoutubeVideoId(uri);
		if (videoId == null)
		{
			Toast.makeText(this, "Cannot play YouTube video", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		youTubePlayerActive = true;

		// Swap the ExoPlayer surface (and its controls) for the YouTube IFrame player
		playerView.setVisibility(View.GONE);
		closedCaptionsButton.setVisibility(View.GONE);
		youTubePlayerView.setVisibility(View.VISIBLE);

		final boolean autoPlay = startAutoPlay;
		final float startSeconds = startPosition > 0 ? startPosition / 1000f : 0f;

		// Disable the native IFrame chrome so we can present a stripped-back set of controls
		IFramePlayerOptions options = new IFramePlayerOptions.Builder(this)
			.controls(0) // web UI is not visible
			.ccLoadPolicy(0) //show captions
			.ivLoadPolicy(3) //won't show annotations.
			.rel(0) //related videos
			.build();

		youTubePlayerView.initialize(new AbstractYouTubePlayerListener()
		{
			@Override
			public void onReady(@NonNull YouTubePlayer youTubePlayer)
			{
				progressBar.setVisibility(View.GONE);

				// Replace the default player UI with a minimal one - no video title,
				// no YouTube logo, and no share/overflow menu.
				DefaultPlayerUiController uiController = new DefaultPlayerUiController(youTubePlayerView, youTubePlayer);
				uiController.showVideoTitle(false);
				uiController.showYouTubeButton(false);
				uiController.showMenuButton(false);
				uiController.showFullscreenButton(false);
				youTubePlayerView.setCustomPlayerUi(uiController.getRootView());

				if (autoPlay)
				{
					youTubePlayer.loadVideo(videoId, startSeconds);
				}
				else
				{
					youTubePlayer.cueVideo(videoId, startSeconds);
				}
			}

			@Override
			public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state)
			{
				// Loop the video (mirrors the ExoPlayer REPEAT_MODE_ONE behaviour). This also
				// prevents YouTube's end-screen - with its share button and related videos -
				// from ever being shown, which the IFrame API cannot otherwise suppress.
				if (state == PlayerConstants.PlayerState.ENDED)
				{
					youTubePlayer.seekTo(0f);
					youTubePlayer.play();
				}
			}
		}, options);
	}

	/**
	 * Extracts the YouTube video id from a watch uri. Mirrors the matching performed by
	 * {@link LinkHandler#isYoutubeVideo(Uri)}.
	 *
	 * @param uri The YouTube uri
	 *
	 * @return The video id, or null if one could not be determined
	 */
	@Nullable
	private static String extractYoutubeVideoId(@Nullable Uri uri)
	{
		if (uri == null || uri.getHost() == null)
		{
			return null;
		}

		if (uri.getHost().endsWith("youtu.be"))
		{
			return uri.getPathSegments().isEmpty() ? null : uri.getPathSegments().get(0);
		}

		if (uri.getHost().endsWith("youtube.com"))
		{
			return uri.getQueryParameter("v");
		}

		return null;
	}

	private void initialiseMediaSource()
	{
		progressBar.setVisibility(View.GONE);

		if (uri != null)
		{
			mediaSource = buildMediaSource(uri);
		}
		else
		{
			Toast.makeText(this, "Could not load video.", Toast.LENGTH_LONG).show();
			finish();
		}

		boolean haveStartPosition = startWindow != C.INDEX_UNSET;
		if (haveStartPosition)
		{
			player.seekTo(startWindow, startPosition);
		}

		player.prepare(mediaSource, !haveStartPosition, false);

		player.addVideoListener(new VideoListener()
		{
			@Override
			public int hashCode()
			{
				return super.hashCode();
			}
		});
	}

	private MediaSource buildMediaSource(Uri uri)
	{
		@C.ContentType int type = Util.inferContentType(uri);
		switch (type)
		{
			case C.TYPE_DASH:
				return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			case C.TYPE_SS:
				return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			case C.TYPE_HLS:
				return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
			default:
			{
				throw new IllegalStateException("Unsupported type: " + type);
			}
		}
	}

	private void releasePlayer()
	{
		if (player != null)
		{
			updateStartPosition();
			player.release();
			player = null;
			mediaSource = null;
		}
	}

	private void updateStartPosition()
	{
		if (player != null)
		{
			startAutoPlay = player.getPlayWhenReady();
			startWindow = player.getCurrentWindowIndex();
			startPosition = Math.max(0, player.getContentPosition());
		}
	}

	private void clearStartPosition()
	{
		startAutoPlay = true;
		startWindow = C.INDEX_UNSET;
		startPosition = C.TIME_UNSET;
	}

	@Override
	public void onClick(View view)
	{
		if (view == closedCaptionsButton)
		{
			Integer captionsRendererIdx = getCaptionRendererIndex();

			if (captionsRendererIdx == null)
			{
				return;
			}

			showTrackSelectorDialog(captionsRendererIdx);
		}
		else if (view == closeVideoButton)
		{
			finish();
		}
	}

	/**
	 * Determines whether the currently playing media has a caption renderer and returns its index
	 * in the MappedTrackInfo object if it does
	 *
	 * @return
	 *  A valid index into the current mapped track info, or null
	 */
	@Nullable
	private Integer getCaptionRendererIndex()
	{
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

		if (mappedTrackInfo == null)
		{
			return null;
		}

		for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++)
		{
			int trackType = mappedTrackInfo.getRendererType(rendererIndex);
			TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex);
			boolean isCaptionRenderer = trackType == C.TRACK_TYPE_TEXT;
			if (isCaptionRenderer && trackGroupArray.length > 0)
			{
				return rendererIndex;
			}
		}

		return null;
	}

	private void showTrackSelectorDialog(int rendererIndex)
	{
		MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

		if (mappedTrackInfo == null)
		{
			return;
		}

		new TrackSelectionDialogBuilder(this, "", trackSelector, rendererIndex)
			.setShowDisableOption(true)
			.setAllowAdaptiveSelections(false)
			.build()
			.show();
	}
}
