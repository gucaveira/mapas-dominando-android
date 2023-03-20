package dominando.android.mapas

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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
            val origin = mapState.origin
            if (origin != null) {
                addMarker(MarkerOptions().position(origin).title("Local atual"))
                animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17.0f))
            }
        }
    }
}