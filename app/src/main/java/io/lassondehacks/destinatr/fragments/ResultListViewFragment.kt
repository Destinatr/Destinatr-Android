package io.lassondehacks.destinatr.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
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




class ResultListViewFragment(val client: GoogleApiClient, val onClickRegister: () -> Unit) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_result_list_view, container, false)

        return view
    }

    fun update(query: String, bounds: LatLngBounds) {

        var result = Places.GeoDataApi.getAutocompletePredictions(client, query,
                bounds,
                AutocompleteFilter.Builder().setCountry("CA").build())

        result.setResultCallback(
                object : ResultCallback<AutocompletePredictionBuffer> {
                   override fun onResult(result: AutocompletePredictionBuffer) {
                       (view!!.findViewById(R.id.results_layout) as LinearLayout).removeAllViews()

                       var ft = getChildFragmentManager().beginTransaction()

                       for (res in result) {
                           ft.add(R.id.results_layout, ResultFragment(Result(res.getFullText(null).toString(), res.getSecondaryText(null).toString(), res.getPrimaryText(null).toString(), 0)))
                       }

                       ft.commit()
                    }
                })
    }

    fun resultCallback(buffer: AutocompletePredictionBuffer) {

    }


}

