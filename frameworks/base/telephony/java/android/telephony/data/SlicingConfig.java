/**
 * Copyright 2021 The Android Open Source Project
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

package android.telephony.data;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a slicing configuration
 */
public final class SlicingConfig implements Parcelable {
    private final List<UrspRule> mUrspRules;
    private final List<NetworkSliceInfo> mSliceInfo;

    public SlicingConfig() {
        mUrspRules = new ArrayList<UrspRule>();
        mSliceInfo = new ArrayList<NetworkSliceInfo>();
    }

    /** @hide */
    public SlicingConfig(android.hardware.radio.V1_6.SlicingConfig sc) {
        this(sc.urspRules, sc.sliceInfo);
    }

    /** @hide */
    public SlicingConfig(List<android.hardware.radio.V1_6.UrspRule> urspRules,
            List<android.hardware.radio.V1_6.SliceInfo> sliceInfo) {
        mUrspRules = new ArrayList<UrspRule>();
        for (android.hardware.radio.V1_6.UrspRule ur : urspRules) {
            mUrspRules.add(new UrspRule(ur.precedence, ur.trafficDescriptors,
                    ur.routeSelectionDescriptor));
        }
        mSliceInfo = new ArrayList<NetworkSliceInfo>();
        for (android.hardware.radio.V1_6.SliceInfo si : sliceInfo) {
            mSliceInfo.add(sliceInfoBuilder(si));
        }
    }

    private NetworkSliceInfo sliceInfoBuilder(android.hardware.radio.V1_6.SliceInfo si) {
        NetworkSliceInfo.Builder builder = new NetworkSliceInfo.Builder()
                .setSliceServiceType(si.sst)
                .setMappedHplmnSliceServiceType(si.mappedHplmnSst);
        if (si.sliceDifferentiator != NetworkSliceInfo.SLICE_DIFFERENTIATOR_NO_SLICE) {
            builder
                .setSliceDifferentiator(si.sliceDifferentiator)
                .setMappedHplmnSliceDifferentiator(si.mappedHplmnSD);
        }
        return builder.build();
    }

    /** @hide */
    public SlicingConfig(Parcel p) {
        mUrspRules = p.createTypedArrayList(UrspRule.CREATOR);
        mSliceInfo = p.createTypedArrayList(NetworkSliceInfo.CREATOR);
    }

    /**
     * This list contains the current URSP rules. Empty list represents that no rules are
     * configured.
     * @return the current URSP rules for this slicing configuration.
     */
    public @NonNull List<UrspRule> getUrspRules() {
        return mUrspRules;
    }

    /**
     * @return the list of all slices for this slicing configuration.
     */
    public @NonNull List<NetworkSliceInfo> getSliceInfo() {
        return mSliceInfo;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(mUrspRules, flags);
        dest.writeTypedList(mSliceInfo, flags);
    }

    public static final @NonNull Parcelable.Creator<SlicingConfig> CREATOR =
            new Parcelable.Creator<SlicingConfig>() {
                @Override
                public SlicingConfig createFromParcel(Parcel source) {
                    return new SlicingConfig(source);
                }

                @Override
                public SlicingConfig[] newArray(int size) {
                    return new SlicingConfig[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlicingConfig that = (SlicingConfig) o;
        return mUrspRules.size() == that.mUrspRules.size()
                && mUrspRules.containsAll(that.mUrspRules)
                && mSliceInfo.size() == that.mSliceInfo.size()
                && mSliceInfo.containsAll(that.mSliceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUrspRules, mSliceInfo);
    }

    @Override
    public String toString() {
        return "{.urspRules = " + mUrspRules + ", .sliceInfo = " + mSliceInfo + "}";
    }
}
