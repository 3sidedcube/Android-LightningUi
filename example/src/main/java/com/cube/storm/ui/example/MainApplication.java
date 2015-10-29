package com.cube.storm.ui.example;

import android.app.Application;
import android.net.Uri;

import com.cube.storm.UiSettings;
import com.cube.storm.ui.model.App;

public class MainApplication extends Application
{
	@Override public void onCreate()
	{
		super.onCreate();

		// Initiate settings
		UiSettings uiSettings = new UiSettings.Builder(this).build();

		// Loading app json
		String appUri = "assets://app.json";
		App app = UiSettings.getInstance().getViewBuilder().buildApp(Uri.parse(appUri));

		if (app != null)
		{
			UiSettings.getInstance().setApp(app);
		}
	}
}
