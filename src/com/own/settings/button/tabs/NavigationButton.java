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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
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

import java.util.List;
import java.util.ArrayList;

import org.cyanogenmod.internal.util.ScreenType;

import com.android.internal.utils.du.ActionConstants;
import com.android.internal.utils.du.Config;
import com.android.internal.utils.du.DUActionUtils;
import com.android.internal.utils.du.Config.ButtonConfig;

import com.own.settings.javas.SeekBarPreference;

public class NavigationButton extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavigationButton";

    private static final String NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String KEY_NAVBAR_MODE = "navbar_mode";
    private static final String KEY_FLING_NAVBAR_SETTINGS = "fling_settings";
    private static final String KEY_CATEGORY_NAVIGATION_INTERFACE = "category_navbar_interface";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_SMARTBAR_SETTINGS = "smartbar_settings";
    private static final String KEY_NAVIGATION_HEIGHT_PORT = "navbar_height_portrait";
    private static final String KEY_NAVIGATION_HEIGHT_LAND = "navbar_height_landscape";
    private static final String KEY_NAVIGATION_WIDTH = "navbar_width";

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
    private static final String KEY_HOME_ANSWER_CALL = "home_answer_call";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_CAMERA = "camera_key";
    private static final String CATEGORY_BACKLIGHT = "key_backlight";

    // Available custom actions to perform on a key press.
    // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
    // frameworks/base/core/java/android/provider/Settings.java
    private enum Action {
        NOTHING,
        MENU,
        APP_SWITCH,
        SEARCH,
        VOICE_SEARCH,
        IN_APP_SEARCH,
        LAUNCH_CAMERA,
        SLEEP,
        LAST_APP,
        SPLIT_SCREEN;
        
        public static Action fromIntSafe(int id) {
            if (id < NOTHING.ordinal() || id > Action.values().length) {
                return NOTHING;
            }
            return Action.values()[id];
        }

        public static Action fromSettings(ContentResolver cr, String setting, Action def) {
            return fromIntSafe(CMSettings.System.getInt(cr, setting, def.ordinal()));
        }
    }

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private SwitchPreference mNavbarVisibility;
    private ListPreference mNavbarMode;
    private PreferenceScreen mFlingSettings;
    private PreferenceCategory mNavInterface;
    private PreferenceCategory mNavGeneral;
    private PreferenceScreen mSmartbarSettings;
    private SeekBarPreference mBarHeightPort;
    private SeekBarPreference mBarHeightLand;
    private SeekBarPreference mBarWidth;

    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private SwitchPreference mCameraWakeScreen;
    private SwitchPreference mCameraSleepOnRelease;
    private SwitchPreference mCameraLaunch;
    private SwitchPreference mHomeAnswerCall;

    private Handler mHandler;
                
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.button_navigation_tab);
        
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mNavInterface = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_INTERFACE);
        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
        mNavbarVisibility = (SwitchPreference) findPreference(NAVBAR_VISIBILITY);
        mNavbarMode = (ListPreference) findPreference(KEY_NAVBAR_MODE);
        mFlingSettings = (PreferenceScreen) findPreference(KEY_FLING_NAVBAR_SETTINGS);
        mSmartbarSettings = (PreferenceScreen) findPreference(KEY_SMARTBAR_SETTINGS);

        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);

        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;

        final boolean showHomeWake = (deviceWakeKeys & KEY_MASK_HOME) != 0;
        final boolean showBackWake = (deviceWakeKeys & KEY_MASK_BACK) != 0;
        final boolean showMenuWake = (deviceWakeKeys & KEY_MASK_MENU) != 0;
        final boolean showAssistWake = (deviceWakeKeys & KEY_MASK_ASSIST) != 0;
        final boolean showAppSwitchWake = (deviceWakeKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean showCameraWake = (deviceWakeKeys & KEY_MASK_CAMERA) != 0;

        boolean showing = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_VISIBLE,
                DUActionUtils.hasNavbarByDefault(getActivity()) ? 1 : 0) != 0;
        updateBarVisibleAndUpdatePrefs(showing);
        mNavbarVisibility.setOnPreferenceChangeListener(this);

        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE,
                0);

        updateBarModeSettings(mode);
        mNavbarMode.setOnPreferenceChangeListener(this);

        int size = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_HEIGHT, 80, UserHandle.USER_CURRENT);
        mBarHeightPort = (SeekBarPreference) findPreference(KEY_NAVIGATION_HEIGHT_PORT);
        mBarHeightPort.setValue(size);
        mBarHeightPort.setOnPreferenceChangeListener(this);

        final boolean canMove = DUActionUtils.navigationBarCanMove();
        if (canMove) {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_HEIGHT_LAND));
            size = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_WIDTH, 80, UserHandle.USER_CURRENT);
            mBarWidth = (SeekBarPreference) findPreference(KEY_NAVIGATION_WIDTH);
            mBarWidth.setValue(size);
            mBarWidth.setOnPreferenceChangeListener(this);
        } else {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_WIDTH));
            size = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT_LANDSCAPE, 80, UserHandle.USER_CURRENT);
            mBarHeightLand = (SeekBarPreference) findPreference(KEY_NAVIGATION_HEIGHT_LAND);
            mBarHeightLand.setValue(size);
            mBarHeightLand.setOnPreferenceChangeListener(this);
        }

        boolean hasAnyBindableKey = false;
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory backlightCategory =
				(PreferenceCategory) prefScreen.findPreference(CATEGORY_BACKLIGHT);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
        final PreferenceCategory cameraCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_CAMERA);
                
        // Home button answers calls.
        mHomeAnswerCall = (SwitchPreference) findPreference(KEY_HOME_ANSWER_CALL);

        mHandler = new Handler();
        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(CMSettings.System.HOME_WAKE_SCREEN));
            }

            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                homeCategory.removePreference(mHomeAnswerCall);
                mHomeAnswerCall = null;
            }

            Action defaultLongPressAction = Action.fromIntSafe(res.getInteger(
                    com.android.internal.R.integer.config_longPressOnHomeBehavior));

            Action defaultDoubleTapAction = Action.fromIntSafe(res.getInteger(
                    com.android.internal.R.integer.config_doubleTapOnHomeBehavior));

            Action longPressAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                    defaultLongPressAction);
            mHomeLongPressAction = initActionList(KEY_HOME_LONG_PRESS, longPressAction);

            Action doubleTapAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                    defaultDoubleTapAction);
            mHomeDoubleTapAction = initActionList(KEY_HOME_DOUBLE_TAP, doubleTapAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(homeCategory);
        }

        if (hasBackKey) {
            if (!showBackWake) {
                backCategory.removePreference(findPreference(CMSettings.System.BACK_WAKE_SCREEN));
            }
        } else {
            prefScreen.removePreference(backCategory);
            prefScreen.removePreference(backlightCategory);
        }

        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory.removePreference(findPreference(CMSettings.System.MENU_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_MENU_ACTION, Action.MENU);
            mMenuPressAction = initActionList(KEY_MENU_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                        CMSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                        hasAssistKey ? Action.NOTHING : Action.SEARCH);
            mMenuLongPressAction = initActionList(KEY_MENU_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(menuCategory);
        }

        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory.removePreference(findPreference(CMSettings.System.ASSIST_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_ASSIST_ACTION, Action.SEARCH);
            mAssistPressAction = initActionList(KEY_ASSIST_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, Action.VOICE_SEARCH);
            mAssistLongPressAction = initActionList(KEY_ASSIST_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(assistCategory);
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory.removePreference(findPreference(
                        CMSettings.System.APP_SWITCH_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_APP_SWITCH_ACTION, Action.APP_SWITCH);
            mAppSwitchPressAction = initActionList(KEY_APP_SWITCH_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                    CMSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, Action.SPLIT_SCREEN);
            mAppSwitchLongPressAction = initActionList(KEY_APP_SWITCH_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(appSwitchCategory);
        }

        if (hasCameraKey) {
            mCameraWakeScreen = (SwitchPreference) findPreference(CMSettings.System.CAMERA_WAKE_SCREEN);
            mCameraSleepOnRelease =
                    (SwitchPreference) findPreference(CMSettings.System.CAMERA_SLEEP_ON_RELEASE);
            mCameraLaunch = (SwitchPreference) findPreference(CMSettings.System.CAMERA_LAUNCH);

            if (!showCameraWake) {
                prefScreen.removePreference(mCameraWakeScreen);
            }
            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(com.android.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease);
            }
        } else {
            prefScreen.removePreference(cameraCategory);
        }

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !getResources().getBoolean(
                    com.android.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(CMSettings.System.CAMERA_WAKE_SCREEN);
            }
        }                
    }

    @Override
    public void onResume() {
        super.onResume();

        // Home button answers calls.
        if (mHomeAnswerCall != null) {
            final int incallHomeBehavior = CMSettings.Secure.getInt(getContentResolver(),
                    CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR,
                    CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DEFAULT);
            final boolean homeButtonAnswersCall =
                (incallHomeBehavior == CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER);
            mHomeAnswerCall.setChecked(homeButtonAnswersCall);
        }
    }

    private ListPreference initActionList(String key, Action value) {
        return initActionList(key, value.ordinal());
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void updateBarModeSettings(int mode) {
        mNavbarMode.setValue(String.valueOf(mode));
        mSmartbarSettings.setEnabled(mode == 0);
        mSmartbarSettings.setSelectable(mode == 0);
        mFlingSettings.setEnabled(mode == 1);
        mFlingSettings.setSelectable(mode == 1);
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mNavbarVisibility.setChecked(showing);
        mNavInterface.setEnabled(mNavbarVisibility.isChecked());
        mNavGeneral.setEnabled(mNavbarVisibility.isChecked());
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

		if (preference.equals(mNavbarMode)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_MODE, mode);
            updateBarModeSettings(mode);
            return true;
        } else if (preference.equals(mNavbarVisibility)) {
            boolean showing = ((Boolean)newValue);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_VISIBLE,
                    showing ? 1 : 0);
            updateBarVisibleAndUpdatePrefs(showing);
            return true;
        } else if (preference == mBarHeightPort) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBarHeightLand) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_HEIGHT_LANDSCAPE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mBarWidth) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_WIDTH, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHomeLongPressAction) {
            handleActionListChange(mHomeLongPressAction, newValue,
                    CMSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleActionListChange(mHomeDoubleTapAction, newValue,
                    CMSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleActionListChange(mMenuPressAction, newValue,
                    CMSettings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleActionListChange(mMenuLongPressAction, newValue,
                    CMSettings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleActionListChange(mAssistPressAction, newValue,
                    CMSettings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleActionListChange(mAssistLongPressAction, newValue,
                    CMSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleActionListChange(mAppSwitchPressAction, newValue,
                    CMSettings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleActionListChange(mAppSwitchLongPressAction, newValue,
                    CMSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mHomeAnswerCall) {
            handleToggleHomeButtonAnswersCallPreferenceClick();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void handleToggleHomeButtonAnswersCallPreferenceClick() {
        CMSettings.Secure.putInt(getContentResolver(),
                CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR, (mHomeAnswerCall.isChecked()
                        ? CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER
                        : CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING));
    }
        
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OWN;
    }
}
