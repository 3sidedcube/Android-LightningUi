/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cube.storm.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cube.storm.ui.R;

import java.util.Locale;

public class PagerSlidingTabStrip extends HorizontalScrollView
{
	public interface IconTabProvider
	{
		public int getPageIconResId(int position);

		public Bitmap getPageIconBitmap(int position);
	}

	// @formatter:off
	private static final int[] ATTRS = new int[] {
		android.R.attr.textSize,
		android.R.attr.textColor
    };
	// @formatter:on

	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;

	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;

	private LinearLayout tabsContainer;
	private ViewPager pager;

	private int tabCount;

	private int currentPosition = 0;
	private float currentPositionOffset = 0f;

	private Paint rectPaint;
	private Paint dividerPaint;

	private int indicatorColor = 0xFF666666;
	private int underlineColor = 0x1A000000;
	private int dividerColor = 0x1A000000;

	private boolean shouldExpand = false;
	private boolean textAllCaps = true;
	private boolean textOnly = false;

	private int scrollOffset = 52;
	private int indicatorHeight = 8;
	private int underlineHeight = 2;
	private int dividerPadding = 12;
	private int tabPadding = 24;
	private int dividerWidth = 1;

	private int tabTextSize = 12;
	private int tabTextColor = 0xFF666666;
	private int tabDeactivateTextColor = 0xFFCCCCCC;

	private int tabIconTint = 0xFF000000;

	private Typeface tabTypeface = null;
	private int tabTypefaceStyle = Typeface.BOLD;

	private int lastScrollX = 0;

	private int tabBackgroundResId = R.drawable.background_tab;
	private int transparentColorId = R.color.transparent;

	private Locale locale;

	private boolean tabSwitch;

	public PagerSlidingTabStrip(Context context)
	{
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		setFillViewport(true);
		setWillNotDraw(false);

		tabsContainer = new LinearLayout(context);
		tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(tabsContainer);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		scrollOffset = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
		indicatorHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
		underlineHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
		dividerPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
		tabPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
		dividerWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
		tabTextSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

		// get system attrs (android:textSize and android:textColor)

		TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

		tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
		tabTextColor = a.getColor(1, tabTextColor);

		a.recycle();

		// get custom attrs

		a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

		indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, indicatorColor);
		underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, underlineColor);
		dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, dividerColor);
		dividerWidth = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerWidth, dividerWidth);
		indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, indicatorHeight);
		underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, underlineHeight);
		dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, dividerPadding);
		tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPadding);
		tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, tabBackgroundResId);
		shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, shouldExpand);
		scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, scrollOffset);
		textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps);
		textOnly = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextOnly, textOnly);
		tabSwitch = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTabSwitch, tabSwitch);
		tabIconTint = a.getColor(R.styleable.PagerSlidingTabStrip_pstsActivateIconTint, tabIconTint);
		tabTextColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsActivateTextColor, tabTextColor);
		tabDeactivateTextColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDeactivateTextColor, tabDeactivateTextColor);

		a.recycle();

		rectPaint = new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setStyle(Style.FILL);

		dividerPaint = new Paint();
		dividerPaint.setAntiAlias(true);
		dividerPaint.setStrokeWidth(dividerWidth);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		if (locale == null)
		{
			locale = getResources().getConfiguration().locale;
		}
	}

	public void setViewPager(ViewPager pager)
	{
		this.pager = pager;

		if (pager.getAdapter() == null)
		{
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}

		pager.setOnPageChangeListener(pageListener);

		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener)
	{
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged()
	{
		tabsContainer.removeAllViews();
		tabCount = pager.getAdapter().getCount();

		for (int i = 0; i < tabCount; i++)
		{
			if (pager.getAdapter() instanceof IconTabProvider && !textOnly)
			{
				if (((IconTabProvider)pager.getAdapter()).getPageIconResId(i) > 0)
				{
					addIconTab(i, ((IconTabProvider)pager.getAdapter()).getPageIconResId(i));
				}
				else if (((IconTabProvider)pager.getAdapter()).getPageIconBitmap(i) != null)
				{
					addIconTab(i, ((IconTabProvider)pager.getAdapter()).getPageIconBitmap(i));
				}
				else
				{
					addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
				}
			}
			else
			{
				addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
			}
		}

		updateTabStyles();

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@SuppressWarnings("deprecation") @SuppressLint("NewApi") @Override
			public void onGlobalLayout()
			{
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				currentPosition = pager.getCurrentItem();
				scrollToChild(currentPosition, 0);
			}
		});
	}

	private void addTextTab(final int position, String title)
	{
		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();

		addTab(position, tab);
	}

	private void addIconTab(final int position, int resId)
	{
		ImageButton tab = new ImageButton(getContext());
		tab.setImageResource(resId);
		tab.setColorFilter(tabIconTint);

		addTab(position, tab);
	}

	private void addIconTab(final int position, Bitmap bitmap)
	{
		ImageView tab = new ImageView(getContext());
		tab.setImageBitmap(bitmap);
		tab.setColorFilter(tabIconTint);
		tab.setScaleType(ScaleType.FIT_CENTER);

		DisplayMetrics dm = getResources().getDisplayMetrics();
		int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dm);

		tab.setPadding(padding + tabPadding, padding, padding + tabPadding, padding);

		tab.setFocusable(true);
		tab.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(android.view.View v)
			{
				pager.setCurrentItem(position);
			}
		});

		tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
	}

	private void addTab(final int position, android.view.View tab)
	{
		tab.setFocusable(true);
		tab.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(android.view.View v)
			{
				pager.setCurrentItem(position);
			}
		});

		tab.setPadding(tabPadding, 0, tabPadding, 0);

		tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
	}

	private void updateTabStyles()
	{
		for (int i = 0; i < tabCount; i++)
		{
			android.view.View v = tabsContainer.getChildAt(i);

			v.setBackgroundResource(!tabSwitch ? tabBackgroundResId : transparentColorId);

			if (v instanceof TextView)
			{

				TextView tab = (TextView)v;
				tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
				tab.setTypeface(tabTypeface, tabTypefaceStyle);
				tab.setTextColor(tabSwitch && i != 0 ? tabDeactivateTextColor : tabTextColor);

				// setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
				// pre-ICS-build
				if (textAllCaps)
				{
					tab.setAllCaps(true);
				}
			}
			else if (v instanceof ImageButton)
			{
				ImageButton tab = (ImageButton)v;
				tab.setSelected(tabSwitch && i == 0);
			}
		}
	}

	private void updateActivateTab(final int position)
	{
		for (int i = 0; i < tabCount; i++)
		{
			android.view.View v = tabsContainer.getChildAt(i);

			if (v instanceof TextView)
			{
				TextView tab = (TextView)v;
				tab.setTextColor(position == i ? tabTextColor : tabDeactivateTextColor);
			}
			else
			{
				v.setSelected(position == i ? true : false);
			}
		}
	}

	private void scrollToChild(int position, int offset)
	{
		if (tabCount == 0)
		{
			return;
		}

		int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

		if (position > 0 || offset > 0)
		{
			newScrollX -= scrollOffset;
		}

		if (newScrollX != lastScrollX)
		{
			lastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (isInEditMode() || tabCount == 0)
		{
			return;
		}

		final int height = getHeight();

		// draw indicator line

		rectPaint.setColor(indicatorColor);

		// default: line below current tab
		android.view.View currentTab = tabsContainer.getChildAt(currentPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();

		// if there is an offset, start interpolating left and right coordinates between current and next tab
		if (currentPositionOffset > 0f && currentPosition < tabCount - 1)
		{

			android.view.View nextTab = tabsContainer.getChildAt(currentPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
			lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
		}

		canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

		// draw underline

		rectPaint.setColor(underlineColor);
		canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

		// draw divider

		dividerPaint.setColor(dividerColor);
		for (int i = 0; i < tabCount - 1; i++)
		{
			android.view.View tab = tabsContainer.getChildAt(i);
			canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
		}
	}

	private class PageListener implements OnPageChangeListener
	{
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
			currentPosition = position;
			currentPositionOffset = positionOffset;

			if (tabsContainer.getChildAt(position) != null)
			{
				scrollToChild(position, (int)(positionOffset * tabsContainer.getChildAt(position).getWidth()));
			}

			invalidate();

			if (delegatePageListener != null)
			{
				delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state)
		{
			if (state == ViewPager.SCROLL_STATE_IDLE)
			{
				scrollToChild(pager.getCurrentItem(), 0);
			}

			if (delegatePageListener != null)
			{
				delegatePageListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int position)
		{
			if (tabSwitch)
			{
				updateActivateTab(position);
			}

			if (delegatePageListener != null)
			{
				delegatePageListener.onPageSelected(position);
			}
		}
	}

	public void setIndicatorColor(int indicatorColor)
	{
		this.indicatorColor = indicatorColor;
		invalidate();
	}

	public void setIndicatorColorResource(int resId)
	{
		this.indicatorColor = getResources().getColor(resId);
		invalidate();
	}

	public int getIndicatorColor()
	{
		return this.indicatorColor;
	}

	public void setIndicatorHeight(int indicatorLineHeightPx)
	{
		this.indicatorHeight = indicatorLineHeightPx;
		invalidate();
	}

	public int getIndicatorHeight()
	{
		return indicatorHeight;
	}

	public void setUnderlineColor(int underlineColor)
	{
		this.underlineColor = underlineColor;
		invalidate();
	}

	public void setUnderlineColorResource(int resId)
	{
		this.underlineColor = getResources().getColor(resId);
		invalidate();
	}

	public int getUnderlineColor()
	{
		return underlineColor;
	}

	public void setDividerColor(int dividerColor)
	{
		this.dividerColor = dividerColor;
		invalidate();
	}

	public void setDividerColorResource(int resId)
	{
		this.dividerColor = getResources().getColor(resId);
		invalidate();
	}

	public int getDividerColor()
	{
		return dividerColor;
	}

	public void setUnderlineHeight(int underlineHeightPx)
	{
		this.underlineHeight = underlineHeightPx;
		invalidate();
	}

	public int getUnderlineHeight()
	{
		return underlineHeight;
	}

	public void setDividerPadding(int dividerPaddingPx)
	{
		this.dividerPadding = dividerPaddingPx;
		invalidate();
	}

	public int getDividerPadding()
	{
		return dividerPadding;
	}

	public void setScrollOffset(int scrollOffsetPx)
	{
		this.scrollOffset = scrollOffsetPx;
		invalidate();
	}

	public int getScrollOffset()
	{
		return scrollOffset;
	}

	public void setShouldExpand(boolean shouldExpand)
	{
		this.shouldExpand = shouldExpand;
		requestLayout();
	}

	public boolean getShouldExpand()
	{
		return shouldExpand;
	}

	public boolean isTextAllCaps()
	{
		return textAllCaps;
	}

	public void setAllCaps(boolean textAllCaps)
	{
		this.textAllCaps = textAllCaps;
	}

	public void setTextSize(int textSizePx)
	{
		this.tabTextSize = textSizePx;
		updateTabStyles();
	}

	public int getTextSize()
	{
		return tabTextSize;
	}

	public void setTextColor(int textColor)
	{
		this.tabTextColor = textColor;
		updateTabStyles();
	}

	public void setTextColorResource(int resId)
	{
		this.tabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getTextColor()
	{
		return tabTextColor;
	}

	public void setTypeface(Typeface typeface, int style)
	{
		this.tabTypeface = typeface;
		this.tabTypefaceStyle = style;
		updateTabStyles();
	}

	public void setTabBackground(int resId)
	{
		this.tabBackgroundResId = resId;
	}

	public int getTabBackground()
	{
		return tabBackgroundResId;
	}

	public void setTabPaddingLeftRight(int paddingPx)
	{
		this.tabPadding = paddingPx;
		updateTabStyles();
	}

	public int getTabPaddingLeftRight()
	{
		return tabPadding;
	}

	public void setTabSwitch(boolean tabSwitch)
	{
		this.tabSwitch = tabSwitch;
		updateTabStyles();
	}

	public void setActivateTextColor(int activateTextColor)
	{
		this.tabTextColor = activateTextColor;
		updateTabStyles();
	}

	public void setDeactivateTextColor(int deactivateTextColor)
	{
		this.tabDeactivateTextColor = deactivateTextColor;
		updateTabStyles();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state)
	{
		SavedState savedState = (SavedState)state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = currentPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState
	{
		int currentPosition;

		public SavedState(Parcelable superState)
		{
			super(superState);
		}

		private SavedState(Parcel in)
		{
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
		{
			@Override
			public SavedState createFromParcel(Parcel in)
			{
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
	}
}
