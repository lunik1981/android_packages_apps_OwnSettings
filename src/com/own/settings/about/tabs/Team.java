/*
 * Copyright (C) 2015-2017 The OwnROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.own.settings.about.tabs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Team extends SettingsPreferenceFragment {
	
	public static final String TAG = "team";

	//Developers Erea
	private String KEY_DEVELOPER_OWNDROID_PLUS_LINK = "developer_owndroid_plus_link"
	
	//Maintainers Erea
	private String KEY_ANGLER_MAINTAINER_PLUS_LINK = "angler_maintainer_plus_link";

	//Developers Erea
	private Preference mOwndroidPlusUrl;
	
	//Maintainers Erea
	private Preference mAnglerPlusUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ownrom_team);

		//Developers Erea
		mOwndroidPlusUrl = findPreference(KEY_DEVELOPER_OWNDROID_PLUS_LINK);

		//Maintainers Erea
        mAnglerPlusUrl = findPreference(KEY_ANGLER_MAINTAINER_PLUS_LINK);
        
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
		//Developers Erea
        if (preference == mOwndroidPlusUrl) {
            launchUrl("https://plus.google.com/+MarkVisser10021991");
		//Maintainers Erea
        } else if (preference == mAnglerPlusUrl) {
            launchUrl("https://plus.google.com/+MarkVisser10021991");
        }
        return super.onPreferenceTreeClick(preference);
    }


    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(intent);
    }

               
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWN;
     }
}
