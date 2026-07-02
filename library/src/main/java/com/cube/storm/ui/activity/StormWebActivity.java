package com.cube.storm.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Window;
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
public class StormWebActivity extends AppCompatActivity
{
	public static final String EXTRA_FILE_NAME = "extra_file_name";

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

		launchChromeCustomTabs(url);
		finish();
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
}
