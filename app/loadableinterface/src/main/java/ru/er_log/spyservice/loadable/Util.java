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

import java.io.File;

import dalvik.system.DexClassLoader;

class Util
{
    static ILoadable loadModule(String className, File dexFile, File cacheDir, ClassLoader parent)
    {
        try
        {
            DexClassLoader classLoader = new DexClassLoader(dexFile.getAbsolutePath(), cacheDir.getAbsolutePath(), null, parent);
            Class<?> moduleClass = classLoader.loadClass(className);

//            // All classes in DEX file.
//            DexFile df = new DexFile(dexFile);
//            for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); )
//            {
//                String _className = iter.nextElement();
//                if (!_className.equals(className))
//                    classLoader.loadClass(_className);
//            }

            if (ILoadable.class.isAssignableFrom(moduleClass))
                return (ILoadable) moduleClass.newInstance();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
