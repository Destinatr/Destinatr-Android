package io.lassondehacks.destinatr.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLngBounds
import io.lassondehacks.destinatr.R
import javax.xml.datatype.DatatypeConstants.SECONDS
import com.google.android.gms.location.places.AutocompletePredictionBuffer
import io.lassondehacks.destinatr.domain.Result
import java.util.concurrent.TimeUnit
import com.google.android.gms.drive.DriveApi
import com.google.android.gms.drive.Drive
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus
import com.google.android.gms.location.places.PlaceBuffer


class ResultListViewFragment(val client: GoogleApiClient, val onClickRegister: (result: Result) -> Unit) : Fragment() {

    var size: Int = 0
        get
        set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_result_list_view, container, false)

        return view
    }

    fun update(query: String, bounds: LatLngBounds) {

        this.size = 0

        var result = Places.GeoDataApi.getAutocompletePredictions(client, query,
                bounds,
                AutocompleteFilter.Builder().setCountry("CA").build())

        result.setResultCallback(
                object : ResultCallback<AutocompletePredictionBuffer> {
                    override fun onResult(result: AutocompletePredictionBuffer) {
                        (view!!.findViewById(R.id.results_layout) as LinearLayout).removeAllViews()

                        var params = emptyArray<String>()

                        for (res in result) {
                            params = params.plus(res.placeId!!)
                            this@ResultListViewFragment.size++
                        }

                        if (size > 0) {
                            Places.GeoDataApi.getPlaceById(client, *params)
                                    .setResultCallback { places ->
                                        if (places.status.isSuccess) {
                                            var ft = childFragmentManager.beginTransaction()

                                            for (place in places) {
                                                ft.add(R.id.results_layout, ResultFragment(
                                                        Result(
                                                                place.name.toString(),
                                                                place.address.toString(),
                                                                place.attributions?.toString(),
                                                                place.latLng.latitude,
                                                                place.latLng.longitude
                                                        ),
                                                        onClickRegister
                                                )
                                                )
                                            }

                                            ft.commit()

                                        }
                                        places.release()
                                    }
                        }
                    }
                })
    }

    fun resultCallback(buffer: AutocompletePredictionBuffer) {

    }


}

