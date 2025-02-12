/*
 * Copyright (C) 2020 The Pixel Experience Project
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
package com.android.internal.util.custom;

import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static volatile boolean sIsGms = false;
    public static final String PACKAGE_GMS = "com.google.android.gms";

    private static final Map<String, Object> propsToChange;
    private static final Map<String, ArrayList<String>> propsToKeep;
    private static final String[] extraPackagesToChange = {
            "com.android.vending",
            "com.breel.wallpapers20",
            "com.google.android.apps.customization.pixel",
            "com.google.android.apps.fitness",
            "com.google.android.apps.recorder",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.turboadapter",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.as",
            "com.google.android.dialer",
            "com.google.android.deskclock",
            "com.google.android.apps.youtube.music",
            "com.google.android.apps.podcasts",
            "com.google.android.apps.nbu.files",
            "com.google.android.contacts",
            "com.google.android.calculator",
            "com.google.android.apps.messaging",
            "com.google.android.gm",
            "com.google.android.gms.location.history",
            "com.android.chrome",
            "com.google.android.inputmethod.latin",
            "com.google.android.soundpicker",
            "com.google.pixel.dynamicwallpapers",
            "com.google.pixel.livewallpaper",
            "com.google.android.apps.safetyhub",
            "com.google.android.apps.turbo",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.googleassistant",
            "com.google.android.apps.maps",
            "com.google.android.gms",
            "com.google.android.apps.photos",
            "com.google.android.apps.nexuslauncher"
    };

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<String>(Arrays.asList("FINGERPRINT")));
        propsToChange = new HashMap<>();
        propsToChange.put("BRAND", "google");
        propsToChange.put("MANUFACTURER", "Google");
        propsToChange.put("DEVICE", "raven");
        propsToChange.put("PRODUCT", "raven");
        propsToChange.put("MODEL", "Pixel 6 Pro");
        propsToChange.put("FINGERPRINT", "google/raven/raven:12/SD1A.210817.015.A4/7697517:userdebug/dev-keys");
    }

    public static void setProps(String packageName) {
        if (packageName == null){
            return;
        }
        if (packageName.equals(PACKAGE_GMS)) {
            sIsGms = true;
            setPropValue("TYPE", "userdebug");
        }
        if (packageName.startsWith("com.google.") || Arrays.asList(extraPackagesToChange).contains(packageName)){
            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)){
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
        }
        // Set proper indexing fingerprint
        if (packageName.equals("com.google.android.settings.intelligence")){
            setPropValue("FINGERPRINT", Build.DATE);
        }
    }

    private static void setPropValue(String key, Object value){
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static boolean isCallerSafetyNet() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet
        if (sIsGms && isCallerSafetyNet()) {
            throw new UnsupportedOperationException();
        }
    }
}
