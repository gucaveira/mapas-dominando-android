package dominando.android.mapas

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class AppMapFragment : SupportMapFragment() {

    private val viewModel: MapViewModel by activityViewModel()

    private var googleMap: GoogleMap? = null

    private var markerCurrentLocation: Marker? = null

    override fun getMapAsync(callback: OnMapReadyCallback) {
        super.getMapAsync {
            googleMap = it
            setupMap()
            callback.onMapReady(googleMap!!)
        }
    }

    private fun setupMap() {
        viewModel.getCurrentLocation().observe(this) { currentLocation ->
            if (currentLocation != null) {
                if (markerCurrentLocation == null) {
                    val icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_marker)

                    markerCurrentLocation = googleMap?.addMarker(
                        MarkerOptions().title(getString(R.string.map_current_location))
                            .icon(icon)
                            .position(currentLocation)
                    )
                }
                markerCurrentLocation?.position = currentLocation
            }
        }

        googleMap?.run {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            isMyLocationEnabled = true
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isZoomControlsEnabled = true
        }

        viewModel.getMapState().observe(viewLifecycleOwner) { mapState ->
            if (mapState != null) {
                updateMap(mapState)
            }
        }
    }

    private fun updateMap(mapState: MapViewModel.MapState) {
        googleMap?.run {
            clear()
            markerCurrentLocation = null
        }

        googleMap?.run {
            clear()
            val area = LatLngBounds.Builder()
            val origin = mapState.origin
            val destination = mapState.destination
            val route = mapState.route

            if (route != null && route.isNotEmpty()) {
                val polylineOptions = PolylineOptions().addAll(route).width(5f)
                    .color(Color.RED).visible(true)
                addPolyline(polylineOptions)
                route.forEach { area.include(it) }
            }


            origin?.let {
                addMarker(
                    MarkerOptions().position(origin)
                        .title(getString(R.string.map_marker_origin))
                )
                area.include(origin)
            }

            destination?.let {
                addMarker(
                    MarkerOptions()
                        .position(destination).title(getString(R.string.map_marker_destination))
                )
                area.include(destination)
            }

            origin?.let {
                if (destination != null) {
                    animateCamera(CameraUpdateFactory.newLatLngBounds(area.build(), 50))
                } else {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f))
                }
            }
        }
    }
}