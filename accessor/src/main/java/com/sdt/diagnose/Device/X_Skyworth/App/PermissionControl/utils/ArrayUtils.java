/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.sdt.diagnose.Device.X_Skyworth.App.PermissionControl.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class ArrayUtils {
    private ArrayUtils() { /* cannot be instantiated */ }

    /**
     * Checks if an array is null or has no elements.
     *
     * @param array the array to check for
     * @return whether the array is null or has no elements.
     */
    public static <T> boolean isEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks that value is present as at least one of the elements of the array.
     *
     * @param array the array to check in
     * @param value the value to check for
     * @return true if the value is present in the array
     */
    public static <T> boolean contains(T[] array, T value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Return first index of {@code value} in {@code array}, or {@code -1} if
     * not found.
     */
    public static <T> int indexOf(T[] array, T value) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], value)) return i;
        }
        return -1;
    }

    public static String[] appendString(String[] cur, String val) {
        if (cur == null) {
            return new String[]{val};
        }
        final int N = cur.length;
        for (String s : cur) {
            if (TextUtils.equals(s, val)) {
                return cur;
            }
        }
        String[] ret = new String[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }
}
