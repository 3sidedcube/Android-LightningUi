package com.cube.storm.ui.model.property;

import android.os.Parcel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * button property class.
 *
 * @author Alan Le Fournis
 * @project LightningUi
 */
@NoArgsConstructor @AllArgsConstructor(suppressConstructorProperties = true)
@Accessors(chain = true) @Data
public class ButtonProperty extends Property
{
	{ this.className = "Button"; }

	private TextProperty title;
	private LinkProperty link;

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel parcel, int i)
	{

	}
}