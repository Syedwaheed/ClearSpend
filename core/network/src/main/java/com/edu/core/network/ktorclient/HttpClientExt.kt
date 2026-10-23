package com.edu.core.network.ktorclient

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.BaseUrlKey
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.JsonConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, DataError.NetworkError> = safeCall {
    get {
        url(constructRoute(route))
        queryParameters.forEach { (key, value) -> parameter(key, value) }
    }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, DataError.NetworkError> = safeCall {
    delete {
        url(constructRoute(route))
        queryParameters.forEach { (key, value) -> parameter(key, value) }
    }
}

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.post(
    route: String,
    body: Request
): Result<Response, DataError.NetworkError> = safeCall {
    post {
        url(constructRoute(route))
        setBody(body)
    }
}

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.delete(
    route: String,
    body: Request,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, DataError.NetworkError> = safeCall {
    delete {
        url(constructRoute(route))
        queryParameters.forEach { (key, value) -> parameter(key, value) }
        setBody(body)
    }
}

/**
 * Every Ktor call in this app goes through [safeCall], directly or via the
 * [get]/[post]/[delete] extensions above. If an endpoint needs something
 * those extensions don't support (a non-JSON body such as form-encoded
 * OAuth, a different [HttpClient] instance, a custom status-to-[DataError]
 * mapping), call [safeCall] directly with that endpoint's own `execute`
 * lambda and/or [statusOverride] instead of re-implementing exception
 * handling or status-code mapping in the feature module — see
 * `TrueLayerAuthApi` for an example (OAuth's `/connect/token` returns 400
 * for `invalid_grant`, which must map to UNAUTHORIZED for THIS endpoint
 * only, so it supplies a [statusOverride] rather than a second safe-call).
 */
suspend inline fun <reified T> safeCall(
    statusOverride: (Int) -> DataError.NetworkError? = { null },
    execute: () -> HttpResponse,
): Result<T, DataError.NetworkError> {
    return try {
        val response = execute()
        responseToResult(response, statusOverride)
    } catch (e: UnresolvedAddressException) {
        Result.Error(DataError.NetworkError.NO_INTERNET)
    } catch (e: SerializationException) {
        Result.Error(DataError.NetworkError.SERIALIZATION)
    } catch (e: JsonConvertException) {
        Result.Error(DataError.NetworkError.SERIALIZATION)
    } catch (e: HttpRequestTimeoutException) {
        Result.Error(DataError.NetworkError.REQUEST_TIMEOUT)
    } catch (e: java.io.IOException) {
        // Broader than UnresolvedAddressException above: covers
        // UnknownHostException, ConnectException and other connectivity
        // failures the CIO engine surfaces as plain IOExceptions.
        Result.Error(DataError.NetworkError.NO_INTERNET)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.Error(DataError.NetworkError.UNKNOWN)
    }
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse,
    statusOverride: (Int) -> DataError.NetworkError? = { null },
): Result<T, DataError.NetworkError> {
    val status = response.status.value
    if (status in 200..299) return Result.Success(response.body())
    statusOverride(status)?.let { return Result.Error(it) }
    return when (status) {
        401 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
        404 -> Result.Error(DataError.NetworkError.NOT_FOUND)
        408 -> Result.Error(DataError.NetworkError.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.NetworkError.CONFLICT)
        413 -> Result.Error(DataError.NetworkError.PAYLOAD_TOO_LARGE)
        429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
        else -> Result.Error(DataError.NetworkError.UNKNOWN)
    }
}

fun HttpClient.constructRoute(route: String): String {
    val baseUrl = attributes[BaseUrlKey]
    return when {
        route.contains(baseUrl) -> route
        route.startsWith("/") -> baseUrl + route.drop(1)
        else -> baseUrl + route
    }
}
