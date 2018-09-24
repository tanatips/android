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

package ffc.app.location

import ffc.entity.House
import ffc.entity.Place
import me.piruin.geok.geometry.FeatureCollection
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaceService {

    @Headers("accept: application/vnd.geo+json")
    @GET("org/{orgId}/place/house")
    fun listHouseGeoJson(@Path("orgId") orgId: String): Call<FeatureCollection<House>>

    @GET("org/{orgId}/place/house?haveLocation=false")
    fun listHouseNoLocation(@Path("orgId") orgId: String): Call<List<House>>

    @PUT("org/{orgId}/place/house/{houseId}")
    fun updateHouse(
        @Path("orgId") orgId: String,
        @Body place: Place,
        @Path("houseId") houseId: String = place.id
    ): Call<ResponseBody>
}