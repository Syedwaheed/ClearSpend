package com.edu.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TrueLayerEnvelope<T>(
    val results: List<T> = emptyList(),
    val status: String = "",
)
