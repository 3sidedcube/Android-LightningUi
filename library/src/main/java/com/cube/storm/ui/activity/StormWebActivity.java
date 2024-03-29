package com.cube.storm.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cube.storm.ui.R;

import java.io.File;

/**
 * Web browser to launch website from URI
 * <p/>
 * Can take either a single URI extra using the key {@link StormActivity#EXTRA_URI}
 *
 * @author Alan Le Fournis
 * @project LightningUi
 */
public class StormWebActivity extends AppCompatActivity implements OnClickListener
{
	public static final String EXTRA_FILE_NAME = "extra_file_name";
	public static final String EXTRA_TITLE = "extra_title";

	public View mWeb;
	public View mShare;
	public View mBack;
	public View mForward;
	public View mClose;
	public View mButtonContainer;

	private WebView webView;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		String url = getIntent() != null ? getIntent().getStringExtra(EXTRA_FILE_NAME) : null;

		if (TextUtils.isEmpty(url))
		{
			Toast.makeText(this, "No url set", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (chromeCustomTabsSupported())
		{
			launchChromeCustomTabs(url);
			finish();
			return;
		}

		String title = getIntent() != null ? getIntent().getStringExtra(EXTRA_TITLE) : null;

		if (!TextUtils.isEmpty(title))
		{
			setTitle(title);
		}

		setContentView(R.layout.web_view);

		mButtonContainer = findViewById(R.id.button_container);
		mWeb = findViewById(R.id.icon_web);
		mBack = findViewById(R.id.icon_back);
		mForward = findViewById(R.id.icon_forward);
		mClose = findViewById(R.id.icon_close);
		mShare = findViewById(R.id.icon_share);
		webView = (WebView)findViewById(R.id.web_view);

		mWeb.setOnClickListener(this);
		mBack.setOnClickListener(this);
		mForward.setOnClickListener(this);
		mClose.setOnClickListener(this);
		mShare.setOnClickListener(this);

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setDisplayZoomControls(false);

		final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
		webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient()
		{
			@Override public void onProgressChanged(WebView view, int progress)
			{
				if (progress < 100 && progressBar.getVisibility() == View.GONE)
				{
					progressBar.setVisibility(View.VISIBLE);
				}

				progressBar.setProgress(progress);
				if (progress == 100)
				{
					progressBar.setVisibility(View.GONE);
				}

				super.onProgressChanged(view, progress);
			}
		});

		if (savedInstanceState != null)
		{
			webView.restoreState(savedInstanceState);
		}
		else
		{
			webView.loadUrl(url);
		}
	}

	/**
	 * Is chrome custom tabs supported for this SDK version?
	 * More specifically, is the Chrome app supported for this SDK version?
	 * @return boolean where true means that chrome custom tabs is supported and false meaning it is not supported
	 */
	private boolean chromeCustomTabsSupported()
	{
		// Chrome is only supported on Jelly Bean and above
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	/**
	 * Launches a chrome custom tab with the given {@link String} url
	 * @param url which you want to load using chrome custom tabs
	 */
	public void launchChromeCustomTabs(@NonNull String url)
	{
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
		builder.addDefaultShareMenuItem();

		int toolbarColor = getToolbarColor();

		if (toolbarColor != 0)
		{
			builder.setToolbarColor(toolbarColor);
		}

		builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
		builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right);

		Uri uri = Uri.parse(url);

		if (uri.getScheme().startsWith("file"))
		{
			uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(uri.getPath()));
		}

		CustomTabsIntent customTabsIntent = builder.build();
		customTabsIntent.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		customTabsIntent.launchUrl(this, uri);
	}

	@ColorInt protected int getToolbarColor()
	{
		TypedValue typedValue = new TypedValue();

		TypedArray a = obtainStyledAttributes(typedValue.data, new int[] {R.attr.colorPrimary});
		int color = a.getColor(0, 0);

		a.recycle();

		return color;
	}

	@Override public void onClick(View v)
	{
		if (v == mWeb)
		{
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(webView.getUrl() == null ? getIntent().getExtras().getString(EXTRA_FILE_NAME) : webView.getUrl()));
			startActivity(i);
		}
		else if (v == mBack)
		{
			webView.goBack();
		}
		else if (v == mForward)
		{
			webView.goForward();
		}
		else if (v == mClose)
		{
			finish();
		}
		else if (v == mShare)
		{
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
			shareIntent.setType("text/plain");
			startActivity(shareIntent);
		}
	}

	@Override public void onSaveInstanceState(Bundle savedInstanceState)
	{
		webView.saveState(savedInstanceState);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		webView.restoreState(savedInstanceState);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem back = menu.add(0, 2, 0, "Back");
		MenuItem forward = menu.add(0, 3, 0, "Forward");
		MenuItem refresh = menu.add(0, 1, 0, "Refresh");
		MenuItem open = menu.add(0, 4, 0, "Open external");

		MenuItemCompat.setShowAsAction(back, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		MenuItemCompat.setShowAsAction(forward, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		MenuItemCompat.setShowAsAction(open, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack())
		{
			webView.goBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == 4)
		{
			onClick(mWeb);
		}
		else if (item.getItemId() == 2)
		{
			onClick(mBack);
		}
		else if (item.getItemId() == 3)
		{
			webView.goForward();
		}
		else if (item.getItemId() == 1)
		{
			webView.reload();
		}

		return super.onOptionsItemSelected(item);
	}
}
