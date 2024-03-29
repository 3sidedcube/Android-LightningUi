package com.cube.storm.ui.model;

import android.net.Uri;
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cube.storm.ui.model.descriptor.PageDescriptor;
import com.cube.storm.ui.model.page.Page;

import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * App class is a global data set which defines the content in the module handled by StormContent.
 *
 * This class is used when loading pages or finding out what type a page is before loading it to prevent
 * having to load up a whole file to decide what to do with it. This is primarily used with {@link com.cube.storm.ui.lib.factory.IntentFactory}
 * to decide what fragment/activity to load for a page Uri or name.
 * <br />
 * This class also has a vector Uri which tells you what the first page should be when starting the app.
 * <br />
 * You must load/create this class in your application singleton to prevent it from being cleared
 * from memory whilst the app is still open.
 * <br /><br />
 * Example load
 * <pre>
 * {@code
 * String appUri = "assets://app.json"; // This can be anything, it is loaded and dealt with by the FileFactory defined in UiSettings
 * App app = UiSettings.getInstance().getViewBuilder().buildApp(Uri.parse(appUri));
 *
 * if (app != null)
 * {
 * 	UiSettings.getInstance().setApp(app);
 * }
 * }
 * </pre>
 *
 * @author Callum Taylor
 * @project LightningUi
 */
@NoArgsConstructor @AllArgsConstructor
@Accessors(chain = true) @Data @EqualsAndHashCode(callSuper=false)
public class App extends Model
{
	protected String vector;
	protected String pack;
	protected Collection<PageDescriptor> map;

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{

	}

	@Nullable
	public PageDescriptor findPageDescriptor(@NonNull Uri page)
	{
		for (PageDescriptor pageDescriptor : getMap())
		{
			if (page.toString().equals(pageDescriptor.getSrc()))
			{
				return pageDescriptor;
			}
		}

		return null;
	}

	@Nullable
	public PageDescriptor findPageDescriptor(@NonNull Page page)
	{
		for (PageDescriptor pageDescriptor : getMap())
		{
			// TODO: Server needs to return IDs for page descriptors in app.json
			if (pageDescriptor.getId() != null && pageDescriptor.getId().equals(page.getId()))
			{
				return pageDescriptor;
			}
		}

		return null;
	}

	@Nullable
	public PageDescriptor findPageDescriptor(@NonNull String nameOrId)
	{
		for (PageDescriptor pageDescriptor : getMap())
		{
			if (pageDescriptor.getName() != null && pageDescriptor.getName().equals(nameOrId))
			{
				return pageDescriptor;
			}
			else if (pageDescriptor.getId() != null && pageDescriptor.getId().equalsIgnoreCase(nameOrId))
			{
				return pageDescriptor;
			}
		}

		return null;
	}
}
