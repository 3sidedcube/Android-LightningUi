package com.cube.storm.ui.view.holder;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.R;
import com.cube.storm.ui.model.list.ToggleableListItem;
import com.cube.storm.ui.model.property.LinkProperty;
import com.cube.storm.ui.view.ViewClickable;

/**
 * View holder for {@link com.cube.storm.ui.model.list.ToggleableListItem} in the adapter
 *
 * @author Alan Le Fournis
 * @project Storm
 */
public class ToggleableListItemHolder extends Holder<ToggleableListItem> implements ViewClickable<ToggleableListItem>
{
	protected ViewGroup toggleContainer;
	protected ImageView expandIcon;
	protected TextView title;
	protected TextView description;
	protected LinearLayout embeddedLinksContainer;

	@Override public View createView(ViewGroup parent)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.toggleable_list_item_view, parent, false);
		toggleContainer = (ViewGroup)view.findViewById(R.id.toggle_container);
		expandIcon = (ImageView)view.findViewById(R.id.expand_icon);
		title = (TextView)view.findViewById(R.id.title);
		description = (TextView)view.findViewById(R.id.description);
		embeddedLinksContainer = (LinearLayout)view.findViewById(R.id.embedded_links_container);

		return view;
	}

	@Override public void populateView(ToggleableListItem model)
	{
		description.setVisibility(View.GONE);
		title.setVisibility(View.GONE);

		if (model.getTitle() != null)
		{
			String content = UiSettings.getInstance().getTextProcessor().process(model.getTitle().getContent());

			if (!TextUtils.isEmpty(content))
			{
				title.setText(content);
				title.setVisibility(View.VISIBLE);
			}
		}

		if (model.getDescription() != null)
		{
			String content = UiSettings.getInstance().getTextProcessor().process(model.getDescription().getContent());

			if (!TextUtils.isEmpty(content))
			{
				description.setText(content);
				description.setVisibility(View.VISIBLE);
			}
		}

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
					button.setText(property.getTitle().getContent());

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

		toggleContainer.setVisibility(View.GONE);
	}

	@Override public void onClick(@NonNull ToggleableListItem model, @NonNull View view)
	{
		if (toggleContainer.getVisibility() == View.GONE)
		{
			toggleContainer.setVisibility(View.VISIBLE);
			expandIcon.setImageResource(R.drawable.ic_collapse);
		}
		else
		{
			toggleContainer.setVisibility(View.GONE);
			expandIcon.setImageResource(R.drawable.ic_expand);
		}
	}
}
