# ClearSpend Build Progress

Single source of truth for orchestrated build state. Resume from the first
phase not marked "verified".

## Conventions established

- **Branching:** committing phase work directly to `main` (matching the
  Phase 1 precedent already in git history: commit `a7867bf "Phase 1: audit
  and build the Foundation layer"`), not stacked `phase-N-<slug>` branches.
  CLAUDE.md specifies no branching strategy, and the orchestration prompt
  never describes merging stacked branches back, so direct-to-main with one
  commit per verified phase is the simplest reversible scheme (each phase is
  its own commit; `git revert`/`git reset` to any phase's hash is always
  available). Recorded as a deviation per the orchestration prompt's "make
  the sensible decision" rule.
- **Money:** `BigDecimal` everywhere (never `Double`), GBP minor-unit-safe.
  `Result<D, E>` is `com.edu.core.domain.util.Result`, never `kotlin.Result`.
- **feature:payments:domain**: Phase 1 removed the old `com.edu.feature.send`
  placeholder entirely (no `MyClass.kt`) rather than renaming it in place,
  since there was no real logic yet to preserve. Phase 5 builds it fresh
  under `com.edu.feature.payments.*`.

## Fixes applied after Phase 2 (before starting Phase 3)

The user caught a real architecture-consistency defect in the Phase 2 diff:
`TrueLayerAuthApi` (feature:auth:data) hand-built its own request URL and
rolled its own exception/status-to-`DataError` mapping instead of going
through `core:network`'s shared `safeCall`/`constructRoute` (which every
other new API class, e.g. `TrueLayerUserInfoApi`, correctly used). Its
mapping had also quietly drifted from the shared one (it mapped 400/403/429
to values the shared `responseToResult` didn't even have cases for).

Fixed:
- `core/network/.../HttpClientExt.kt`: `safeCall`/`responseToResult` now take
  an optional `statusOverride: (Int) -> DataError.NetworkError?` so an
  endpoint with genuinely different status semantics (OAuth 400
  `invalid_grant` → UNAUTHORIZED) can say so without a second
  exception-handling implementation. Added the missing 429→TOO_MANY_REQUESTS
  and 413→PAYLOAD_TOO_LARGE cases, a `HttpRequestTimeoutException` →
  REQUEST_TIMEOUT catch, and widened the NO_INTERNET catch to plain
  `java.io.IOException` (not just Ktor's `UnresolvedAddressException`) —
  all three were real gaps in the Phase 1 helper, not just this bug.
- `core/network/.../HttpClientFactory.kt`: `buildUnauthenticatedHttpClient`
  now takes a `baseUrl` and sets `BaseUrlKey` like `buildHttpClient` does, so
  every `HttpClient` in the app — not just the authenticated one — supports
  `constructRoute`.
- `feature/auth/data/.../TrueLayerAuthApi.kt`: rewritten to call the shared
  `safeCall`/`constructRoute` with a `statusOverride`, deleting its
  module-local `postToken`/`toResult` duplicate.
- `AuthDataModule.kt` / `TrueLayerAuthApiTest.kt`: updated for the new
  `buildUnauthenticatedHttpClient(baseUrl, engine)` signature.
- Added tests: 429/413 mapping, `statusOverride` precedence and fallback
  (`core:network`), all existing `TrueLayerAuthApiTest` cases re-verified.
- **CLAUDE.md §2 and §4** and **BUILD_PROMPTS.md Part A** updated with an
  explicit rule: `core:network` is the only place that builds a route or
  maps a Ktor exception/status to `DataError.NetworkError`; a `*:data`
  module extends `core:network` instead of reinventing this, even for an
  endpoint with unusual needs. This rule is now baked into every future
  phase subagent's dispatch prompt (it's in Part A, which every phase
  dispatch includes verbatim) and into this orchestrator's own per-phase
  audit going forward.
- Re-ran `./gradlew clean build test` after the fix: `BUILD SUCCESSFUL in
  1m 55s`, aggregated JUnit XML `tests=226 skipped=0 failures=0 errors=0`
  (222 + the 4 new core:network tests). One earlier run failed at
  `:app:packageDebug` ("IncrementalSplitterRunnable") — retrying just that
  task in isolation succeeded immediately, and a second full `clean build
  test` passed clean, confirming it was a transient packaging/file-lock
  flake (this environment has Android Studio open on the same project),
  not caused by the code change. No commit message/phase number changes:
  this lands as a small follow-up commit on top of Phase 2, since Phase 2
  itself is otherwise sound and already verified.

## Phase Status

| Phase | Status | Commit(s) | Tests | Notes |
|---|---|---|---|---|
| 1 — Foundation | verified | `ca12960` → `a7867bf` → `e926e4f` → `729609b` | 66 tests, 0 failures (`./gradlew build test`, BUILD SUCCESSFUL in 41s) | Built before this orchestration session; re-verified now (build + test rerun, non-negotiable-rules audit rerun, see below). Committed directly to main, not a phase branch. |
| 2 — Auth + app shell | verified | `68f0184` | 222 tests, 0 failures (`./gradlew clean build test`, BUILD SUCCESSFUL in 2m1s) | feature:auth:domain/data/presentation + :app shell (Koin, Navigation3, deep link, biometric, splash). See Phase 2 section below for full detail. |
| 3 — Dashboard | in-progress | — | — | — |
| 4 — Transactions | todo | — | — | — |
| 5 — Send Money | todo | — | — | — |
| 6 — Analytics | todo | — | — | — |
| 7 — Polish | todo | — | — | — |

## Phase 1 — re-verification (this session)

Ran fresh since no PROGRESS.md existed yet to trust blindly:

- `./gradlew build test` → `BUILD SUCCESSFUL in 41s`, 1587 actionable tasks.
- Aggregated JUnit XML across all modules: `tests=66 skipped=0 failures=0 errors=0`.
- Audit grep across `core/`, `feature/`, `app/`: no `Double` for money in
  domain/database/presentation-ui, no `kotlin.Result`, no `.toUi(`, no
  hardcoded `version("...")` outside the catalog, no mixed
  `clearspend.jvm.library` + `clearspend.android.*` in any `:domain` module.
- `Transaction`/`Balance`/`Account` models use `BigDecimal` + `Instant` +
  enums as CLAUDE.md §15 Phase 1 / BUILD_PROMPTS Phase 1 requires.
- `app/src/main` is down to `MainActivity.kt` only — no leftover
  `ui/theme` duplicate, no `CurrencyFormatter.kt`, no `BaseUseCase.kt`.
- `core/presentation/ui` has the full shared-composable set (AccountCard,
  BalanceCard, TransactionRow, AmountText, CategoryIcon,
  ClearSpendBottomNav, LoadingScreen, ErrorScreen, EmptyStateScreen),
  UiStateMachine/MviViewModel, UiMapper + 3 shared mappers, 3 formatters,
  CoreUiModule, with matching test files for each.
- Not independently re-verified: `@Preview` visual match to screenshots
  (no emulator/adb device available in this environment — see "needs
  human" below), and whether the Tink/token-refresh gap-fixes CLAUDE.md §8
  calls for are fully exhaustive (spot-checked `AuthPlugin`/
  `TokenRefreshPlugin`/`EncryptedDataStoreSecureStorage`; they exist and
  have tests, did not re-derive TrueLayer's exact refresh semantics
  line-by-line).

**Verdict: Phase 1 = verified.** Proceeding to Phase 2.

## Needs human

- No Android emulator / connected device detected in this environment
  (`adb devices` not checked yet as of Phase 1 — will check at each
  phase gate going forward). Visual screenshot comparisons rely on
  `@Preview` and Compose UI tests only until a device is available.
- Real TrueLayer sandbox credentials (`TRUELAYER_CLIENT_SECRET`,
  `TRUELAYER_SIGNING_KEY`) are not present in `local.properties` beyond
  `sdk.dir` (per CLAUDE.md §11) — real sandbox OAuth login and real
  sandbox payments cannot be exercised by this orchestrator until those
  are supplied. Phases will use `FakePaymentsApi` / existing scaffolding
  where the gate allows it and note the rest as "needs human".

## Phase 2 — Auth + app shell

Built by a dispatched subagent (general-purpose, opus), then independently
re-verified in this session (fresh `./gradlew clean build test`, aggregated
JUnit XML across all modules, and a non-negotiable-rules grep audit).

- **Build/test (reproduced independently):** `BUILD SUCCESSFUL in 2m 1s`,
  1624 actionable tasks; aggregated test XML `tests=222 skipped=0
  failures=0 errors=0` (up from 66 after Phase 1).
- **Audit (reproduced independently):** no `kotlin.Result`, no `.toUi(`,
  no `Double` on amount/balance/available/current/overdraft fields, no
  ad-hoc `NumberFormat`/`SimpleDateFormat`/`DateTimeFormatter` outside the
  `formatter` package, no cross-feature `projects.feature.*` deps, no
  `Route` leakage into feature modules, no token/secret logging, no
  hardcoded `version("...")` outside the catalog, no `jvm.library` +
  `android.*` mixing in any `:domain` module, no `@Ignore`/no-op
  assertions, no stray TODO/FIXME in changed files.
- **Secrets check:** `git diff` scanned for secret-shaped strings before
  commit — none found. `local.properties` untouched/unstaged.
- **Manual spot-check:** read `ConnectingViewModel.kt` in full — correct
  `MviViewModel` usage, state-restoration handling for an in-flight OAuth
  exchange, pure `reduceUiState`, effects sent from inside the cold flow.
- **What was built:** CLAUDE.md §8 updated to `uk-cs-mock`
  (+`uk-ob-all uk-oauth-all`); `feature:auth:domain` (models, repository
  interfaces, 7 use cases incl. `DetermineStartDestinationUseCase`);
  `feature:auth:data` (AuthApi/TrueLayerAuthApi, token store via Tink,
  `SessionTokenManager`, OAuth URL builder + state generator, bank-provider
  repository); `feature:auth:presentation` (full MVI sets for Onboarding,
  ConnectBank, BankSelection, Connecting, Biometric, with `@Preview`s);
  `:app` (Application/Koin wiring, full `Route` sealed set, Navigation3
  host incl. a bottom-sheet scene strategy for Send, deep-link parsing
  with replay protection, Splash MVI, placeholder screens for
  later-phase tabs); root `README.md`.
- **Screens 01–08:** no emulator/device available in this environment
  (`adb devices` empty, no `emulator` binary) — visual match relies on
  `@Preview` + the Compose UI test (`AuthScreensTest`, compiles but not
  run) rather than a live side-by-side screenshot comparison. Logged as
  "needs human" below, per the orchestration prompt's instruction to
  keep going rather than block the phase on it.
- **Deviations** (subagent's calls, reviewed and accepted as sensible):
  biometric is skipped after silent token refresh (only an UNAUTHORIZED
  refresh failure forces re-auth); OAuth `state` persisted Tink-encrypted
  (survives process death across the Custom Tab) and validated in
  `ValidateAuthRedirectUseCase`, not in `MainActivity`; Custom Tab opens
  from Bank Selection (06), matching DESIGN_SPEC §3's 05→06 order;
  Onboarding "Sign in" routes to Connect Bank (no app account exists);
  Splash always re-runs routing even after restore; biometric greeting
  best-effort fetches `/data/v1/info` and falls back to a plain
  "Welcome back"; light theme forced in `MainActivity`.
- **Left undone (by design, later phases):** Biometric screen reachable
  only once Settings (Phase 7) flips `biometricEnabled`; bottom nav keeps
  Phase 1's full-width style (floating-pill restyle is Phase 3); global
  401→Connect Bank routing is each data screen's job from Phase 3 on;
  sign-out Room-cache clearing is Phase 7; bank-provider list is static
  (fine for sandbox).

## Needs human (cumulative)

- No Android emulator/device in this environment — every phase's visual
  screenshot comparison and on-device flows (OAuth login, biometric
  prompt, real sandbox payment) are deferred to a human with a device.
- Real TrueLayer sandbox credentials: `TRUELAYER_CLIENT_SECRET` (register
  `clearspend://callback` as an allowed redirect URI in the TrueLayer
  Console too) and, later, `TRUELAYER_SIGNING_KEY` for Phase 5 payments.
- Phase 2 specifically: device login with Mock bank (john/doe) to confirm
  landing on the Dashboard placeholder; run `AuthScreensTest`
  (`connectedAndroidTest`); BiometricPrompt fingerprint + PIN-fallback
  check; process-death resume check on the Connecting screen.

## Deviations

- Branching (see Conventions above).
- `feature:payments:domain` placeholder removed outright in Phase 1 rather
  than renamed (see Conventions above).
- Phase 2 deviations: see Phase 2 section above.
