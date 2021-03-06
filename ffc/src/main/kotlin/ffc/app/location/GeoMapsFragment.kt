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

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.FloatingActionButton
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.sembozdemir.permissionskt.handlePermissionsResult
import ffc.android.drawable
import ffc.android.gone
import ffc.android.observe
import ffc.android.rawAs
import ffc.android.sceneTransition
import ffc.android.toBitmap
import ffc.android.viewModel
import ffc.app.R
import ffc.app.dev
import ffc.app.familyFolderActivity
import ffc.app.util.alert.handle
import ffc.entity.gson.toJson
import ffc.entity.place.House
import me.piruin.geok.geometry.FeatureCollection
import me.piruin.geok.geometry.Point
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.intentFor
import org.json.JSONObject
import th.or.nectec.marlo.PointMarloFragment

class GeoMapsFragment : PointMarloFragment() {

    val REQ_ADD_LOCATION = 1032

    val preferZoomLevel = 17.0f

    private var addLocationButton: FloatingActionButton? = null
    private val viewModel by lazy { viewModel<GeoViewModel>() }
    private val preference by lazy { GeoPreferences(context!!, org) }

    private val org by lazy { familyFolderActivity.org }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)
        setStartLocation(preference.lastCameraPosition)
        addLocationButton = activity!!.find(R.id.addLocationButton)
        viewFinder.gone()
        hideToolsMenu()
        observeViewModel()
    }

    private fun observeViewModel() {
        observe(viewModel.geojson) {
            it?.let {
                val coordinates = (it.features[0].geometry as Point).coordinates
                googleMap.animateCameraTo(
                    coordinates.latitude,
                    coordinates.longitude,
                    googleMap?.cameraPosition?.zoom?.takeIf { it >= preferZoomLevel }
                        ?: preferZoomLevel
                )
                googleMap.clear()
                addGeoJsonLayer(GeoJsonLayer(googleMap, JSONObject(it.toJson())))
                preference.geojsonCache = it
            }
        }
        observe(viewModel.exception) {
            it?.let { familyFolderActivity.handle(it) }
        }
    }

    private fun setStartLocation(lastPosition: CameraPosition?) {
        if (lastPosition != null) {
            setStartLocation(lastPosition.target, lastPosition.zoom)
        } else {
            setStartLocation(LatLng(13.76498, 100.538335), 5.0f)
            setStartAtCurrentLocation(true)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        super.onMapReady(googleMap)
        viewModel.geojson.value = doAsyncResult { preference.geojsonCache }.get()
        addLocationButton?.setOnClickListener {
            val intent = intentFor<MarkLocationActivity>(
                "target" to googleMap!!.cameraPosition.target,
                "zoom" to googleMap.cameraPosition.zoom
            )
            startActivityForResult(intent, REQ_ADD_LOCATION)
        }
        askMyLocationPermission()
        loadGeoJson()
    }

    private fun loadGeoJson() {
        placeGeoJson(familyFolderActivity.org!!).all {
            onFound { viewModel.geojson.value = it }
            onFail {
                dev { viewModel.geojson.value = context?.rawAs(R.raw.place) }
                viewModel.exception.value = it
            }
        }
    }

    private fun addGeoJsonLayer(layer: GeoJsonLayer) {
        with(layer) {
            features.forEach {
                it.pointStyle = GeoJsonPointStyle().apply {
                    icon = if (it.getProperty("haveChronic") == "true") chronicHomeIcon else homeIcon
                    title = "บ้านเลขที่ ${it.getProperty("no")}"
                    snippet = it.getProperty("coordinates")?.trimMargin()
                }
            }
            setOnFeatureClickListener { feature ->
                val intent = intentFor<HouseActivity>("houseId" to feature.getProperty("id"))
                startActivityForResult(intent, REQ_ADD_LOCATION, activity!!.sceneTransition())
            }
            addLayerToMap()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ADD_LOCATION -> {
                if (resultCode == Activity.RESULT_OK) loadGeoJson()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        activity!!.handlePermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        super.onPause()
        googleMap?.cameraPosition?.let { preference.lastCameraPosition = it }
    }

    fun bitmapOf(@DrawableRes resId: Int) = BitmapDescriptorFactory.fromBitmap(context!!.drawable(resId).toBitmap())

    private val homeIcon by lazy { bitmapOf(R.drawable.ic_marker_home_green_24dp) }

    private val chronicHomeIcon by lazy { bitmapOf(R.drawable.ic_marker_home_red_24dp) }

    class GeoViewModel : ViewModel() {
        val geojson = MutableLiveData<FeatureCollection<House>>()
        val exception = MutableLiveData<Throwable>()
    }
}
