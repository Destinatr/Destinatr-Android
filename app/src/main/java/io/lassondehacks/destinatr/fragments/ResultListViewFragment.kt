package io.lassondehacks.destinatr.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLngBounds
import io.lassondehacks.destinatr.R
import javax.xml.datatype.DatatypeConstants.SECONDS
import com.google.android.gms.location.places.AutocompletePredictionBuffer
import io.lassondehacks.destinatr.domain.Result
import java.util.concurrent.TimeUnit


class ResultListViewFragment(val client: GoogleApiClient, val onClickRegister: () -> Unit) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_result_list_view, container, false)

        return view
    }

    fun update(query: String, bounds: LatLngBounds, filters: AutocompleteFilter) {

        var result = Places.GeoDataApi.getAutocompletePredictions(client, query,
                bounds, filters)

        var ft = getChildFragmentManager().beginTransaction()

        val autocompletePredictions = result.await(2, TimeUnit.SECONDS)

        for (res in autocompletePredictions) {
            ft.add(R.id.results_layout, ResultFragment(Result(res.placeId, res.getPrimaryText(null).toString(), 10)))
        }

        ft.commit()

    }


}

