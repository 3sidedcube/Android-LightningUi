package com.cube.storm.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * A {@link ViewPager} that disables swiping between pages. Page changes are driven exclusively
 * through the bottom navigation. Replaces the legacy {@code AHBottomNavigationViewPager}.
 */
public class NonSwipeableViewPager extends ViewPager
{
	public NonSwipeableViewPager(Context context)
	{
		super(context);
	}

	public NonSwipeableViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		// Never intercept touch events so swiping is disabled
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// Do not handle swipe gestures
		return false;
	}
}