package com.cube.storm.ui.view.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.model.list.collection.CollectionItem;
import com.cube.storm.ui.model.list.collection.CollectionListItem;
import com.cube.storm.ui.model.property.LinkProperty;
import com.cube.storm.ui.view.ViewClickable;

import java.util.ArrayList;
import java.util.List;

/**
 * // TODO: Add class description
 *
 * @author Alan Le Fournis
 * @project Storm
 */
public class CollectionListItemHolder extends Holder<CollectionListItem>
{
	private ViewHolder holder;
	private View view;
	private LinearLayout linearLayout;
	protected LinearLayout embeddedLinksContainer;

	@Override public View createView(ViewGroup parent)
	{
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_list_item_view, null, false);
		linearLayout = (LinearLayout)view.findViewById(R.id.view_container);
		embeddedLinksContainer = (LinearLayout)view.findViewById(R.id.embedded_links_container);
		holder = new ViewHolder(view);

		return view;
	}

	@Override public void populateView(CollectionListItem model)
	{
		List<View> views = new ArrayList<View>(model.getCells().size());
		ArrayList<CollectionItem> arrayList = new ArrayList<CollectionItem>();
		arrayList.addAll(model.getCells());

		for (int index = 0; index < arrayList.size(); index++)
		{
			View subView = getView(arrayList.get(index), holder.viewContainer.getChildAt(index));

			if (subView != null)
			{
				views.add(subView);
			}
		}

		holder.viewContainer.removeAllViews();

		for (View subView : views)
		{
			holder.viewContainer.addView(subView);
		}

		view.refreshDrawableState();

		if (model.getEmbeddedLinks() != null)
		{
			embeddedLinksContainer.removeAllViews();

			for (LinkProperty linkProperty : model.getEmbeddedLinks())
			{
				final LinkProperty property = linkProperty;
				View embeddedLinkView = LayoutInflater.from(embeddedLinksContainer.getContext()).inflate(R.layout.button_embedded_link, embeddedLinksContainer, false);

				if (embeddedLinkView != null)
				{
					Button button = (Button)embeddedLinkView.findViewById(R.id.button);
					button.setVisibility(View.GONE);
					String content = UiSettings.getInstance().getTextProcessor().process(linkProperty.getTitle().getContent());

					if (!TextUtils.isEmpty(content))
					{
						button.setText(content);
						button.setVisibility(View.VISIBLE);
					}

					button.setOnClickListener(new OnClickListener()
					{
						@Override public void onClick(View v)
						{
							UiSettings.getInstance().getLinkHandler().handleLink(v.getContext(), property);
						}
					});

					embeddedLinksContainer.setVisibility(View.VISIBLE);
					embeddedLinksContainer.addView(button);
				}
			}
		}
	}

	protected View getView(final CollectionItem model, View view)
	{
		if (model != null)
		{
			Holder holder = null;

			if (view == null)
			{
				try
				{
					Class<? extends Holder> cls = UiSettings.getInstance().getViewFactory().getHolderForView(model.getClassName());
					holder = cls.newInstance();
					view = holder.createView((ViewGroup)linearLayout.getParent());
					holder.populateView(model);

					view.setTag(holder);
				}
				catch (InstantiationException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				if (view.getTag() != null)
				{
					holder = (Holder)view.getTag();
					holder.populateView(model);
				}
			}

			if (view != null)
			{
				final Holder tmp = holder;

				if (tmp != null && ViewClickable.class.isAssignableFrom(tmp.getClass()))
				{
					view.setOnClickListener(new OnClickListener()
					{
						@Override public void onClick(View v)
						{
							((ViewClickable)tmp).onClick(model, v);
						}
					});
				}
			}

			return view;
		}
		return null;
	}

	public static class ViewHolder
	{
		public LinearLayout viewContainer;

		public ViewHolder(View root)
		{
			viewContainer = (LinearLayout)root.findViewById(R.id.view_container);
		}
	}
}
