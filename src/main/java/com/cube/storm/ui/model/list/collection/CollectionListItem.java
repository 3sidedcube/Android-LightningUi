package com.cube.storm.ui.model.list.collection;

import android.os.Parcel;

import com.cube.storm.ui.model.Model;

import java.util.Collection;

import lombok.Getter;

/**
 * // TODO: Add class description
 *
 * @author Alan Le Fournis
 * @project Storm
 */
public class CollectionListItem extends Model
{
	@Getter protected Collection<CollectionItem> cells;

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}