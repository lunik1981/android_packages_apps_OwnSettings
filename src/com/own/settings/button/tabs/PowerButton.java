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
import android.content.pm.UserInfo;
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

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.DevelopmentSettings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.own.settings.javas.DeviceUtils;
import com.own.settings.javas.TelephonyUtils;

import cyanogenmod.providers.CMSettings;
import com.android.internal.util.cm.PowerMenuConstants;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.cyanogenmod.internal.util.ScreenType;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_AIRPLANE;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_ASSIST;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_LOCKDOWN;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_RESTART;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_SCREENSHOT;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_SETTINGS;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_SILENT;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_USERS;
import static com.android.internal.util.cm.PowerMenuConstants.GLOBAL_ACTION_KEY_VOICEASSIST;

public class PowerButton extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "PowerButton";
    
    private static final String KEY_POWER_END_CALL = "power_end_call";
    private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";
    
    private static final String CATEGORY_POWER = "power_key";

    private SwitchPreference mPowerEndCall;
    private SwitchPreference mCameraDoubleTapPowerGesture;
    private CheckBoxPreference mRebootPref;
    private CheckBoxPreference mScreenshotPref;
    private CheckBoxPreference mAirplanePref;
    private CheckBoxPreference mUsersPref;
    private CheckBoxPreference mSettingsPref;
    private CheckBoxPreference mLockdownPref;
    private CheckBoxPreference mSilentPref;
    private CheckBoxPreference mVoiceAssistPref;
    private CheckBoxPreference mAssistPref;
    
    private Handler mHandler;

    Context mContext;
    private ArrayList<String> mLocalUserConfig = new ArrayList<String>();
    private String[] mAvailableActions;
    private String[] mAllActions;
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.button_power_tab);
		mContext = getActivity().getApplicationContext();
		
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        mAvailableActions = getActivity().getResources().getStringArray(
                R.array.power_menu_actions_array);
        mAllActions = PowerMenuConstants.getAllActions();

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);
                
                
       final boolean hasPowerKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);
       
        boolean hasAnyBindableKey = false;
        final PreferenceCategory powerCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_POWER);

        for (String action : mAllActions) {
            // Remove preferences not present in the overlay
            if (!isActionAllowed(action)) {
                if (prefScreen.findPreference(action) != null) {
                    prefScreen.removePreference(prefScreen.findPreference(action));
                }
                continue;
            }
            
        // Power button ends calls.
        mPowerEndCall = (SwitchPreference) findPreference(KEY_POWER_END_CALL);

        // Double press power to launch camera.
        mCameraDoubleTapPowerGesture
                    = (SwitchPreference) findPreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
                    
        mHandler = new Handler();
        
        if (hasPowerKey) {
            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                powerCategory.removePreference(mPowerEndCall);
                mPowerEndCall = null;
            }
            if (mCameraDoubleTapPowerGesture != null &&
                    isCameraDoubleTapPowerGestureAvailable(getResources())) {
                // Update double tap power to launch camera if available.
                mCameraDoubleTapPowerGesture.setOnPreferenceChangeListener(this);
                int cameraDoubleTapPowerDisabled = Settings.Secure.getInt(
                        getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
                mCameraDoubleTapPowerGesture.setChecked(cameraDoubleTapPowerDisabled == 0);                                        
            } else {
                powerCategory.removePreference(mCameraDoubleTapPowerGesture);
                mCameraDoubleTapPowerGesture = null;
            }
        } else {
            prefScreen.removePreference(powerCategory);
        }

            if (action.equals(GLOBAL_ACTION_KEY_RESTART)) {
                mRebootPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_RESTART);
                mRebootPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_SCREENSHOT)) {
                mScreenshotPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_SCREENSHOT);
                mScreenshotPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_AIRPLANE)) {
                mAirplanePref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_AIRPLANE);
                mAirplanePref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_USERS)) {
                mUsersPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_USERS);
                mUsersPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_SETTINGS)) {
                mSettingsPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_SETTINGS);
                mSettingsPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_LOCKDOWN)) {
                mLockdownPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_LOCKDOWN);
                mLockdownPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_SILENT)) {
                mSilentPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_SILENT);
                mSilentPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_VOICEASSIST)) {
                mVoiceAssistPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_VOICEASSIST);
                mVoiceAssistPref.setOnPreferenceChangeListener(this);
            } else if (action.equals(GLOBAL_ACTION_KEY_ASSIST)) {
                mAssistPref = (CheckBoxPreference) prefScreen.findPreference(GLOBAL_ACTION_KEY_ASSIST);
                mAssistPref.setOnPreferenceChangeListener(this);
			}
		}
        getUserConfig();       	
	}

    @Override
    public void onResume() {
        super.onResume();

        // Power button ends calls.
        if (mPowerEndCall != null) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mPowerEndCall.setChecked(powerButtonEndsCall);
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
    public void onStart() {
        super.onStart();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        if (mRebootPref != null) {
            mRebootPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_RESTART));
        }

        if (mScreenshotPref != null) {
            mScreenshotPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SCREENSHOT));
        }

        if (mAirplanePref != null) {
            mAirplanePref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_AIRPLANE));
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                if (prefScreen.findPreference(GLOBAL_ACTION_KEY_USERS) != null) {
                    prefScreen.removePreference(prefScreen.findPreference(GLOBAL_ACTION_KEY_USERS));
                }
                mUsersPref = null;
            } else {
                List<UserInfo> users = ((UserManager) mContext.getSystemService(
                        Context.USER_SERVICE)).getUsers();
                boolean enabled = (users.size() > 1);
                mUsersPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_USERS) && enabled);
                mUsersPref.setEnabled(enabled);
            }
        }

        if (mSettingsPref != null) {
            mSettingsPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SETTINGS));
        }

        if (mLockdownPref != null) {
            mLockdownPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_LOCKDOWN));
        }

        if (mSilentPref != null) {
            mSilentPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_SILENT));
        }

        if (mVoiceAssistPref != null) {
            mVoiceAssistPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_VOICEASSIST));
        }

        if (mAssistPref != null) {
            mAssistPref.setChecked(settingsArrayContains(GLOBAL_ACTION_KEY_ASSIST));
        }

    }
                	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean value = newValue == null ? false : (boolean) newValue;
		
		if (preference == mCameraDoubleTapPowerGesture) {
            Settings.Secure.putInt(getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
             value ? 0 : 1 /* Backwards because setting is for disabling */);
        } else if (preference == mRebootPref) {
            mRebootPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_RESTART);
        } else if (preference == mScreenshotPref) {
            mScreenshotPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);
        } else if (preference == mAirplanePref) {
            mAirplanePref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);
        } else if (preference == mUsersPref) {
            mUsersPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);
        } else if (preference == mSettingsPref) {
            mSettingsPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_SETTINGS);
        } else if (preference == mLockdownPref) {
            mLockdownPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_LOCKDOWN);
        } else if (preference == mSilentPref) {
            mSilentPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_SILENT);
        } else if (preference == mVoiceAssistPref) {
            mVoiceAssistPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_VOICEASSIST);
        } else if (preference == mAssistPref) {
            mAssistPref.setChecked(value);
            updateUserConfig(value, GLOBAL_ACTION_KEY_ASSIST);      
        } else {
			return false;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
		if (preference == mPowerEndCall) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        }
        
        return super.onPreferenceTreeClick(preference);
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }
    
    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }
    private boolean settingsArrayContains(String preference) {
        return mLocalUserConfig.contains(preference);
    }

    private boolean isActionAllowed(String action) {
        if (Arrays.asList(mAvailableActions).contains(action)) {
            return true;
        }
        return false;
    }

    private void updateUserConfig(boolean enabled, String action) {
        if (enabled) {
            if (!settingsArrayContains(action)) {
                mLocalUserConfig.add(action);
            }
        } else {
            if (settingsArrayContains(action)) {
                mLocalUserConfig.remove(action);
            }
        }
        saveUserConfig();
    }

    private void getUserConfig() {
        mLocalUserConfig.clear();
        String[] defaultActions;
        String savedActions = CMSettings.Secure.getStringForUser(mContext.getContentResolver(),
                CMSettings.Secure.POWER_MENU_ACTIONS, UserHandle.USER_CURRENT);

        if (savedActions == null) {
            defaultActions = mContext.getResources().getStringArray(
                    com.android.internal.R.array.config_globalActionsList);
            for (String action : defaultActions) {
                mLocalUserConfig.add(action);
            }
        } else {
            for (String action : savedActions.split("\\|")) {
                mLocalUserConfig.add(action);
            }
        }
    }

    private void saveUserConfig() {
        StringBuilder s = new StringBuilder();

        // TODO: Use DragSortListView
        ArrayList<String> setactions = new ArrayList<String>();
        for (String action : mAllActions) {
            if (settingsArrayContains(action) && isActionAllowed(action)) {
                setactions.add(action);
            } else {
                continue;
            }
        }

        for (int i = 0; i < setactions.size(); i++) {
            s.append(setactions.get(i).toString());
            if (i != setactions.size() - 1) {
                s.append("|");
            }
        }

        CMSettings.Secure.putStringForUser(getContentResolver(),
                CMSettings.Secure.POWER_MENU_ACTIONS, s.toString(), UserHandle.USER_CURRENT);
        updatePowerMenuDialog();
    }

    private void updatePowerMenuDialog() {
        Intent u = new Intent();
        u.setAction(Intent.UPDATE_POWER_MENU);
        mContext.sendBroadcastAsUser(u, UserHandle.ALL);
    }
    
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWN;
    }
}

