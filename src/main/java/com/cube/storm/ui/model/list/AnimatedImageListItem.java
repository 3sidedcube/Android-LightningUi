package com.cube.storm.ui.model.list;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Stored properties for an animated list item
 *
 * @author Luke Reed
 * @project Storm
 */
public class AnimatedImageListItem extends ListItem
{
	@SerializedName("delay") @Getter protected long delay;

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}
