/*
 * Copyright (c) 2018 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ffc.v3

import android.app.Application
import ffc.entity.Lookup
import ffc.v3.api.FfcCentral
import me.piruin.spinney.Spinney
import okhttp3.Cache

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Spinney.setDefaultItemPresenter { item, position ->
            if (item is Lookup) item.name else item.toString()
        }

        FfcCentral.cache = Cache(cacheDir, 10 * 1024 * 1024) //10 MB
    }
}