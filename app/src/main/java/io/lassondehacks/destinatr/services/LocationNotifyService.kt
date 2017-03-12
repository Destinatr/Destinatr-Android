package io.lassondehacks.destinatr.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationNotifyService : Service(), LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    internal var mLocationRequest: LocationRequest? = null
    internal var mGoogleApiClient: GoogleApiClient? = null

    val BROADCAST = "PACKAGE_NAME.android.action.broadcast"

    override fun onCreate() {

        //show error dialog if GoolglePlayServices not available
        if (isGooglePlayServicesAvailable) {
            mLocationRequest = LocationRequest()
            mLocationRequest!!.fastestInterval = 10
            mLocationRequest!!.interval = 50
            mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            //mLocationRequest!!.smallestDisplacement = 1.0f
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()

            mGoogleApiClient!!.connect()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    //Check Google play is available or not
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(applicationContext)
            return ConnectionResult.SUCCESS == status
        }


    override fun onConnected(bundle: Bundle?) {
        startLocationUpdates()
    }

    override fun onConnectionSuspended(i: Int) {

    }

    protected fun startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this)
        } catch (e: IllegalStateException) {
        }

    }

    override fun onLocationChanged(location: Location?) {
        val i = Intent("io.lassondehacks.destinatr.intent.action.NotifyUpdate")
        i.putExtra("CURRENT_LOCATION", "${location?.latitude},${location?.longitude}")
        sendBroadcast(i)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    companion object {
        var mCurrentLocation: Location? = null
    }
}