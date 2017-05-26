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

package com.own.settings.button.tabs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.own.settings.javas.DeviceUtils;
import com.own.settings.javas.TelephonyUtils;

import cyanogenmod.providers.CMSettings;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.DevelopmentSettings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.cyanogenmod.internal.util.ScreenType;

public class VolumeButton extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "VolumeButton";

    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
    private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
    private static final String KEY_VOLUME_CONTROL_RING_STREAM = "volume_keys_control_ring_stream";
    
    private static final String CATEGORY_VOLUME = "volume_keys";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_VOLUME = 0x40;

    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mVolumeWakeScreen;
    private SwitchPreference mVolumeMusicControls;
    private SwitchPreference mSwapVolumeButtons;

    private Handler mHandler;
      
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.button_volume_tab);
        
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);
                
        final boolean hasVolumeKeys = (deviceKeys & KEY_MASK_VOLUME) != 0;
        
        final boolean showVolumeWake = (deviceWakeKeys & KEY_MASK_VOLUME) != 0;
        
		boolean hasAnyBindableKey = false;
		
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);
                
        if (DeviceUtils.hasVolumeRocker(getActivity())) {
            if (!showVolumeWake) {
                volumeCategory.removePreference(findPreference(CMSettings.System.VOLUME_WAKE_SCREEN));
            }

            int cursorControlAction = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);

            int swapVolumeKeys = CMSettings.System.getInt(getContentResolver(),
                    CMSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
            mSwapVolumeButtons = (SwitchPreference)
                    prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
            if (mSwapVolumeButtons != null) {
                mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
            }
        } else {
            prefScreen.removePreference(volumeCategory);
        }
        
        mVolumeWakeScreen = (SwitchPreference) findPreference(CMSettings.System.VOLUME_WAKE_SCREEN);
        mVolumeMusicControls = (SwitchPreference) findPreference(KEY_VOLUME_MUSIC_CONTROLS);

        if (mVolumeWakeScreen != null) {
            if (mVolumeMusicControls != null) {
                mVolumeMusicControls.setDependency(CMSettings.System.VOLUME_WAKE_SCREEN);
                mVolumeWakeScreen.setDisableDependentsState(true);
            }
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        CMSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleSystemActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mVolumeKeyCursorControl) {
            handleSystemActionListChange(mVolumeKeyCursorControl, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
		}
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mSwapVolumeButtons) {
            int value = mSwapVolumeButtons.isChecked()
                    ? (ScreenType.isTablet(getActivity()) ? 2 : 1) : 0;
            CMSettings.System.putInt(getActivity().getContentResolver(),
                    CMSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }
    
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWN;
    }
}
