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

package com.own.settings.owncenter.tabs;

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

	// Developers
	private String KEY_DEVELOPER_OWNDROID_PLUS_LINK = "developer_owndroid_plus_link";
	
	// Website Developers
	private String KEY_DEVELOPER_VICTOR_PLUS_LINK = "developer_victor_plus_link";

	// Designers
	private String KEY_DESIGNER_TJSTEVEMX_PLUS_LINK = "designer_tjstevemx_plus_link"


	// Developers
	private Preference mOwndroidPlusUrl;
	
	// Website Developers
	private Preference mVictorPlusUrl;
	
	// Designers
	private Preference mTjstevemxPlusUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ownrom_team);

		// Developers
		mOwndroidPlusUrl = findPreference(KEY_DEVELOPER_OWNDROID_PLUS_LINK);
		
		// Website Developers
		mVictorPlusUrl = findPreference(KEY_DEVELOPER_VICTOR_PLUS_LINK);
		
		// Designers
		mTjstevemxPlusUrl = findPreference(KEY_DESIGNER_TJSTEVEMX_PLUS_LINK);
        
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
		
		// Developers
        if (preference == mOwndroidPlusUrl) {
            launchUrl("https://plus.google.com/+MarkVisser10021991");
            
		// Website Developers
		} else if (preference == mVictorPlusUrl) {
            launchUrl("https://plus.google.com/+VictorLinfield");

		// Designers
		} else if (preference == mTjstevemxPlusUrl) {
            launchUrl("https://plus.google.com/107002554168576794353");
                        
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
