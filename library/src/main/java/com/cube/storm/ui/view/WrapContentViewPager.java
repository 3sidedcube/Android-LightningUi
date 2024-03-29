package com.cube.storm.ui.view;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Calculates the highest height for all the items, and set that height as default.
 */
public class WrapContentViewPager extends ViewPager
{
	public WrapContentViewPager(Context context)
	{
		super(context);
	}

	public WrapContentViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		// Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
		// At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
		if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
			// super has to be called in the beginning so the child views can be initialized.
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			int height = 0;
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				child.measure(
					MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
				);
				int h = child.getMeasuredHeight();
				if (h > height) height = h;
			}

			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		}
		// super has to be called again so the new specs are treated as exact measurements
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
