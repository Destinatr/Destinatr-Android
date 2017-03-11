package io.lassondehacks.destinatr

import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.app.AlertDialog
import android.location.Location
import android.location.LocationListener
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import kotlinx.android.synthetic.main.activity_maps.*
import android.R.string.cancel
import android.content.DialogInterface
import android.content.res.ColorStateList


class MapsActivity : FragmentActivity(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    val REQUEST_LOCATION_PERMISSION = 1

    private var mMap: GoogleMap? = null

    var googleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        search_bar.setOnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.x <= search_bar.compoundDrawables[DRAWABLE_LEFT].bounds.width()) {
                    showParkingFiltersAlert()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            setupMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_LOCATION_PERMISSION) {
                setupMap()
            }
        }
    }

    fun setupMap() {
        mMap!!.isMyLocationEnabled = true
        mMap!!.setPadding(0, 250, 0, 0)
    }

    override fun onStart() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
        googleApiClient?.connect()
        super.onStart()
    }

    override fun onStop() {
        googleApiClient?.disconnect()
        super.onStop()
    }

    override fun onConnected(p0: Bundle?) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            updatePosition()

            var currentPosition = mLastLocation
            val center = CameraUpdateFactory.newLatLng(LatLng(currentPosition!!.latitude, currentPosition!!.longitude))
            mMap!!.moveCamera(center)
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        updatePosition()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    fun updatePosition() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
    }

    fun showParkingFiltersAlert() {
        var parkingFilters = AlertDialog.Builder(this)
        parkingFilters.setTitle("Filtres de stationnements")

        parkingFilters.setView(R.layout.parking_filters)
        parkingFilters.setPositiveButton("Fermer", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        var alert = parkingFilters.create()
        alert.show()
    }

}
