package io.lassondehacks.destinatr.domain

data class Restriction(
        var code: String,
        var toujours: Boolean?,
        var journee: Array<String>?,
        var mois: Array<Int>?,
        var heureDebut: Array<String>?,
        var heureFin: Array<String>?
)