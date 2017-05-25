/*
 * Copyright (C) 2015-2017 The OwnROM Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compiance with the License.
 * You may obtain a copy of the license at
 * 
 * http://www.apache.org.licenses/LICENSE-2.0
 * 
 * Unles required by applicable law or agreed to in writing, software
 * distributed uder the License is destributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and 
 * limitations under the Lincense.
 */

package com.own.settings.statusbar.tabs;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.ContentResolver;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.provider.Settings;
import android.os.UserHandle;
import android.view.View;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import cyanogenmod.preference.CMSystemSettingListPreference;

public class Clock extends SettingsPreferenceFragment implements
		OnPreferenceChangeListener {
			
	private static final String TAG = "Clock";
	
	private static final String STATUS_BAR_CLOCK_POSITION = "status_bar_clock";
	private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
	
	private CMSystemSettingListPreference mStatusBarClock;
	private CMSystemSettingListPreference mStatusBarAmPm;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.statusbar_clock_tab);
		
		mStatusBarClock = (CMSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_POSITION);
		mStatusBarAmPm = (CMSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
		
		if (DateFormat.is24HourFormat(getActivity())) {
			mStatusBarAmPm.setEnabled(false);
			mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
		}

	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Adjust clock position for RTL if necessary
		Configuration config = getResources().getConfiguration();
		if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
			mStatusBarClock.setEntries(getActivity().getResources().getStringArray(
				R.array.status_bar_clock_position_entries_rtl));
			mStatusBarClock.setSummary(mStatusBarClock.getEntry());
		}
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object objValue) {
		return false;
	}
	
	@Override
	protected int getMetricsCategory() {
		return MetricsEvent.OWN;
	}
}
