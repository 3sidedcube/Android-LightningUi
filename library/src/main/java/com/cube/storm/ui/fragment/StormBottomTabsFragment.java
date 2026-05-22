package com.cube.storm.ui.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.lib.helper.ImageHelper;
import com.cube.storm.ui.model.descriptor.PageDescriptor;
import com.cube.storm.ui.model.descriptor.TabbedPageDescriptor;
import com.cube.storm.ui.model.page.TabbedPageCollection;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import android.graphics.drawable.Drawable;
import lombok.Getter;

/**
 * Renders a Storm TabbedPageCollection as a bottom tabs view.
 */
public class StormBottomTabsFragment extends StormTabbedFragment implements AHBottomNavigation.OnTabSelectedListener, AHBottomNavigation.OnLayoutChangeListener
{
	private static final String EXTRA_SELECTED_TAB = "selectedTab";
	public static final int MAX_BOTTOM_TABS = 5;

	private AHBottomNavigationViewPager viewPager;
	public AHBottomNavigation bottomNavigation;
	@Getter private int selectedTab = 0;
	@Getter private List<String> tabTitles = new ArrayList<>();

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if (view == null || getContext() == null)
		{
			return null;
		}
		viewPager = view.findViewById(R.id.view_pager);
		bottomNavigation = view.findViewById(R.id.bottom_tabs);

		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_SELECTED_TAB))
		{
			selectedTab = savedInstanceState.getInt(EXTRA_SELECTED_TAB);
		}
		return view;
	}

	/**
	 * Loads no more than 5 elements, and calls addTabBarItemToBottomTabs method
	 *
	 * @param collection All pages available
	 */
	protected void loadPages(@NonNull TabbedPageCollection collection)
	{
		super.loadPages(collection);
		int maxElements = Math.min(pageAdapter.getPages().size(), MAX_BOTTOM_TABS);
		// It creates a bottom tab element for 5 adapter items or less
		for (int bottomTabIdx = 0; bottomTabIdx < maxElements; bottomTabIdx++)
		{
			PageDescriptor pageDescriptor = pageAdapter.getPages().get(bottomTabIdx).getPageDescriptor();
			if (pageDescriptor instanceof TabbedPageDescriptor)
			{
				addTabBarItemToBottomTabs((TabbedPageDescriptor)pageDescriptor);
			}
		}

		viewPager.setCurrentItem(selectedTab, true);
		bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
		bottomNavigation.setOnTabSelectedListener(this);
		bottomNavigation.addOnLayoutChangeListener(this);
		setAccentColorFromBottomNavigation();
	}

	protected void setAccentColorFromBottomNavigation()
	{
		bottomNavigation.setAccentColor(ResourcesCompat.getColor(getResources(), R.color.main_red, null));
	}

	private void addTabBarItemToBottomTabs(TabbedPageDescriptor descriptor)
	{
		final Resources resources = getResources();
		final int iconWidthHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.getDisplayMetrics());
		final String iconSrc = ImageHelper.getImageSrc(descriptor.getTabBarItem().getImage(), iconWidthHeight, iconWidthHeight);
		final String itemName = UiSettings.getInstance().getTextProcessor().process(descriptor.getTabBarItem().getTitle());
		tabTitles.add(itemName);
		final AHBottomNavigationItem navItem = new AHBottomNavigationItem(itemName, R.drawable.ic_collapse);
		bottomNavigation.addItem(navItem);

		if (iconSrc != null)
		{
			String resolvedSrc = ImageHelper.resolveUri(iconSrc);
			Glide.with(this)
			     .asBitmap()
			     .load(resolvedSrc)
			     .into(new CustomTarget<Bitmap>()
			     {
				     @Override public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
				     {
					     navItem.setDrawable(new BitmapDrawable(resources, resource));
					     bottomNavigation.refresh();
				     }

				     @Override public void onLoadCleared(@Nullable Drawable placeholder)
				     {
						 // Empty
				     }
			     });
		}
	}

	/**
	 * Creates menu items if the number of pages is greater than 5
	 *
	 * @param menu The current menu
	 * @param inflater the MenuInflater class to inflate the menu
	 */
	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		if (viewPager.getAdapter() != null && viewPager.getAdapter().getCount() > MAX_BOTTOM_TABS)
		{
			for (int iterator = MAX_BOTTOM_TABS; iterator < pageAdapter.getPages().size(); iterator++)
			{
				MenuItem item = menu.add(viewPager.getAdapter().getPageTitle(iterator));
				final int finalIterator = iterator;
				item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						if (getContext() == null)
						{
							return false;
						}
						PageDescriptor pageDescriptor = pageAdapter.getPages().get(finalIterator).getPageDescriptor();
						Intent stormIntent = UiSettings.getInstance().getIntentFactory().getIntentForPageDescriptor(getContext(), pageDescriptor);
						startActivity(stormIntent);
						return true;
					}
				});
			}
		}
	}

	@Override public int getLayoutResource()
	{
		return R.layout.tabbed_page_bottom_fragment_view;
	}

	/**
	 * Calls the content description update and changes the section title
	 *
	 * @param position Position of the tab
	 * @param wasSelected Selected tab
	 * @return
	 */
	@Override
	public boolean onTabSelected(int position, boolean wasSelected)
	{
		viewPager.setCurrentItem(position);
		selectedTab = position;

		if (getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null)
		{    //Hide the back arrow in the main activity
			ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
			if (actionBar != null)
			{
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setTitle(bottomNavigation.getItem(position).getTitle(getContext()));
			}
		}
		return true;
	}

	// When the bottomNavigation layout change, set the tab item content descriptions for every tab
	@Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
	{
		setTabItemContentDescriptions();
	}

	/**
	 * This method set the content descriptions for all items from the bottom navigation menu.
	 * Check if the current tab is selected to update the content description comment
	 */
	private void setTabItemContentDescriptions()
	{
		int tabCount = bottomNavigation.getItemsCount();
		for (int bottomTabIdx = 0; bottomTabIdx < tabCount; bottomTabIdx++)
		{
			View tab = bottomNavigation.getViewAtPosition(bottomTabIdx);
			if (tab != null)
			{
				String formatString = selectedTab == bottomTabIdx ? getString(R.string.bottom_navigation_tab_selected) : getString(R.string.bottom_navigation_tab);
				String tabContentDescription = String.format(formatString, tabTitles.get(bottomTabIdx), bottomTabIdx + 1, tabCount);
				tab.setContentDescription(tabContentDescription);
				tab.setFocusable(true);
			}
		}
	}

	@Override
	public void switchToTab(int index)
	{
		bottomNavigation.setCurrentItem(index);
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(EXTRA_SELECTED_TAB, selectedTab);
		super.onSaveInstanceState(outState);
	}
}
