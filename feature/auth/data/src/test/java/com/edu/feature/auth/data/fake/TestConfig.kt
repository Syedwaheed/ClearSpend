package com.edu.feature.auth.data.fake

import com.edu.feature.auth.data.TrueLayerAuthConfig

val testAuthConfig = TrueLayerAuthConfig(
    authBaseUrl = "https://auth.truelayer-sandbox.com",
    clientId = "sandbox-client",
    clientSecret = "test-secret",
    redirectUri = "clearspend://callback",
)
