package com.cube.storm.ui.controller.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.model.Model;
import com.cube.storm.ui.model.list.List;
import com.cube.storm.ui.model.list.List.ListFooter;
import com.cube.storm.ui.model.list.List.ListHeader;
import com.cube.storm.ui.view.ViewClickable;
import com.cube.storm.ui.view.holder.Holder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The base adapter used for displaying Storm views in a list. Using an adapter to do such a task has
 * the benefit of view recycling which makes the content smooth to scroll.
 *
 * This adapter only supports {@link com.cube.storm.ui.model.Model} classes which have a defined {@link com.cube.storm.ui.view.holder.Holder} counter-class.
 *
 * <b>Usage</b>
 *
 * <b>Problems</b>
 * Problems can arise with this method of rendering content, specifically with render efficiency where
 * the views are not being recycled because there is only 1 of its view type in the list. This is the
 * equivalent of having all of the views inflated into a {@link android.widget.ScrollView}. The smoothness
 * of the scrolling (depending on how much content there is) diminishes with the amount of unique content
 * that the list is rendering.
 *
 * @author Callum Taylor
 * @project StormUI
 */
public class StormListAdapter extends BaseAdapter
{
	/**
	 * The list of models of the views we are rendering in the list. This is a 1 dimensional representation
	 * of a multi-dimensional 'sub listed' array set which is outlined by the json. When setting the items
	 * in this list, the models have to be traversed in order to build the 1 dimensional list for the
	 * adapter to work correctly.
	 */
	private ArrayList<Model> items = new ArrayList<Model>();

	/**
	 * The different unique item types. This is used to tell the adapter how many unique views we're
	 * going to be rendering so it knows what and when to recycle. The list is just for index based
	 * convenience, the object type in the list is a reference to the view holder class we will use
	 * to render said view.
	 */
	private ArrayList<Class<? extends Holder>> itemTypes = new ArrayList<Class<? extends Holder>>();

	private Context context;

	public StormListAdapter(Context context)
	{
		this.context = context;
	}

	public StormListAdapter(Context context, Collection<? extends Model> items)
	{
		this.context = context;
		setItems(items);
	}

	/**
	 * Sets the items in the collection. Filters out any model that does not have a defined {@link com.cube.storm.ui.view.holder.Holder}
	 *
	 * @param items The new items to set. Can be null to clear the list.
	 */
	public void setItems(@Nullable Collection<? extends Model> items)
	{
		if (items != null)
		{
			this.items = new ArrayList<Model>(items.size());
			this.itemTypes = new ArrayList<Class<? extends Holder>>(items.size() / 2);

			for (Model item : items)
			{
				addItem(item);
			}
		}
		else
		{
			this.items = new ArrayList<Model>(0);
			this.itemTypes = new ArrayList<Class<? extends Holder>>(0);
		}

		Log.e("DEBUG", items.toString());
	}

	/**
	 * Adds an item to the list, only if a holder class is found as returned by {@link com.cube.storm.ui.lib.factory.ViewFactory#getHolderForView(String)}
	 *
	 * @param item The model to add to the list
	 */
	public void addItem(@NonNull Model item)
	{
		if (item instanceof List)
		{
			if (((List)item).getHeader() != null && !TextUtils.isEmpty(((List)item).getHeader().getContent()))
			{
				ListHeader header = new ListHeader();
				header.setHeader(((List)item).getHeader());

				addItem(header);
			}

			if (((List)item).getChildren() != null)
			{
				for (Model subItem : ((List)item).getChildren())
				{
					if (subItem != null)
					{
						addItem(subItem);
					}
				}
			}

			if (((List)item).getFooter() != null && !TextUtils.isEmpty(((List)item).getFooter().getContent()))
			{
				ListFooter footer = new ListFooter();
				footer.setFooter(((List)item).getFooter());

				addItem(footer);
			}
		}
		else
		{
			Class<? extends Holder> holderClass = UiSettings.getInstance().getViewFactory().getHolderForView(item.getClassName());

			if (holderClass != null)
			{
				this.items.add(item);
			}

			if (!this.itemTypes.contains(holderClass))
			{
				this.itemTypes.add(holderClass);
			}
		}
	}

	@Override public int getCount()
	{
		return items.size();
	}

	@Override public Model getItem(int position)
	{
		return items.get(position);
	}

	@Override public long getItemId(int position)
	{
		return ((Object)getItem(position)).hashCode();
	}

	@Override public boolean isEnabled(int position)
	{
		return ViewClickable.class.isAssignableFrom(itemTypes.get(getItemViewType(position)));
	}

	@Override public int getViewTypeCount()
	{
		return itemTypes.size();
	}

	@Override public int getItemViewType(int position)
	{
		Model view = items.get(position);
		return itemTypes.indexOf(UiSettings.getInstance().getViewFactory().getHolderForView(view.getClassName()));
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		Model model = getItem(position);
		Holder holder = null;

		if (convertView != null)
		{
			holder = (Holder)convertView.getTag();
		}
		else
		{
			try
			{
				holder = itemTypes.get(getItemViewType(position)).newInstance();
				convertView = holder.createView(parent);
			}
			catch (Exception e)
			{
				throw new InstantiationError("Could not instantiate a new holder for model " + model.getClassName());
			}
		}

		if (holder != null)
		{
			holder.populateView(model);
		}

		return convertView;
	}
}
