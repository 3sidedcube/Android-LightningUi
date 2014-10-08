package com.cube.storm.ui.lib.factory;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.activity.StormActivity;
import com.cube.storm.ui.data.FragmentIntent;
import com.cube.storm.ui.fragment.StormListFragment;
import com.cube.storm.ui.model.Model;
import com.cube.storm.ui.model.descriptor.PageDescriptor;
import com.cube.storm.ui.model.page.ListPage;
import com.cube.storm.ui.model.page.Page;

/**
 * This is the factory class which is used by Storm to decide which activity/fragments to instantiate
 * based on the source file's class type, name, or Uri.
 *
 * @author Callum Taylor
 * @project StormUI
 */
public abstract class IntentFactory
{
	/**
	 * @deprecated You should not load a fragment intent from an already-instantiated page, use {@link #getFragmentIntentForPageDescriptor(com.cube.storm.ui.model.descriptor.PageDescriptor)} instead
	 *
	 * Returns a fragment intent for a specific page
	 *
	 * @param pageData The page to use to decide the fragment that is used
	 *
	 * @return The fragment intent, or null if one was not suitable enough
	 */
	@Nullable @Deprecated
	public FragmentIntent getFragmentIntentForPage(@NonNull Page pageData)
	{
		FragmentIntent intent;

		if (pageData instanceof ListPage)
		{
			intent = new FragmentIntent(StormListFragment.class, pageData.getTitle() != null ? pageData.getTitle().getContent() : "", null);
			return intent;
		}

		return null;
	}

	/**
	 * @deprecated You should not load an intent from an already-instantiated page, use {@link #getIntentForPageDescriptor(android.content.Context, com.cube.storm.ui.model.descriptor.PageDescriptor)} instead
	 *
 	 * Returns an activity intent for a specific page
	 *
	 * @param pageData The page to use to decide the activity that is used
	 *
	 * @return The intent, or null if one was not suitable enough
	 */
	@Nullable @Deprecated
	public Intent getIntentForPage(@NonNull Context context, @NonNull Page pageData)
	{
		Intent intent;

		if (pageData instanceof ListPage)
		{
			intent = new Intent(context, StormActivity.class);
			return intent;
		}

		return null;
	}

	/**
	 * Loads a fragment intent from a page descriptor by finding the model of the page type defined in {@link com.cube.storm.ui.model.descriptor.PageDescriptor#getType()} in the
	 * {@link com.cube.storm.ui.view.View} enum.
	 *
	 * @param pageDescriptor The page descriptor to load from
	 *
	 * @return The intent, or null if one was not suitable enough
	 */
	@Nullable
	public FragmentIntent getFragmentIntentForPageDescriptor(@NonNull PageDescriptor pageDescriptor)
	{
		FragmentIntent intent;
		Class<? extends Model> pageType = UiSettings.getInstance().getViewFactory().getModelForView(pageDescriptor.getType());

		if (ListPage.class.isAssignableFrom(pageType))
		{
			intent = new FragmentIntent(StormListFragment.class, null);
			return intent;
		}

		return null;
	}

	/**
	 * Loads an intent from a page descriptor by finding the model of the page type defined in {@link com.cube.storm.ui.model.descriptor.PageDescriptor#getType()} in the
	 * {@link com.cube.storm.ui.view.View} enum.
	 *
	 * @param context The context used to create the intent
	 * @param pageDescriptor The page descriptor to load from
	 *
	 * @return The intent, or null if one was not suitable enough
	 */
	@Nullable
	public Intent getIntentForPageDescriptor(@NonNull Context context, @NonNull PageDescriptor pageDescriptor)
	{
		Intent intent;
		Class<? extends Model> pageType = UiSettings.getInstance().getViewFactory().getModelForView(pageDescriptor.getType());

		if (ListPage.class.isAssignableFrom(pageType))
		{
			intent = new Intent(context, StormActivity.class);
			return intent;
		}

		return null;
	}
}
