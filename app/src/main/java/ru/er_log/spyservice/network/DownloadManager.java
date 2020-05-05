/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + Copyright (C) 2020 Eldar Timraleev (aka CRaFT4ik). All rights reserved.                        +
 + Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file      +
 + except in compliance with the License. You may obtain a copy of the License at                 +
 + http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in   +
 + writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT    +
 + WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the       +
 + specific language governing permissions and limitations under the License.                     +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package ru.er_log.spyservice.network;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import ru.er_log.spyservice.Settings;

public class DownloadManager
{
    private IDownloadAPI downloadAPI;

    private interface IDownloadAPI
    {
        @Streaming
        @GET("download.php")
        Call<ResponseBody> download(@Query("file") String file, @Query("lastModifiedTime") long lastModifiedTime);
    }

    public DownloadManager()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Settings.API_URL)
                .build();

        downloadAPI = retrofit.create(IDownloadAPI.class);
    }

    /** Downloads file. If the {@param callback} is null, then used synchronized connection method. */
    public boolean download(@NotNull String getName, @NotNull String saveFile, @Nullable IDownloadCallback callback)
    {
        File toSave = new File(saveFile);
        toSave.getParentFile().mkdirs();
        if (toSave.isDirectory())
            throw new IllegalArgumentException("Parameter 'saveFile' is a directory");

        Callback<ResponseBody> callbackD = new Callback<ResponseBody>()
        {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response)
            {
                assert callback != null;
                if (onCallResponse(response, toSave))
                    callback.onComplete(toSave);
                else
                    callback.onFailure();
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t)
            {
                assert callback != null;
                if (onCallFailure(t))
                    callback.onComplete(toSave);
                else
                    callback.onFailure();
            }
        };

        long lastModified = 0;
        if (toSave.isFile())
            lastModified = toSave.lastModified();
        Call<ResponseBody> call = downloadAPI.download(getName, lastModified);

        if (callback != null) // Asynchronous process.
        {
            call.enqueue(callbackD);
            return true;
        } else // Synchronous process.
        {
            try
            {
                Response<ResponseBody> response = call.execute();
                return onCallResponse(response, toSave);
            } catch (Exception e)
            {
                return onCallFailure(e);
            }
        }
    }

    private boolean onCallResponse(@NotNull Response<ResponseBody> response, File toSave)
    {
        if (response.isSuccessful())
        {
            if (response.code() == 200 && response.body() != null && Objects.equals(response.body().contentType(), MediaType.get("application/force-download")))
            {
                Log.d(Settings.LOG_TAG, "Server contacted and has file");
                try
                {
                    writeResponseBodyToDisk(response.body(), toSave);
                    Log.d(Settings.LOG_TAG, "File downloaded and written to disk");
                    return true;
                } catch (Exception e)
                {
                    Log.e(Settings.LOG_TAG, "Can't write file to disk");
                    e.printStackTrace();
                    return false;
                } finally
                {
                    if (response.body() != null)
                        response.body().close();
                }
            } else if (response.code() == 204)
            {
                Log.d(Settings.LOG_TAG, "Server contacted but file exists and there's no need to update it");
                return true;
            } else
            {
                Log.w(Settings.LOG_TAG, "Server contacted, but got unexpected response: " + response.toString());
                return false;
            }
        } else
        {
            Log.e(Settings.LOG_TAG, "Server contacted, but returned an error code: " + response.toString());
            return false;
        }
    }

    private boolean onCallFailure(@NotNull Throwable t)
    {
        Log.e(Settings.LOG_TAG, "Server contact failed: " + t.getMessage());
        return false;
    }

    private static void writeResponseBodyToDisk(@NotNull ResponseBody body, @NotNull File file) throws Exception
    {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {
            byte[] fileReader = new byte[4096];

            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);

            while (true)
            {
                int read;
                if ((read = inputStream.read(fileReader)) == -1)
                    break;

                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;

                Log.d(Settings.LOG_TAG, "file download: " + fileSizeDownloaded + " of " + fileSize + " bytes");
            }

            outputStream.flush();

            if (fileSizeDownloaded != fileSize)
                throw new Exception("downloaded size != file size; end of stream reached");
        } finally
        {
            if (inputStream != null)
                inputStream.close();

            if (outputStream != null)
                outputStream.close();
        }
    }
}
