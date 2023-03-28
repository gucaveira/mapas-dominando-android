package dominando.android.mapas

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class AppMapFragment : SupportMapFragment() {

    private val viewModel: MapViewModel by activityViewModel()

    private var googleMap: GoogleMap? = null

    override fun getMapAsync(callback: OnMapReadyCallback) {
        super.getMapAsync {
            googleMap = it
            setupMap()
            callback.onMapReady(googleMap!!)
        }
    }

    private fun setupMap() {
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
            val area = LatLngBounds.Builder()
            val origin = mapState.origin
            val destination = mapState.destination

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