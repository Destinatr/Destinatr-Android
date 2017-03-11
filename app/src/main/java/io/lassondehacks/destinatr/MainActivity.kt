package io.lassondehacks.destinatr

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_NONE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.lassondehacks.destinatr.fragments.ResultListViewFragment
import kotlinx.android.synthetic.main.activity_main.*

import io.lassondehacks.destinatr.domain.Result
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Places


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks{

    var googleApiClient: GoogleApiClient? = null
    var resultList: ResultListViewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        var res1 = Result("Premier resultat", "une adresse", 10)
//        var res2 = Result("Deuxieme resultat", "une adresse", 101)
//        var res3 = Result("troisieme resultat", "une adresse", 1001)
//
//        var results = arrayOf(res1, res2, res3)
//
//        var ft = supportFragmentManager.beginTransaction()
//        ft.add(R.id.activity_main, ResultListViewFragment(results, { selectDest() }))
//        ft.commit()
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build()
        googleApiClient?.connect()

        resultList = ResultListViewFragment(googleApiClient as GoogleApiClient, { selectDest() })

        var ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.activity_main, resultList)
        ft.commit()
    }

    fun selectDest() {

    }

    override fun onStart() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
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
        var bounds = LatLngBounds(LatLng(-71.963, 45.421), LatLng(-71.96, 45.42))
        val typeFilter = AutocompleteFilter.Builder().setCountry("CA").build()
        resultList?.update("C", bounds)
    }

    override fun onConnectionSuspended(p0: Int) {
    }
}
