package io.lassondehacks.destinatr.domain

data class Restriction(
        var code: String,
        var toujours: Boolean?,
        var journee: Array<String>?,
        var moisDebut: Array<String>?,
        var moisFin: Array<String>?,
        var heureDebut: Array<String>?,
        var heureFin: Array<String>?
)