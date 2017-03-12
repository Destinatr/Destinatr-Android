package io.lassondehacks.destinatr

import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.location.Location
import android.view.MotionEvent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_maps.*
import android.app.*
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.NotificationCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RatingBar
import android.widget.Switch
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import io.lassondehacks.destinatr.domain.DirectionInfo
import io.lassondehacks.destinatr.domain.Parking
import io.lassondehacks.destinatr.domain.Result
import io.lassondehacks.destinatr.fragments.PlaceInfoFragment
import io.lassondehacks.destinatr.fragments.ResultListViewFragment
import io.lassondehacks.destinatr.services.LocationNotifyService
import io.lassondehacks.destinatr.services.DirectionService
import io.lassondehacks.destinatr.services.ParkingService
import io.lassondehacks.destinatr.utils.LocationUtilities
import io.lassondehacks.destinatr.utils.ParkingClusterRenderer
import io.lassondehacks.destinatr.utils.PointClusterItem
import java.util.*

class MapsActivity : FragmentActivity(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    val REQUEST_LOCATION_PERMISSION = 1
    val NOTIFICATION_ID = 1337

    private var mMap: GoogleMap? = null

    val POSITION_UPDATE = "io.lassondehacks.destinatr.intent.action.NotifyUpdate"
    var receiver: LocationReceiver? = null
    var markedPosition: LatLng? = null
    var markedPositionForDestinationWalk: LatLng? = null
    var notificationPushed: Boolean = false

    var marker: Marker? = null
    var polylineToPark: Polyline? = null
    var polylineToEnd: Polyline? = null

    var googleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null

    var resultsFragment: ResultListViewFragment? = null

    var placeInfoFragment: PlaceInfoFragment = PlaceInfoFragment()

    var clusterManager: ClusterManager<PointClusterItem>? = null

    var lastLoadedBounds: LatLngBounds? = null

    var searchTimer: Timer? = null
    var parkingLoadingTimer: Timer? = null

    var stopParkingFetch = false
    var parkingFetchThreadRunning = false

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

        var filter = IntentFilter()
        filter.addAction(POSITION_UPDATE)

        if (receiver == null) {
            receiver = LocationReceiver()
            registerReceiver(receiver, filter)
        }
    }

    override fun onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver)
            receiver = null
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        if (notificationPushed) {
            notificationPushed = false
            showRatingDialog()
        }
    }

    fun showRatingDialog() {
        val dialog = AlertDialog.Builder(this)
                .setView(R.layout.rating_modal)
                .show()

        (dialog.findViewById(R.id.ratingBar) as RatingBar).setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            println(rating) //TODO send value to server
            dialog.cancel()
            if (markedPositionForDestinationWalk != null) {
                switchToGoogleNavigationWalk(markedPositionForDestinationWalk!!)
            }
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

            clusterManager = ClusterManager<PointClusterItem>(this, mMap)
            clusterManager!!.renderer = ParkingClusterRenderer(applicationContext, mMap!!, clusterManager!!)
            mMap!!.setOnCameraIdleListener {
                if (parkingLoadingTimer != null) {
                    parkingLoadingTimer?.cancel()
                }
                parkingLoadingTimer = Timer()
                parkingLoadingTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            if (lastLoadedBounds == null
                                    || !(lastLoadedBounds!!.contains(mMap!!.projection.visibleRegion.latLngBounds.southwest)
                                    && lastLoadedBounds!!.contains(mMap!!.projection.visibleRegion.latLngBounds.northeast))) {
                                lastLoadedBounds = LatLngBounds(mMap!!.projection.visibleRegion.latLngBounds.southwest, mMap!!.projection.visibleRegion.latLngBounds.northeast)
                                var distanceResult: FloatArray = arrayOf(0f, 0f, 0f).toFloatArray()
                                Location.distanceBetween(
                                        mMap!!.projection.visibleRegion.latLngBounds.southwest.latitude,
                                        mMap!!.projection.visibleRegion.latLngBounds.southwest.longitude,
                                        mMap!!.projection.visibleRegion.latLngBounds.northeast.latitude,
                                        mMap!!.projection.visibleRegion.latLngBounds.northeast.longitude, distanceResult)
                                distanceResult[0] = Math.max(0.0f, Math.min(30000.0f, distanceResult[0] / 2))
                                beginFetchParking(mMap!!.cameraPosition.target, distanceResult[0].toInt())
                            }
                        }
                    }
                }, 300)
                clusterManager?.onCameraIdle()
            }
            mMap?.setOnMarkerClickListener(clusterManager)

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
                if (searchTimer != null) {
                    searchTimer?.cancel()
                }
                searchTimer = Timer()
                searchTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            resultsFragment!!.update(
                                    search_bar.text.toString(),
                                    LocationUtilities.getBoundingBoxAround(LatLng(mLastLocation?.latitude!!, mLastLocation?.longitude!!), 1f))
                            if (resultsFragment?.size?.compareTo(0) == 1) {
                                result_container.visibility = View.INVISIBLE
                            } else {
                                result_container.visibility = View.VISIBLE
                            }
                        }
                    }
                }, 550)
            }

        })


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
            var distanceResult: FloatArray = arrayOf(0f, 0f, 0f).toFloatArray()
            Location.distanceBetween(
                    mMap!!.projection.visibleRegion.latLngBounds.southwest.latitude,
                    mMap!!.projection.visibleRegion.latLngBounds.southwest.longitude,
                    mMap!!.projection.visibleRegion.latLngBounds.northeast.latitude,
                    mMap!!.projection.visibleRegion.latLngBounds.northeast.longitude, distanceResult)
            distanceResult[0] = Math.max(0.0f, Math.min(30000.0f, distanceResult[0] / 2))
            beginFetchParking(mMap!!.cameraPosition.target, distanceResult[0].toInt())
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

    }

    fun onResultSelection(result: Result) {

        var ds = DirectionService({
            r ->
            onDirectionData(r)
        })

        if (marker != null) {
            marker!!.remove()
        }

        ds.getDirectionInfo(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude), LatLng(result.latitude!!, result.longitude!!))

        placeInfoFragment.setInfo(result)

        infoCardContainer.visibility = View.VISIBLE
        result_container.visibility = View.INVISIBLE
        marker = mMap!!.addMarker(MarkerOptions().position(LatLng(result.latitude!!, result.longitude!!)).title(result.title))

        val destination = CameraUpdateFactory.newLatLng(LatLng(result.latitude!!, result.longitude!!))
        mMap!!.moveCamera(destination)
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
        var view = this.currentFocus
        if (view != null) {
            var imm = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun onDirectionData(directions: Array<DirectionInfo>) {

        val dirStartToPark = directions[0]

        placeInfoFragment.setcb(dirStartToPark.directions!!.last(), {
            r ->
            switchToGoogleNavigationDrive(r)
        })

        placeInfoFragment.setDuration(dirStartToPark.durationText)
        if (polylineToPark != null) {
            polylineToPark!!.remove()
        }
        var rectLine = PolylineOptions().width(20f).color(Color.argb(200, 58, 164, 221))

        for (i in 0..dirStartToPark.directions!!.count() - 1) {
            rectLine.add(dirStartToPark.directions!![i])
        }
        polylineToPark = mMap!!.addPolyline(rectLine)


        if(directions.size > 1) {

            val dirParkToEnd = directions[1]

            markedPositionForDestinationWalk = dirParkToEnd.directions!!.last()

            //Parking to end

            if (polylineToEnd != null) {
                polylineToEnd!!.remove()
            }
            var rectLine2 = PolylineOptions().width(20f).color(Color.argb(200, 241, 90, 43))

            for (i in 0..dirParkToEnd.directions!!.count() - 1) {
                rectLine2.add(dirParkToEnd.directions!![i])
            }
            polylineToEnd = mMap!!.addPolyline(rectLine2)

        }
    }

    override fun onBackPressed() {
        if (infoCardContainer.visibility != View.INVISIBLE) {
            result_container.visibility = View.INVISIBLE
            infoCardContainer.visibility = View.INVISIBLE
            search_bar.text.clear()
            mMap!!.clear()
            val destination = CameraUpdateFactory.newLatLng(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
            mMap!!.moveCamera(destination)
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
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

    fun beginFetchParking(pos: LatLng, radius: Int) {
        var page = 0
        clearParkings()
        if (parkingFetchThreadRunning)
            stopParkingFetch = true
        var thread = Thread {
            while (parkingFetchThreadRunning) {
            }
            parkingFetchThreadRunning = true
            while (!stopParkingFetch) {
                var (err, parkings, remaining) = ParkingService.getParkingsAtLocationAtPage(
                        pos, radius, page, getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean(R.id.free_parkings.toString(), true),
                        getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean(R.id.parking_meter_parkings.toString(), true)
                )
                if (err == null && parkings != null) {
                    addParkings(parkings)
                    page++
                    if (remaining!! <= 0) {
                        break
                    }
                } else {
                    break
                }
            }
            parkingFetchThreadRunning = false
            stopParkingFetch = false
        }
        thread.start()
    }

    fun addParkings(parkings: List<Parking>) {
        for (parking in parkings) {
            clusterManager?.addItem(PointClusterItem(parking.position, parking.free!!))
        }
        runOnUiThread {
            clusterManager?.cluster()
        }
    }

    fun clearParkings() {
        clusterManager!!.clearItems()
//        mMap!!.clear()
    }

    fun switchToGoogleNavigationDrive(location: LatLng) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        markedPosition = location
        startActivity(mapIntent)
    }

    fun switchToGoogleNavigationWalk(location: LatLng) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        markedPosition = location
        startActivity(mapIntent)
    }

    fun createRatingNotification() {

        if (!notificationPushed) {
            var notificationIntent = Intent(getApplicationContext(), MapsActivity::class.java)
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

            val mBuilder = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_directions_car_black_24dp)
                    .setContentTitle("DestinatR - You are at your destination!")
                    .setContentText("Please rate your parking to help us improve our app")
                    .setVibrate(longArrayOf(100, 250, 100, 250, 100, 250))
                    .setLights(Color.RED, 3000, 3000)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
            var notification = mBuilder.build()

            notification.flags = (Notification.FLAG_AUTO_CANCEL or Notification.FLAG_ONGOING_EVENT)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
            notificationPushed = true
        }
    }

    inner class LocationReceiver : BroadcastReceiver(), LocationListener {

        override fun onReceive(context: Context, intent: Intent) {
            if (this@MapsActivity.markedPosition != null) {

                val resultArray = FloatArray(4)
                val currentLocationStr = intent.getStringExtra("CURRENT_LOCATION")
                val currentLng = currentLocationStr.substringAfter(",").toDouble()
                val currentLat = currentLocationStr.substringBefore(",").toDouble()

                Location.distanceBetween(
                        currentLat,
                        currentLng,
                        this@MapsActivity.markedPosition!!.latitude,
                        this@MapsActivity.markedPosition!!.longitude,
                        //45.421300,
                        //-71.962980,
                        resultArray
                )
                if (resultArray[0] <= 20.0f) {
                    this@MapsActivity.createRatingNotification()
                }
            }
        }

        override fun onLocationChanged(location: Location?) {
            if (this@MapsActivity.markedPosition != null) {

                val resultArray = FloatArray(4)
                val currentLocationStr = intent.getStringExtra("CURRENT_LOCATION")
                val currentLng = currentLocationStr.substringAfter(",").toDouble()
                val currentLat = currentLocationStr.substringBefore(",").toDouble()

                Location.distanceBetween(
                        currentLat,
                        currentLng,
                        this@MapsActivity.markedPosition!!.latitude,
                        this@MapsActivity.markedPosition!!.longitude,
                        //45.421300,
                        //-71.962980,
                        resultArray
                )
                if (resultArray[0] <= 20.0f) {
                    this@MapsActivity.createRatingNotification()
                }
            }
        }

    }
}
