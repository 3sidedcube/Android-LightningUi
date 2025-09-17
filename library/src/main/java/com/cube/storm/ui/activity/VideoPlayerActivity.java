package com.cube.storm.ui.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.cube.storm.UiSettings;
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

import java.util.Arrays;
import java.util.List;

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

	/**
	 * Priority list of Youtube itag formats to attempt to retrieve
	 * <p>
	 * See http://en.wikipedia.org/wiki/YouTube#Quality_and_formats
	 * <p>
	 * TODO: In future can add DASH media sources
	 */
	private static final List<Integer> YOUTUBE_ITAG_PREFERENCE = Arrays.asList(22, 18, 43, 5, 36, 17);

	// Saved instance state keys.
	private static final String KEY_WINDOW = "window";
	private static final String KEY_POSITION = "position";
	private static final String KEY_AUTO_PLAY = "auto_play";
	private static final String KEY_URI = "playing_uri";
	private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";

	private PlayerView playerView;
	private ProgressBar progressBar;

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
		progressBar = findViewById(R.id.progress);
		playerView.requestFocus();

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
		if (player == null)
		{
			player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
			player.setPlayWhenReady(startAutoPlay);
			player.setRepeatMode(REPEAT_MODE_ONE);
			playerView.setPlayer(player);
			playerView.setUseController(true);
			playerView.setPlaybackPreparer(this);

			boolean isResolved = false;
			boolean isMediaSourceReady = false;

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
							isMediaSourceReady = false;
							try
							{
								Class.forName("at.huber.youtubeExtractor.YouTubeExtractor");
								extractRawYoutubeUri();
							}
							catch (ClassNotFoundException ex)
							{
								Log.w("3SC", "Cannot play " + uri + ". Ensure the Storm app either has an API key or a dependency on the youtube extractor");
								Toast.makeText(this, "Cannot play YouTube video", Toast.LENGTH_LONG).show();
							}
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

	@SuppressLint("StaticFieldLeak")
	private void extractRawYoutubeUri()
	{
		new at.huber.youtubeExtractor.YouTubeExtractor(this)
		{
			@Override
			public void onExtractionComplete(
				SparseArray<at.huber.youtubeExtractor.YtFile> ytFiles,
				at.huber.youtubeExtractor.VideoMeta vMeta
			)
			{
				if (ytFiles != null)
				{
					for (Integer itag : YOUTUBE_ITAG_PREFERENCE)
					{
						at.huber.youtubeExtractor.YtFile file = ytFiles.get(itag);
						if (file != null)
						{
							VideoPlayerActivity.this.uri = Uri.parse(ytFiles.get(itag).getUrl());
							break;
						}
					}
					initialiseMediaSource();
				}
			}
		}.extract(uri.toString(), true, true);
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
