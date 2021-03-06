/*
 * Copyright 2015 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.systemTray.jna.linux;

import com.sun.jna.Pointer;

import dorkbox.systemTray.jna.JnaHelper;

/**
 * bindings for libgthread
 *
 * Direct-mapping, See: https://github.com/java-native-access/jna/blob/master/www/DirectMapping.md
 */
public
class GThread {
    static {
        JnaHelper.register("gthread-2.0", GThread.class);
    }

    public static native void g_thread_init(Pointer GThreadFunctions);
}
