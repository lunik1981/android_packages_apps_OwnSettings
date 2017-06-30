/*
 * Copyright (C) 2015-2017 The OwnROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class OTA extends SettingsPreferenceFragment {

    public static final String TAG = "OTA";

    private String KEY_OWNROM_WEBSITE = "ownrom_website" ;
    private String KEY_OWNROM_WALLS = "ownrom_walls" ;
    private String KEY_OWNROM_SOURCE = "ownrom_source";
    private String KEY_OWNROM_GPLUS = "ownrom_google_plus";
	private String KEY_OWNROM_FACEBOOK = "ownrom_facebook";
	private String KEY_OWNROM_TELEGRAM = "ownrom_telegram";
	private String KEY_OWNROM_TRANSLATIONS = "ownrom_translations";
    private String KEY_OWNROM_SHARE = "ownrom_share";
    private String KEY_OWNROM_DONATE = "ownrom_donate";

    private Preference mWebsiteUrl;
    private Preference mWallsUrl;
    private Preference mSourceUrl;
    private Preference mGoogleUrl;
    private Preference mFacebookUrl;
    private Preference mTelegramUrl;
    private Preference mTranslationsUrl;
    private Preference mShare;
    private Preference mDonateUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.owncenter_ota_tab);

        mWebsiteUrl = findPreference(KEY_OWNROM_WEBSITE);
        mWallsUrl = findPreference(KEY_OWNROM_WALLS);
        mSourceUrl = findPreference(KEY_OWNROM_SOURCE);
        mGoogleUrl = findPreference(KEY_OWNROM_GPLUS);
        mFacebookUrl = findPreference(KEY_OWNROM_FACEBOOK);
        mTelegramUrl = findPreference(KEY_OWNROM_TELEGRAM);
        mTranslationsUrl = findPreference(KEY_OWNROM_TRANSLATIONS);
        mShare = findPreference(KEY_OWNROM_SHARE);
        mDonateUrl = findPreference(KEY_OWNROM_DONATE);
        
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
		if (preference == mWebsiteUrl) {
			launchUrl("https://ownrom.netlify.com");
		} else if (preference == mWallsUrl) {
			launchUrl("https://drive.google.com/open?id=0ByxXWLUoTqcveHlfOHd2ZUlENW8");
        } else if (preference == mSourceUrl) {
            launchUrl("https://github.com/OwnROM");
        } else if (preference == mGoogleUrl) {
            launchUrl("https://plus.google.com/communities/108869588356214314591");
        } else if (preference == mFacebookUrl) {
            launchUrl("https://www.facebook.com/OwnDroid.nl/");
        } else if (preference == mTelegramUrl) {
            launchUrl("https://t.me/OwnROM");
        } else if (preference == mTranslationsUrl) {
            launchUrl("https://crowdin.com/project/ownsettings");
        } else if (preference == mShare) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, String.format(
                    getActivity().getString(R.string.share_message), Build.MODEL));
            startActivity(Intent.createChooser(intent, getActivity().getString(R.string.share_chooser_title)));
        } else if (preference == mDonateUrl) {
            launchUrl("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3XYRGG4EYLWNL");
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
