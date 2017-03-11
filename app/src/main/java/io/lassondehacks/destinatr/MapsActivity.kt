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
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import kotlinx.android.synthetic.main.activity_maps.*
import android.R.string.cancel
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.renderscript.RenderScript
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Switch
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.*
import io.lassondehacks.destinatr.domain.DirectionInfo
import io.lassondehacks.destinatr.domain.Result
import io.lassondehacks.destinatr.fragments.PlaceInfoFragment
import io.lassondehacks.destinatr.fragments.ResultListViewFragment
import io.lassondehacks.destinatr.services.LocationNotifyService
import io.lassondehacks.destinatr.services.DirectionService
import io.lassondehacks.destinatr.utils.LocationUtilities


class MapsActivity : FragmentActivity(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    val REQUEST_LOCATION_PERMISSION = 1

    private var mMap: GoogleMap? = null

    var marker: Marker? = null
    var polyline: Polyline? = null

    var googleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null

    var resultsFragment: ResultListViewFragment? = null

    var placeInfoFragment: PlaceInfoFragment = PlaceInfoFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        startService(Intent(this, LocationNotifyService::class.java))

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
                if (resultsFragment?.size?.compareTo(0) == 1) {
                    result_container.visibility = View.VISIBLE
                }
            }
            return@setOnTouchListener false
        }

        var ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.infoCardContainer, placeInfoFragment)
        ft.commit()
        infoCardContainer.visibility = View.INVISIBLE
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
                    .addApi(Places.GEO_DATA_API)
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
            startLocationUpdates()

            var currentPosition = mLastLocation
            val center = CameraUpdateFactory.newLatLng(LatLng(currentPosition!!.latitude, currentPosition!!.longitude))
            mMap!!.moveCamera(center)
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
        }
        var ft = supportFragmentManager.beginTransaction()
        resultsFragment = ResultListViewFragment(googleApiClient!!, {
            r ->
            onResultSelection(r)
        })
        ft.add(R.id.result_container, resultsFragment)
        ft.commit()
        result_container.visibility = View.INVISIBLE

        search_bar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resultsFragment!!.update(
                        search_bar.text.toString(),
                        LocationUtilities.getBoundingBoxAround(LatLng(mLastLocation?.latitude!!, mLastLocation?.longitude!!), 1f))
                if (resultsFragment?.size?.compareTo(0) == 1) {
                    result_container.visibility = View.INVISIBLE
                } else {
                    result_container.visibility = View.VISIBLE
                }
            }

        });


        mMap?.setOnMapClickListener {
            result_container.visibility = View.INVISIBLE
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        updatePosition()
    }

    fun updatePosition() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
    }

    fun showParkingFiltersAlert() {
        var parkingFilters = AlertDialog.Builder(this)
        parkingFilters.setTitle("Filtres de stationnements")

        parkingFilters.setView(R.layout.parking_filters)
        parkingFilters.setPositiveButton("Fermer", { dialog, id ->
            dialog.cancel()
        })
        var alert = parkingFilters.create()
        alert.show()
        val positive = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        positive.setTextColor(Color.BLACK)

        var prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        var editor = prefs.edit()
        (alert.findViewById(R.id.parking_meter_parkings) as Switch).isChecked = prefs.getBoolean(R.id.parking_meter_parkings.toString(), true)
        (alert.findViewById(R.id.parking_meter_parkings) as Switch).setOnCheckedChangeListener { buttonView, isChecked ->
            editor.putBoolean(R.id.parking_meter_parkings.toString(), isChecked)
            editor.apply()
        }

        (alert.findViewById(R.id.free_parkings) as Switch).isChecked = prefs.getBoolean(R.id.free_parkings.toString(), true)
        (alert.findViewById(R.id.free_parkings) as Switch).setOnCheckedChangeListener { buttonView, isChecked ->
            editor.putBoolean(R.id.free_parkings.toString(), isChecked)
            editor.apply()
        }

        (alert.findViewById(R.id.sticker_parkings) as Switch).isChecked = prefs.getBoolean(R.id.sticker_parkings.toString(), true)
        (alert.findViewById(R.id.sticker_parkings) as Switch).setOnCheckedChangeListener { buttonView, isChecked ->
            editor.putBoolean(R.id.sticker_parkings.toString(), isChecked)
            editor.apply()
        }
    }

    fun onResultSelection(result: Result) {

        var ds = DirectionService({
            r ->
            onDirectionData(r)
        })

        if(marker != null) {
            marker!!.remove()
        }

        ds.getDirectionInfo(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude), LatLng(result.latitude!!, result.longitude!!))

        placeInfoFragment.setInfo(result, {})
        infoCardContainer.visibility = View.VISIBLE
        result_container.visibility = View.INVISIBLE
        marker = mMap!!.addMarker(MarkerOptions().position(LatLng(result.latitude!!, result.longitude!!)).title(result.title))

        val destination = CameraUpdateFactory.newLatLng(LatLng(result.latitude!!, result.longitude!!))
        mMap!!.moveCamera(destination)
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f))
        var view = this.currentFocus
        if (view != null) {
            var imm = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

    fun onDirectionData(directions: DirectionInfo) {

        println(directions)

        placeInfoFragment.setDuration(directions.durationText)
        if(polyline != null) {
            polyline!!.remove()
        }
        var rectLine = PolylineOptions().width(20f).color(Color.argb(180, 33, 150, 243))

            for (i in 0..directions.directions!!.count() - 1) {
                rectLine.add(directions.directions!![i])
            }
        polyline = mMap!!.addPolyline(rectLine)
    }

    override fun onBackPressed() {
        if (infoCardContainer.visibility != View.INVISIBLE) {
            result_container.visibility = View.INVISIBLE
            infoCardContainer.visibility = View.INVISIBLE
            search_bar.text.clear()
            mMap!!.clear()
            val destination = CameraUpdateFactory.newLatLng(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
            mMap!!.moveCamera(destination)
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f))
            var view = this.currentFocus
            if (view != null) {
                var imm = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } else {
            super.onBackPressed()
        }
    }

    fun startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient!!,
                LocationRequest.create().setInterval(10).setSmallestDisplacement(1.0f).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)!!,
                this
        )
    }
}
