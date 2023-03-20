package dominando.android.mapas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivityBasico : AppCompatActivity() {
    private var googleMap: GoogleMap? = null
    private var origin = LatLng(-23.561706, -46.655981)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        fragment.getMapAsync {


            initMap(it)
        }
    }

    private fun initMap(map: GoogleMap?) {
        googleMap = map?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
        updateMap()
    }

    private fun updateMap() {
        googleMap?.run {

            //uiSettings.isMapToolbarEnabled = false
            //uiSettings.isZoomControlsEnabled = true
            // uiSettings.setAllGesturesEnabled(false)


            animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17.0f))
            addMarker(
                MarkerOptions()
                    .position(origin)
                    .title("Av. Paulista")
                    .snippet("São Paulo")
            )
        }
    }

    private fun updateMapAltoRelevo() {
        googleMap?.run {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
           // val icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_marker)

            addMarker(
                MarkerOptions()
                    .position(origin)
                    //.icon(icon)
                    .title("Av. Paulista")
                    .snippet("São Paulo")
            )

            val cameraPosition = CameraPosition.Builder()
                .target(origin)
                .zoom(17f)
                .bearing(90f)
                .tilt(45f).build()

            animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

}