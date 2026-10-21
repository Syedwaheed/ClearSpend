package com.edu.feature.auth.data.di

import com.edu.core.domain.network.TokenProvider
import com.edu.core.network.buildUnauthenticatedHttpClient
import com.edu.feature.auth.data.oauth.AuthUrlBuilder
import com.edu.feature.auth.data.oauth.SecureRandomStateGenerator
import com.edu.feature.auth.data.oauth.StateGenerator
import com.edu.feature.auth.data.remote.AuthApi
import com.edu.feature.auth.data.remote.TrueLayerAuthApi
import com.edu.feature.auth.data.remote.TrueLayerUserInfoApi
import com.edu.feature.auth.data.remote.UserInfoApi
import com.edu.feature.auth.data.token.SessionTokenManager
import com.edu.feature.auth.data.token.TokenMapper
import com.edu.feature.auth.data.token.TokenStore
import com.edu.feature.auth.data.token.TrueLayerTokenProvider
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AUTH_HTTP_CLIENT = named("truelayer_auth_http_client")

/**
 * Auth plumbing (not repositories — those are bound in :app's appModule).
 * Requires a `TrueLayerAuthConfig`, `SecureDataStorage`, `Clock` and the
 * Data API `HttpClient` from the graph.
 */
val authDataModule = module {
    single<HttpClient>(AUTH_HTTP_CLIENT) { buildUnauthenticatedHttpClient() }
    single<AuthApi> { TrueLayerAuthApi(httpClient = get(AUTH_HTTP_CLIENT), config = get()) }
    single<UserInfoApi> { TrueLayerUserInfoApi(dataHttpClient = get()) }
    single { TokenStore(secureStorage = get()) }
    single { TokenMapper(clock = get()) }
    single { SessionTokenManager(tokenStore = get(), authApi = get(), tokenMapper = get(), clock = get()) }
    single<TokenProvider> { TrueLayerTokenProvider(tokenManager = get()) }
    single { AuthUrlBuilder(config = get()) }
    single<StateGenerator> { SecureRandomStateGenerator() }
}
