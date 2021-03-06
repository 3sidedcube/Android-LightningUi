package com.cube.storm.ui.model.property;

import android.os.Parcel;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Text property class. This class has a content string which can either be a language coded string
 * or a string. If a language manager is set, the string is replaced with it's lookup equivalent from
 * the language manager, else it is left. Do not set a language manager to disable this behaviour.
 *
 * @author Callum Taylor
 * @project LightningUi
 */
@NoArgsConstructor @AllArgsConstructor
@Accessors(chain = true) @Data @EqualsAndHashCode(callSuper=false)
public class TextProperty extends Property
{
	public static String CLASS_NAME = "Text";

	{ this.className = CLASS_NAME; }

	protected Map<String, String> content;

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}
}
