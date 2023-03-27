package dominando.android.mapas

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dominando.android.mapas.MapViewModel.LocationError

class MainActivity : AppCompatActivity() {

    private val viewModel: MapViewModel by viewModels()

    private var isGpsDialogOpened: Boolean = false

    private val fragment: AppMapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fragmentMap) as AppMapFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isGpsDialogOpened = savedInstanceState?.getBoolean(EXTRA_GPS_DIALOG) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_GPS_DIALOG, isGpsDialogOpened)
    }

    override fun onStart() {
        super.onStart()
        fragment.getMapAsync {
            initUi()
            viewModel.connectGoogleApiClient()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnectGoogleApiClient()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == REQUEST_ERROR_PLAY_SERVICES && resultCode == Activity.RESULT_OK -> {
                viewModel.connectGoogleApiClient()

            }

            requestCode == REQUEST_CHECK_GPS -> {
                isGpsDialogOpened = false
                if (resultCode == RESULT_OK) {
                    loadLastLocation()
                } else {
                    Toast.makeText(this, R.string.map_error_gps_disabled, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && permissions.isNotEmpty()) {
            if (permissions.firstOrNull() == ACCESS_FINE_LOCATION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            ) {
                loadLastLocation()
            } else {
                showError(R.string.map_error_permissions)
                finish()
            }
        }
    }

    private fun initUi() {
        viewModel.getConnectionStatus().observe(this) { status ->
            status?.let {
                when {
                    status.success -> loadLastLocation()
                    else -> status.connectionResult?.let { handleConnectionError(it) }
                }
            }
        }

        viewModel.getCurrentLocationError().observe(this) { error ->
            handleLocationError(error)
        }
    }

    private fun handleLocationError(error: LocationError?) {
        if (error != null) {
            when (error) {
                is LocationError.ErrorLocationUnavailable -> showError(R.string.map_error_get_current_location)
                is LocationError.GpsDisabled -> {
                    if (isGpsDialogOpened.not()) {
                        isGpsDialogOpened = true
                        error.exception.startResolutionForResult(this, REQUEST_CHECK_GPS)
                    }
                }
                is LocationError.GpsSettingUnavailable -> showError(R.string.map_error_gps_settings)
            }
        }
    }

    private fun loadLastLocation() {
        if (hasPermission().not()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS
            )
            return
        }
        viewModel.requestLocation()
    }

    private fun handleConnectionError(result: ConnectionResult) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_ERROR_PLAY_SERVICES)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        } else {
            showPlayServicesErrorMessage(result.errorCode)
        }
    }

    private fun hasPermission(): Boolean {
        val granted = PackageManager.PERMISSION_GRANTED
        return ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == granted
    }

    private fun showError(@StringRes errorMessage: Int) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showPlayServicesErrorMessage(errorCode: Int) {
        GoogleApiAvailability
            .getInstance()
            .getErrorDialog(this, errorCode, REQUEST_ERROR_PLAY_SERVICES)?.show()
    }

    companion object {
        private const val REQUEST_ERROR_PLAY_SERVICES = 1
        private const val REQUEST_PERMISSIONS = 2
        private const val REQUEST_CHECK_GPS = 3
        private const val EXTRA_GPS_DIALOG = "gpsDialogIsOpen"
    }
}