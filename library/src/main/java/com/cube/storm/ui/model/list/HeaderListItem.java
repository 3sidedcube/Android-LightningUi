package com.cube.storm.ui.model.list;

import android.os.Parcel;

import com.cube.storm.ui.model.property.ImageProperty;
import com.cube.storm.ui.model.property.TextProperty;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A view model with an image and description property
 *
 * @author Alan Le Fournis
 * @project LightningUi
 */
@NoArgsConstructor @AllArgsConstructor
@Accessors(chain = true) @Data @EqualsAndHashCode(callSuper=false)
public class HeaderListItem extends TitleListItem
{
	public static String CLASS_NAME = "HeaderListItem";

	{ this.className = CLASS_NAME; }

	protected ArrayList<ImageProperty> image;

	protected TextProperty description;

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}
