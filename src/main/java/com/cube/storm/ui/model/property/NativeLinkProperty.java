package com.cube.storm.ui.model.property;

import android.os.Parcel;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A link property that will have a native URI scheme. You must override {@link com.cube.storm.ui.lib.factory.IntentFactory} in order to handle
 * the link.
 *
 * @author Callum Taylor
 * @project LightningUi
 */
@NoArgsConstructor
@Accessors(chain = true) @Data
public class NativeLinkProperty extends DestinationLinkProperty
{
	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}
