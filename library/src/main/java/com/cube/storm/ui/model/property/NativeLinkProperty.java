package com.cube.storm.ui.model.property;

import android.os.Parcel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * A link property that will have a native URI scheme. You must override {@link com.cube.storm.ui.lib.factory.IntentFactory} in order to handle
 * the link.
 *
 * @author Callum Taylor
 * @project LightningUi
 */
@AllArgsConstructor
@Accessors(chain = true) @Data @EqualsAndHashCode(callSuper=false)
public class NativeLinkProperty extends DestinationLinkProperty
{
	public static String CLASS_NAME = "NativeLink";

	{ this.className = CLASS_NAME; }

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}
