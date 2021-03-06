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

package ffc.app.health.service

import ffc.entity.healthcare.HealthCareService
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HealthCareServiceApi {

    @POST("org/{orgId}/healthcareservice")
    fun post(@Body services: HealthCareService, @Path("orgId") orgId: String): Call<HealthCareService>

    @PUT("org/{orgId}/healthcareservice/{serviceId}")
    fun put(
        @Path("orgId") orgId: String,
        @Body services: HealthCareService,
        @Path("serviceId") serviceId: String = services.id
    ): Call<HealthCareService>

    @GET("org/{orgId}/person/{personId}/healthcareservice")
    fun get(@Path("orgId") orgId: String, @Path("personId") personId: String): Call<List<HealthCareService>>
}
