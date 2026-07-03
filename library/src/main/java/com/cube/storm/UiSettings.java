package com.cube.storm;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cube.storm.ui.controller.adapter.StormListAdapter;
import com.cube.storm.ui.data.ContentSize;
import com.cube.storm.ui.lib.EventHook;
import com.cube.storm.ui.lib.factory.FileFactory;
import com.cube.storm.ui.lib.factory.IntentFactory;
import com.cube.storm.ui.lib.handler.LinkHandler;
import com.cube.storm.ui.lib.helper.ViewHelper;
import com.cube.storm.ui.lib.parser.ViewBuilder;
import com.cube.storm.ui.lib.parser.ViewProcessor;
import com.cube.storm.ui.lib.processor.TextProcessor;
import com.cube.storm.ui.lib.provider.DefaultIntentProvider;
import com.cube.storm.ui.lib.provider.IntentProvider;
import com.cube.storm.ui.lib.resolver.AppResolver;
import com.cube.storm.ui.lib.resolver.IntentResolver;
import com.cube.storm.ui.lib.resolver.IntentResolverMap;
import com.cube.storm.ui.lib.resolver.ViewResolver;
import com.cube.storm.ui.lib.spec.ChevronSpec;
import com.cube.storm.ui.lib.spec.DividerSpec;
import com.cube.storm.ui.lib.spec.ListDividerSpec;
import com.cube.storm.ui.model.App;
import com.cube.storm.ui.model.Model;
import com.cube.storm.ui.model.descriptor.PageDescriptor;
import com.cube.storm.ui.model.list.ListItem;
import com.cube.storm.ui.model.list.collection.CollectionItem;
import com.cube.storm.ui.model.page.Page;
import com.cube.storm.ui.model.property.LinkProperty;
import com.cube.storm.ui.model.property.TextProperty;
import com.cube.storm.util.lib.processor.Processor;
import com.cube.storm.util.lib.resolver.AssetsResolver;
import com.cube.storm.util.lib.resolver.FileResolver;
import com.cube.storm.util.lib.resolver.Resolver;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the entry point class of the library. To enable the use of the library, you must instantiate
 * a new {@link UiSettings.Builder} object in your {@link android.app.Application} singleton class.
 *
 * This class should not be directly instantiated.
 *
 * @author Callum Taylor
 * @project LightningUi
 */
public class UiSettings
{
	/**
	 * The singleton instance of the settings
	 */
	private static UiSettings instance;

	/**
	 * Gets the instance of the {@link UiSettings} class
	 * Throws a {@link java.lang.IllegalAccessError} if the singleton has not been instantiated properly
	 *
	 * @return The instance
	 */
	public static UiSettings getInstance()
	{
		if (instance == null)
		{
			throw new IllegalAccessError("You must build the Ui settings object first using UiSettings$Builder");
		}

		return instance;
	}

	/**
	 * Default private constructor
	 */
	private UiSettings(){}

	/**
	 * App data for the content
	 */
	@Getter private App app;

	/**
	 * The central point of the library to work out how to link between pages based on page descriptors or Uris.
	 * ALWAYS call this to deal with resolving intents/fragments over {@link #intentProviders} as this implementation
	 * calls {@link #intentProviders}
	 */
	@Getter @Setter private IntentFactory intentFactory = new IntentFactory();

	/**
	 * List of providers for dealing with intents. Intent provider should return null in each method it doesnt consume.
	 * Intent providers are executed in order or list with user-overridden providers to be prioritised.
	 */
	@Getter @Setter private List<IntentProvider> intentProviders = new ArrayList<>();

	/**
	 * Intent resolver used to resolving specific pages to an fragment/intent using either a page ID/Uri/descriptor.
	 * Intents registered here are prioritised over default resolutions from {@link #intentFactory}
	 */
	@Getter @Setter private IntentResolverMap intentResolver = new IntentResolverMap();

	/**
	 * Factory class responsible for loading a file from disk based on its Uri
	 */
	@Getter @Setter private FileFactory fileFactory;

	/**
	 * The view processor map used by {@link com.cube.storm.ui.lib.parser.ViewBuilder}. Use {@link com.cube.storm.UiSettings.Builder#registerType(Type, com.cube.storm.ui.lib.parser.ViewProcessor)} to
	 * override the processor used to match models with json class names
	 */
	@Getter @Setter private Map<Type, ViewProcessor> viewProcessors = new LinkedHashMap<Type, ViewProcessor>(0);

	/**
	 * The density to use when loading images
	 */
	@Getter @Setter private ContentSize contentSize;

	/**
	 * The handler used when a link is triggered
	 */
	@Getter @Setter private LinkHandler linkHandler;

	/**
	 * The gson builder class used to build all of the storm objects from json/string/binary
	 */
	@Getter @Setter private ViewBuilder viewBuilder;

	/**
	 * Processor class used to process strings as part of {@link com.cube.storm.ui.model.property.TextProperty}
	 */
	@Getter @Setter private Processor<TextProperty, String> textProcessor;

	/**
	 * Uri resolver used to load a file based on it's protocol. You should not need to use this instance
	 * directly to load a file, instead use {@link com.cube.storm.ui.lib.factory.FileFactory} which uses this
	 * to resolve a file and load it. Only use this if you want to load a specific scheme
	 */
	@Getter @Setter private Map<String, Resolver> uriResolvers = new HashMap<String, Resolver>(2);

	/**
	 * Maps class names with a view resolver object to resolve models/viewholders.
	 */
	@Getter @Setter private Map<String, ViewResolver> viewResolvers = new HashMap<String, ViewResolver>(2);

	/**
	 * Default divider spec to use in {@link com.cube.storm.ui.controller.adapter.StormListAdapter}
	 */
	@Getter @Setter private DividerSpec dividerSpec;

	/**
	 * Registered hook classes for various events
	 */
	@Getter @Setter private ArrayList<EventHook> eventHooks = new ArrayList<>();

	/**
	 * Registered hook classes for various events
	 */
	@Getter @Setter private Class<? extends StormListAdapter> viewAdapter = StormListAdapter.class;

	/**
	 * Default language Uri
	 */
	@Getter @Setter private String defaultLanguageUri = "";
	
	/**
	 * Chevron spec to use for {@link com.cube.storm.ui.view.holder.list.StandardListItemViewHolder}s throughout the app
	 */
	@Getter @Setter private ChevronSpec chevronSpec;

	/**
	 * Sets the app model of the content
	 *
	 * @param app The new app model
	 */
	public void setApp(@NonNull App app)
	{
		this.app = app;
	}

	/**
	 * The builder class for {@link com.cube.storm.UiSettings}. Use this to create a new {@link com.cube.storm.UiSettings} instance
	 * with the customised properties specific for your project.
	 *
	 * Call {@link #build()} to build the settings object.
	 */
	public static class Builder
	{
		/**
		 * The temporary instance of the {@link UiSettings} object.
		 */
		private UiSettings construct;

		private Context context;

		/**
		 * Default constructor
		 */
		public Builder(Context context)
		{
			this.construct = new UiSettings();
			this.context = context.getApplicationContext();

			fileFactory(new FileFactory(){});
			linkHandler(new LinkHandler());
			textProcessor(new TextProcessor());

			contentSize(ContentSize.AUTO);

			// Register views and models
			registerViewResolver(ViewHelper.getViewResolvers());

			// Register view resolvers for Gson adapters
			ViewProcessor<? extends Model> baseProcessor = new ViewProcessor<Model>()
			{
				@Override public Class<? extends Model> getClassFromName(String name)
				{
					ViewResolver resolver = UiSettings.getInstance().getViewResolvers().get(name);

					if (resolver != null)
					{
						return resolver.resolveModel();
					}

					return null;
				}
			};

			registerType(Page.class, baseProcessor);
			registerType(ListItem.class, baseProcessor);
			registerType(CollectionItem.class, baseProcessor);
			registerType(LinkProperty.class, baseProcessor);

			registerUriResolver("file", new FileResolver());
			registerUriResolver("assets", new AssetsResolver(this.context));
			registerUriResolver("app", new AppResolver(this.context));

			viewBuilder(new ViewBuilder(){});
			dividerSpec(new ListDividerSpec());
			chevronSpec(ChevronSpec.noChevronSpec());
		}

		/**
		 * Sets the default {@link com.cube.storm.ui.lib.spec.DividerSpec} for the list adapter to use when layout out its children
		 *
		 * @param spec The new divider spec to use by default
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder dividerSpec(DividerSpec spec)
		{
			construct.dividerSpec = spec;
			return this;
		}

		/**
		 * Registers a page id/name to resolve to a specific intent resolver.
		 *
		 * @param pageId The id of the page. This will also match on a page's `name`
		 * @param resolver The intent resolver class
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerIntentResolver(String pageId, IntentResolver resolver)
		{
			construct.getIntentResolver().registerPageId(pageId, resolver);
			return this;
		}

		/**
		 * Registers a page URI to resolve to a specific intent resolver.
		 *
		 * @param pageUri The URI of the page.
		 * @param resolver The intent resolver class
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerIntentResolver(Uri pageUri, IntentResolver resolver)
		{
			construct.getIntentResolver().registerPageUri(pageUri, resolver);
			return this;
		}

		/**
		 * Registers a page descriptor to resolve to a specific intent resolver.
		 *
		 * @param pageDescriptor The descriptor of the page.
		 * @param resolver The intent resolver class
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerIntentResolver(PageDescriptor pageDescriptor, IntentResolver resolver)
		{
			construct.getIntentResolver().registerPageDescriptor(pageDescriptor, resolver);
			return this;
		}

		/**
		 * Adds an intent provider to the bottom of the provider list (lowest priority)
		 * @param provider
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerIntentProvider(IntentProvider provider)
		{
			construct.intentProviders.add(provider);
			return this;
		}

		/**
		 * Adds an intent provider to the start of the provider list (top-most priority)
		 * @param provider
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerIntentProviderStart(IntentProvider provider)
		{
			construct.intentProviders.add(0, provider);
			return this;
		}

		/**
		 * Sets the intent provider list
		 * @param provider The providers to set (in order)
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder setIntentProvider(IntentProvider... provider)
		{
			construct.intentProviders.clear();
			construct.intentProviders.addAll(Arrays.asList(provider));
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.ui.lib.factory.FileFactory} for the module
		 *
		 * @param fileFactory The new {@link com.cube.storm.ui.lib.factory.FileFactory}
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder fileFactory(FileFactory fileFactory)
		{
			construct.fileFactory = fileFactory;
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.ui.data.ContentSize} for the module
		 *
		 * @param contentSize The new {@link com.cube.storm.ui.data.ContentSize}
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder contentSize(ContentSize contentSize)
		{
			construct.contentSize = contentSize;
			return this;
		}

		/**
		 * Sets the default language srcUri
		 *
		 * @param languageUri The language uri of the default language
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder setDefaultLanguageUri(String languageUri)
		{
			construct.defaultLanguageUri = languageUri;
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.ui.lib.handler.LinkHandler} for the module
		 *
		 * @param linkHandler The new {@link com.cube.storm.ui.lib.handler.LinkHandler}
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder linkHandler(LinkHandler linkHandler)
		{
			construct.linkHandler = linkHandler;
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.ui.lib.parser.ViewBuilder} for the module
		 *
		 * @param viewBuilder The new {@link com.cube.storm.ui.lib.parser.ViewBuilder}
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder viewBuilder(ViewBuilder viewBuilder)
		{
			construct.viewBuilder = viewBuilder;
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.util.lib.processor.Processor} for the module
		 *
		 * @param textProcessor The new {@link com.cube.storm.util.lib.processor.Processor}
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder textProcessor(Processor<TextProperty, String> textProcessor)
		{
			construct.textProcessor = textProcessor;
			return this;
		}

		/**
		 * Registers a view resolver for matching class name with model and viewholder. Use this method to set what class
		 * gets used for a specific view/model.
		 *
		 * @param viewName The name of the view to register
		 * @param resolver The view resolver class
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerViewResolver(String viewName, ViewResolver resolver)
		{
			construct.viewResolvers.put(viewName, resolver);
			return this;
		}

		/**
		 * Registers a view resolver for matching class name with model and viewholder. Use this method to set what class
		 * gets used for a specific view/model.
		 *
		 * @param resolvers The map of view resolver classes
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerViewResolver(Map<String, ViewResolver> resolvers)
		{
			construct.viewResolvers.putAll(resolvers);
			return this;
		}

		/**
		 * Registers a deserializer type for a class instance. Use this method to override what processor
		 * gets used for a specific view type.
		 *
		 * @param instanceClass The class to register for deserialization
		 * @param deserializer The processor class
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerType(Type instanceClass, ViewProcessor deserializer)
		{
			construct.viewProcessors.put(instanceClass, deserializer);
			return this;
		}

		/**
		 * Registers a uri resolver to use in the {@link com.cube.storm.ui.lib.factory.FileFactory}
		 *
		 * @param protocol The string protocol to register
		 * @param resolver The resolver to use for the registered protocol
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerUriResolver(String protocol, Resolver resolver)
		{
			construct.uriResolvers.put(protocol, resolver);
			return this;
		}

		/**
		 * Registers a uri resolvers
		 *
		 * @param resolvers The map of resolvers to register
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerUriResolver(Map<String, Resolver> resolvers)
		{
			construct.uriResolvers.putAll(resolvers);
			return this;
		}

		/**
		 * Registers an event hook class for various events
		 *
		 * @param hook The hook to register
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder registerEventHook(@NonNull EventHook hook)
		{
			construct.getEventHooks().add(hook);

			return this;
		}

		/**
		 * Sets the default class to use for storm list adapter
		 *
		 * @param adapterClass The class to use for list adapters
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder viewAdapter(@NonNull Class<? extends StormListAdapter> adapterClass)
		{
			construct.viewAdapter = adapterClass;

			return this;
		}

		/**
		 * Sets the app to display chevrons on StandardListItemView if the link matches the correct criteria
		 *
		 * If not set, StandardListItemView will not display with chevrons
		 *
		 * @return The {@link com.cube.storm.UiSettings.Builder} instance for chaining
		 */
		public Builder chevronSpec(@NonNull ChevronSpec spec)
		{
			construct.chevronSpec = spec;
			return this;
		}

		/**
		 * Builds the final settings object and sets its instance. Use {@link #getInstance()} to retrieve the settings
		 * instance.
		 *
		 * @return The newly set {@link com.cube.storm.UiSettings} instance
		 */
		public UiSettings build()
		{
			if (construct.getIntentProviders().size() == 0)
			{
				registerIntentProvider(new DefaultIntentProvider());
			}

			return (UiSettings.instance = construct);
		}
	}
}
