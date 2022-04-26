package io.github.grebenindmitry.babilado.structures

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)