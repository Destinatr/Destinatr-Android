package io.lassondehacks.destinatr.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import io.lassondehacks.destinatr.domain.Result

import io.lassondehacks.destinatr.R

class PlaceInfoFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_place_info, container, false)
        (view as RelativeLayout).gravity = 0x50
        return view
    }


    fun setInfo(result: Result, onClickStartRoute: (result: Result) -> Unit) {
        (view?.findViewById(R.id.titleText) as TextView).text = result.title
        (view?.findViewById(R.id.addressText) as TextView).text = result.address
        (view?.findViewById(R.id.startRouteButton) as FloatingActionButton).setOnClickListener {
            onClickStartRoute(result)
        }
    }

    fun setDuration(duration: String) {
        (view?.findViewById(R.id.routeTimeText) as TextView).text = duration
    }
}