package io.lassondehacks.destinatr.services

import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPost
import io.lassondehacks.destinatr.domain.Rating

class RatingService {
    companion object {
        val API_URL = "http://159.203.14.223:8080"
        fun postRating(rating: Rating) {
            "$API_URL/rating".httpPost(
                    arrayListOf(
                            "latitude" to rating.position.latitude,
                            "longitude" to rating.position.longitude,
                            "timestamp" to rating.timestamp,
                            "value" to rating.value
                    )
            ).responseJson { request, response, result ->
                result.fold({ d ->

                }, { err ->
                    println(err)
                })
            }
        }
    }
}