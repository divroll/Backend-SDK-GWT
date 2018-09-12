/**
 *
 * Copyright (c) 2017 Dotweblabs Web Technologies and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.divroll.factory.sdk;

import com.google.gwt.core.client.EntryPoint;

/**
 *
 * Application entry point
 *
 * @author Kerby Martino
 * @since 0-SNAPSHOT
 * @version 0-SNAPSHOT
 */
public class SDKEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        jsLog("Divroll Factory SDK Loaded");
    }

    public native void jsLog(String msg) /*-{
        console.log(msg);
	}-*/;
}
