/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.server.wm;

import static android.hardware.display.DisplayManager.DeviceConfig.KEY_HIGH_REFRESH_RATE_BLACKLIST;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.res.Resources;
import android.provider.DeviceConfig;
import android.util.ArraySet;

import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BackgroundThread;
import com.android.server.utils.DeviceConfigInterface;

import java.io.PrintWriter;

/**
 * A Denylist for packages that should force the display out of high refresh rate.
 */
class HighRefreshRateBlacklist {

    private final ArraySet<String> mBlacklistedPackages = new ArraySet<>();
    @NonNull
    private final String[] mDefaultBlacklist;
    private final Object mLock = new Object();

    private DeviceConfigInterface mDeviceConfig;
    private OnPropertiesChangedListener mListener = new OnPropertiesChangedListener();

    static HighRefreshRateBlacklist create(@NonNull Resources r) {
        return new HighRefreshRateBlacklist(r, DeviceConfigInterface.REAL);
    }

    @VisibleForTesting
    HighRefreshRateBlacklist(Resources r, DeviceConfigInterface deviceConfig) {
        mDefaultBlacklist = r.getStringArray(R.array.config_highRefreshRateBlacklist);
        mDeviceConfig = deviceConfig;
        mDeviceConfig.addOnPropertiesChangedListener(DeviceConfig.NAMESPACE_DISPLAY_MANAGER,
                BackgroundThread.getExecutor(), mListener);
        final String property = mDeviceConfig.getProperty(DeviceConfig.NAMESPACE_DISPLAY_MANAGER,
                KEY_HIGH_REFRESH_RATE_BLACKLIST);
        updateBlacklist(property);
    }

    private void updateBlacklist(@Nullable String property) {
        synchronized (mLock) {
            mBlacklistedPackages.clear();
            if (property != null) {
                String[] packages = property.split(",");
                for (String pkg : packages) {
                    String pkgName = pkg.trim();
                    if (!pkgName.isEmpty()) {
                        mBlacklistedPackages.add(pkgName);
                    }
                }
            } else {
                // If there's no config, or the config has been deleted, fallback to the device's
                // default denylist
                for (String pkg : mDefaultBlacklist) {
                    mBlacklistedPackages.add(pkg);
                }
            }
        }
    }

    boolean isBlacklisted(String packageName) {
        synchronized (mLock) {
            return mBlacklistedPackages.contains(packageName);
        }
    }
    void dump(PrintWriter pw) {
        pw.println("High Refresh Rate Blacklist");
        pw.println("  Packages:");
        synchronized (mLock) {
            for (String pkg : mBlacklistedPackages) {
                pw.println("    " + pkg);
            }
        }
    }

    /** Used to prevent WmTests leaking issues. */
    @VisibleForTesting
    void dispose() {
        mDeviceConfig.removeOnPropertiesChangedListener(mListener);
        mDeviceConfig = null;
        mBlacklistedPackages.clear();
    }

    private class OnPropertiesChangedListener implements DeviceConfig.OnPropertiesChangedListener {
        public void onPropertiesChanged(@NonNull DeviceConfig.Properties properties) {
            if (properties.getKeyset().contains(KEY_HIGH_REFRESH_RATE_BLACKLIST)) {
                updateBlacklist(
                        properties.getString(KEY_HIGH_REFRESH_RATE_BLACKLIST, null /*default*/));
            }
        }
    }
}

