package com.cube.storm.ui.view.holder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * // TODO: Add class description
 *
 * @author Matt Allen
 * @Project LightningUi
 */
public abstract class ViewHolder<T> extends RecyclerView.ViewHolder
{
	public ViewHolder(View itemView)
	{
		super(itemView);
	}

	/**
	 * Called when the view needs to be populated
	 *
	 * @param model The model to populate the view with
	 */
	public abstract void populateView(T model);
}
