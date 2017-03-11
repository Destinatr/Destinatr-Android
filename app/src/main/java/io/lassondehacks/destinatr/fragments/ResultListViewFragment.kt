package io.lassondehacks.destinatr.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import io.lassondehacks.destinatr.R

class ResultListViewFragment(val title: String, val distance_km: Int, val onClickRegister: () -> Unit) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_result_list_view, container, false)

        (view?.findViewById(R.id.result_title) as TextView).text = title

        (view?.findViewById(R.id.card_view) as CardView).setOnClickListener { onClickRegister() }

        (view?.findViewById(R.id.distance_label) as TextView).text = "${distance_km}km from your position"

        return view
    }


}

