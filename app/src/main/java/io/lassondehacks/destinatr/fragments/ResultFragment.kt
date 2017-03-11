package io.lassondehacks.destinatr.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.lassondehacks.destinatr.domain.Result

import io.lassondehacks.destinatr.R

class ResultFragment(val result: Result) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_result, container, false)

        (view.findViewById(R.id.result_title) as TextView).text = result.title

        (view.findViewById(R.id.distance_label) as TextView).text = "${result.distance_km}km from your position"

        (view.findViewById(R.id.address_label) as TextView).text = result.address

        return view
    }


}

