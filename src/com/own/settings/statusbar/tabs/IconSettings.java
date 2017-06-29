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

package com.own.settings.statusbar.tabs;

 import android.os.Bundle;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.content.ContentResolver;
 import android.content.res.Resources;
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

public class IconSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;

    private CMSystemSettingListPreference mStatusBarBattery;
    private CMSystemSettingListPreference mStatusBarBatteryShowPercent;
    private ListPreference mTextChargingSymbol;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_icons_tab);
        
        mStatusBarBattery = (CMSystemSettingListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(0));
        
        mTextChargingSymbol = (ListPreference) findPreference(TEXT_CHARGING_SYMBOL);
        int textChargingSymbolValue = Settings.Secure.getInt(resolver,
                Settings.Secure.TEXT_CHARGING_SYMBOL, 0);
        mTextChargingSymbol.setValue(Integer.toString(textChargingSymbolValue));
        mTextChargingSymbol.setSummary(mTextChargingSymbol.getEntry());
        mTextChargingSymbol.setOnPreferenceChangeListener(this);


    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mTextChargingSymbol) {
            int val = Integer.parseInt((String) newValue);
            int index = mTextChargingSymbol.findIndexOfValue((String) newValue);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.TEXT_CHARGING_SYMBOL, val);
            mTextChargingSymbol.setSummary(mTextChargingSymbol.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN) {
            mStatusBarBatteryShowPercent.setEnabled(false);
            mQsBatteryTitle.setEnabled(false);
            mTextChargingSymbol.setEnabled(false);
        } else if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
            mTextChargingSymbol.setEnabled(true);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
            mTextChargingSymbol.setEnabled(false);
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWN;
    }
}
