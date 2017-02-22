package com.cube.storm.ui.lib.parser;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.model.App;
import com.cube.storm.ui.model.page.Page;
import com.cube.storm.ui.model.page.TabbedPageCollection;
import com.cube.storm.ui.model.property.TextProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;

/**
 * View parser used to process the json files into models to be used with the list/grid adapters
 *
 * @author Callum Taylor
 * @project LightningUi
 */
public abstract class ViewBuilder
{
	private static Gson viewBuilder;

	/**
	 * Required to include view overrides
	 */
	public void rebuild()
	{
		viewBuilder = null;
		getGson();
	}

	/**
	 * Gets the gson object with the registered storm view type adapters. Use {@link #build(com.google.gson.JsonElement, Class)} or
	 * {@link #build(String, Class)} to build your page/view objects.
	 *
	 * @return The gson object
	 */
	public Gson getGson()
	{
		if (viewBuilder == null)
		{
			GsonBuilder builder = new GsonBuilder();

			for (Type instanceClass : UiSettings.getInstance().getViewProcessors().keySet())
			{
				builder.registerTypeAdapter(instanceClass, UiSettings.getInstance().getViewProcessors().get(instanceClass));
			}

			builder.registerTypeAdapter(TextProperty.class, new JsonDeserializer<TextProperty>()
			{
				@Override public TextProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
				{
					JsonElement content = json.getAsJsonObject().get("content");
					if (content.isJsonPrimitive())
					{
						TextProperty text = new TextProperty();
						text.setContent(new HashMap<String, String>());
						text.getContent().put(Locale.getDefault().getLanguage(), content.getAsString());

						return text;
					}
					else
					{
						return new Gson().fromJson(json, TextProperty.class);
					}
				}
			});

			viewBuilder = builder.create();
		}

		return viewBuilder;
	}

	/**
	 * Builds an App object from a file Uri
	 *
	 * @param fileUri The file Uri to load from
	 *
	 * @return The app data or null
	 */
	@Nullable
	public App buildApp(@NonNull Uri fileUri)
	{
		InputStream appData = UiSettings.getInstance().getFileFactory().loadFromUri(fileUri);

		if (appData != null)
		{
			return build(appData, App.class);
		}

		return null;
	}

	/**
	 * Builds an App object from a json element
	 *
	 * @param app The json element page data
	 *
	 * @return The app data, or null
	 */
	@Nullable
	public App buildApp(@NonNull byte[] app)
	{
		try
		{
			return build(new String(app, "UTF-8"), App.class);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds a Page object from a file Uri
	 *
	 * @param fileUri The file Uri to load from
	 *
	 * @return The page data or null
	 */
	@Nullable
	public Page buildPage(@NonNull Uri fileUri)
	{
		InputStream pageData = UiSettings.getInstance().getFileFactory().loadFromUri(fileUri);

		if (pageData != null)
		{
			return build(pageData, Page.class);
		}

		return null;
	}

	/**
	 * Builds a page object from a byte array json string
	 *
	 * @param page The byte array json string page data
	 *
	 * @return The page data, or null
	 */
	@Nullable
	public Page buildPage(@NonNull byte[] page)
	{
		try
		{
			return build(new String(page, "UTF-8"), Page.class);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds a page object from a json string
	 *
	 * @param page The json string page data
	 *
	 * @return The page data, or null
	 */
	@Nullable
	public Page buildPage(@NonNull String page)
	{
		return build(page, Page.class);
	}

	/**
	 * Builds a page object from a json element
	 *
	 * @param page The json element page data
	 *
	 * @return The page data, or null
	 */
	@Nullable
	public Page buildPage(@NonNull JsonElement page)
	{
		return build(page, Page.class);
	}

	/**
	 * Builds a Page object from a file Uri
	 *
	 * @param fileUri The file Uri to load from
	 *
	 * @return The page data or null
	 */
	@Nullable
	public TabbedPageCollection buildTabbedPage(@NonNull Uri fileUri)
	{
		InputStream pageData = UiSettings.getInstance().getFileFactory().loadFromUri(fileUri);

		if (pageData != null)
		{
			return build(pageData, TabbedPageCollection.class);
		}

		return null;
	}

	/**
	 * Builds a page object from a byte array json string
	 *
	 * @param page The byte array json string page data
	 *
	 * @return The page data, or null
	 */
	@Nullable
	public TabbedPageCollection buildTabbedPage(@NonNull byte[] page)
	{
		try
		{
			return build(new String(page, "UTF-8"), TabbedPageCollection.class);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds a page object from a json string
	 *
	 * @param page The json string page data
	 *
	 * @return The page data, or null
	 */
	@Nullable
	public TabbedPageCollection buildTabbedPage(@NonNull String page)
	{
		return build(page, TabbedPageCollection.class);
	}

	/**
	 * Builds a page object from a json element
	 *
	 * @param page The json element page data
	 *
	 * @return The page data, or null
	 */
	@Nullable
	public TabbedPageCollection buildTabbedPage(@NonNull JsonElement page)
	{
		return build(page, TabbedPageCollection.class);
	}

	/**
	 * Builds a class from a json string input
	 *
	 * @param input The json input stream to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(String input, Class<T> outClass)
	{
		return outClass.cast(getGson().fromJson(input, outClass));
	}

	/**
	 * Builds a class from a json string input
	 *
	 * @param stream The json input stream to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(InputStream stream, Class<T> outClass)
	{
		try
		{
			if (stream != null)
			{
				return outClass.cast(getGson().fromJson(new InputStreamReader(new BufferedInputStream(stream, 8192)), outClass));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds a class from a json element input
	 *
	 * @param input The json element input to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(JsonElement input, Class<T> outClass)
	{
		return outClass.cast(getGson().fromJson(input, outClass));
	}

	/**
	 * Builds a class from a json string input
	 *
	 * @param stream The json input stream to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(InputStream stream, Type outClass)
	{
		try
		{
			if (stream != null)
			{
				return getGson().fromJson(new InputStreamReader(new BufferedInputStream(stream, 8192)), outClass);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds a class from a json string input
	 *
	 * @param input The json string input to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(String input, Type outClass)
	{
		return getGson().fromJson(input, outClass);
	}

	/**
	 * Builds a class from a json element input
	 *
	 * @param input The json element input to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(JsonElement input, Type outClass)
	{
		return getGson().fromJson(input, outClass);
	}
}
