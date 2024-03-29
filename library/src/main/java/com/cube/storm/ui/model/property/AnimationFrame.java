package com.cube.storm.ui.model.property;

import android.os.Parcel;
import androidx.annotation.Nullable;

import com.cube.storm.ui.model.Model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Animation frame class that has an array of {@link com.cube.storm.ui.model.property.ImageProperty} and a delay used
 * in {@link com.cube.storm.ui.model.property.AnimationImageProperty}
 *
 * @author Callum Taylor
 * @project LightningUi
 */
@NoArgsConstructor @AllArgsConstructor
@Accessors(chain = true) @Data @EqualsAndHashCode(callSuper=false)
public class AnimationFrame extends Model
{
	protected ArrayList<ImageProperty> image;
	protected long delay;

	// Support for legacy spotlight image
	@Deprecated protected ArrayList<ImageProperty> src;

	@Nullable public ArrayList<ImageProperty> getImage()
	{
		if (image != null)
		{
			return image;
		}

		return src;
	}

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}
