/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + Copyright (C) 2020 Eldar Timraleev (aka CRaFT4ik). All rights reserved.                        +
 + Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file      +
 + except in compliance with the License. You may obtain a copy of the License at                 +
 + http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in   +
 + writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT    +
 + WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the       +
 + specific language governing permissions and limitations under the License.                     +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package ru.er_log.spyservice.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import ru.er_log.spyservice.Settings;

public class CommonUtils
{
    private static String uniqueId = null;
    public static String getUniqueAppInstallationId(Context context)
    {
        synchronized (CommonUtils.class)
        {
            if (uniqueId != null) return uniqueId;
            File file = new File(context.getApplicationInfo().dataDir + File.separator + "unique.id");

            if (file.isFile())
            {
                try (FileInputStream inputStream = new FileInputStream(file.getAbsolutePath()))
                {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    uniqueId = bufferedReader.readLine();
                    return uniqueId;
                } catch (IOException e)
                {
                    Log.e(Settings.LOG_TAG, "Can't read existing unique install app id, returning random");
                    e.printStackTrace();
                }
            }

            @SuppressLint("HardwareIds")
            String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if ("9774d56d682e549c".compareTo(deviceId) == 0) deviceId = null;

            if (deviceId != null)
                uniqueId = UUID.nameUUIDFromBytes(deviceId.getBytes(StandardCharsets.UTF_8)).toString();
            else
                uniqueId = UUID.randomUUID().toString();

            try (FileOutputStream fileOutputStream = new FileOutputStream(file))
            {
                fileOutputStream.write(uniqueId.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e)
            {
                Log.e(Settings.LOG_TAG, "Can't write new unique install app id, returning random");
                e.printStackTrace();
            }

            return uniqueId;
        }
    }
}
