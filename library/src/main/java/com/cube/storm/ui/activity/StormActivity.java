package com.cube.storm.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;
import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.data.FragmentIntent;
import com.cube.storm.ui.lib.EdgeToEdgeUtils;

/**
 * Base storm activity that hosts a single fragment to host any {@link com.cube.storm.ui.model.page.Page} subclass.
 *
 * @author Callum Taylor
 * @project LightingUi
 */
public class StormActivity extends AppCompatActivity implements StormInterface
{
	public static final String EXTRA_URI = "stormui.uri";

	protected void configureEdgeToEdge()
	{
		EdgeToEdgeUtils.addAllPaddings(findViewById(R.id.fragment_holder));
	}

	@Override public void onCreate(Bundle savedInstanceState)
	{
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResource());

		configureEdgeToEdge();

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeActionContentDescription("Navigate back to main screen");
		}

		if (savedInstanceState == null)
		{
			if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_URI))
			{
				String pageUri = String.valueOf(getIntent().getExtras().get(EXTRA_URI));
				loadPage(pageUri);
			}
			else
			{
				onLoadFail();
			}
		}
	}

	/**
	 * See https://developer.android.com/training/implementing-navigation/ancestral
	 *
	 * @return Boolean indicating whether the press was handled (always true currently)
	 */
	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			try
			{
				Intent upIntent = NavUtils.getParentActivityIntent(this);

				if (upIntent == null)
				{
					finish();
					return true;
				}

				if (NavUtils.shouldUpRecreateTask(this, upIntent))
				{
					// This activity is NOT part of this app's task, so create a new task
					// when navigating up, with a synthesized back stack.
					TaskStackBuilder.create(this)
					                // Add all of this activity's parents to the back stack
					                .addNextIntentWithParentStack(upIntent)
					                // Navigate up to the closest parent
					                .startActivities();
				}
				else
				{
					finish();
				}
			}
			catch (Exception ex)
			{
				finish();
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public int getLayoutResource()
	{
		return R.layout.activity_view;
	}

	@Override public void loadPage(String pageUri)
	{
		FragmentIntent fragmentIntent = UiSettings.getInstance().getIntentFactory().getFragmentIntentForPageUri(Uri.parse(pageUri));

		if (fragmentIntent != null)
		{
			fragmentIntent.getArguments().putAll(getIntent().getExtras());

			Fragment fragment = Fragment.instantiate(this, fragmentIntent.getFragment().getName(), fragmentIntent.getArguments());
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).commit();

			if (!TextUtils.isEmpty(fragmentIntent.getTitle()))
			{
				setTitle(fragmentIntent.getTitle());
			}
		}
		else
		{
			onLoadFail();
		}
	}

	@Override public void onLoadFail()
	{
		Toast.makeText(this, "Failed to load page", Toast.LENGTH_SHORT).show();
		finish();
	}
}
