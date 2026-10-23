# ClearSpend

UK personal-finance Android app (Kotlin, Jetpack Compose, Material 3) built on the
TrueLayer **sandbox**. Architecture and conventions: see `CLAUDE.md`.

## TrueLayer credentials

Add these keys to `local.properties` (git-ignored, never commit it):

```
TRUELAYER_CLIENT_ID=sandbox-your-client-id
TRUELAYER_CLIENT_SECRET=your_sandbox_secret
TRUELAYER_REDIRECT_URI=clearspend://callback
TRUELAYER_SANDBOX=true
```

The `build-logic` convention plugins expose them as `BuildConfig` fields in every
module. A missing key becomes an empty string, so the project still builds; the token
exchange then fails with "Couldn't connect your bank" until a real sandbox secret is set.
`clearspend://callback` must also be added as an allowed redirect URI in the TrueLayer
Console.

Sandbox login: pick any bank (the Mock bank `uk-cs-mock` is always offered), then
sign in with `john` / `doe`.

> **Security note.** Shipping `client_secret` in an APK is acceptable for this sandbox
> portfolio app only, because anything in an APK can be extracted. In production the
> token exchange, refresh, payment creation and request signing move to a backend that
> holds the secret. All secret-dependent calls sit behind `AuthApi`
> (`feature:auth:data`) so only that implementation changes.

## Build

```bash
./gradlew build    # full build
./gradlew test     # unit tests
```
