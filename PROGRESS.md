# ClearSpend Build Progress

Single source of truth for orchestrated build state. Resume from the first
phase not marked "verified".

## Conventions established

- **Branching (changed starting Phase 4):** each phase now gets its own
  `feature/<slug>` branch off `main` (e.g. `feature/transactions`,
  `feature/send-money`), pushed to GitHub at the end of the phase instead of
  committed straight to `main`. The user opens and reviews the PR themselves
  (not the AI) — this project doubles as PR-review practice for interviews,
  so a feature branch + real PR per phase is the point, not a shortcut to
  avoid. Phases 1-3 were committed directly to `main` before this convention
  existed (`a7867bf`, `68f0184`/`4d8a0cd`, `7383b8a`) and are not being
  rewritten; only work from Phase 4 onward uses branches. See
  `BUILD_PROMPTS.md` "How to use" for the exact steps each phase session
  follows.
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
| 2 — Auth + app shell | verified | `68f0184`, fix `4d8a0cd` | 226 tests, 0 failures (`./gradlew clean build test`, BUILD SUCCESSFUL in 1m55s) | feature:auth:domain/data/presentation + :app shell (Koin, Navigation3, deep link, biometric, splash). `4d8a0cd` fixes a core:network bypass the user caught in review — see "Fixes applied after Phase 2" below. |
| 3 — Dashboard | verified | `7383b8a` | 603 tests, 0 failures (`./gradlew clean build test`, BUILD SUCCESSFUL in 2m51s) | Home, Accounts, Account Detail, Card + bottom nav. Pulled forward Phase 4's TransactionRepositoryImpl (Home/Account Detail need it). See Phase 3 section below. |
| 4 — Transactions | verified | `d0c2c25` on `feature/transactions` (pushed, PR pending) | 799 tests, 0 failures (`./gradlew clean build test` → transient `:app:packageDebug` flake, rerun `./gradlew build test` → BUILD SUCCESSFUL in 1m 12s) | Screens 12-16 in feature:transactions:presentation + transactions domain use cases. First phase built on its own branch. See Phase 4 section below. |
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

## Phase 3 — Dashboard, Accounts, Card, bottom nav

Built by a dispatched subagent (general-purpose, opus), independently
re-verified in this session.

- **Build/test (reproduced independently):** `BUILD SUCCESSFUL in 2m 51s`;
  aggregated JUnit XML `tests=603 skipped=0 failures=0 errors=0` (226→603,
  +377 new).
- **Audit (reproduced independently):** no `kotlin.Result`, no `.toUi(`,
  no `Double` for money fields, no `@Ignore`, no `Route` import in any
  feature module, no hand-built `"https://..."` URLs outside tests, no
  cross-feature `projects.feature.*` deps, no TODO/FIXME in changed files.
- **core:network convention (the thing fixed before this phase) held**:
  read every new `*Api.kt` in feature:accounts/transactions/dashboard:data
  — every one is a one-line `dataHttpClient.get(route = ...)` through the
  shared helper; none hand-builds a URL or a status table. The brief's
  explicit callout of this rule, plus pointing at `TrueLayerUserInfoApi` as
  the reference pattern, worked.
- **Manual spot-check:** read `GetDashboardUseCase.kt` in full — correct
  parallel loading (`async`/`coroutineScope`), accounts failure short-circuits
  and cancels the rest, a 401 anywhere escalates to UNAUTHORIZED, one
  aggregated `Dashboard` returned so the ViewModel emits a single result
  through the sequential MVI queue rather than writing state from multiple
  coroutines. Confirmed Loading/Error/Empty/Offline composables are used in
  the Dashboard/Accounts/AccountDetail/Card screens.
- **Secrets check:** clean, as with Phase 2.
- **What was built:** feature:accounts:data (Accounts/Cards/Info APIs +
  mappers + Room-cached repos), feature:transactions:data (pulled forward:
  TransactionRepositoryImpl), feature:dashboard:data (direct debits/standing
  orders), feature:accounts:domain (net position, balance history, consent
  countdown, account detail, primary card), feature:dashboard:domain
  (greeting, payday, upcoming payments, safe-to-spend, cash flow, pending
  summary, spending-by-category, `GetDashboardUseCase`), full MVI sets for
  Home/Accounts/AccountDetail/Card, a redesigned shared composable set
  (BalanceCard, AccountCard, TransactionRow, floating-pill
  ClearSpendBottomNav, new OfflineBanner/ProviderLogo/DashedBorder), a new
  `core:data` offline-cache policy (`NetworkFirst`), and :app wiring
  (AppModule bindings, nav routes, 401→Connect Bank).
- **Deviations** (subagent's calls, reviewed and accepted as sensible):
  TransactionRepositoryImpl pulled forward from Phase 4 (Home/Account Detail
  need it); direct-debit/standing-order repos live in feature:dashboard:data
  since Home is their first consumer (Bills in Phase 6 reaches them only via
  the shared core:domain interfaces — no feature→feature coupling); card
  utilisation and the two loader use cases moved to **core:domain** (shared
  by Home and Accounts, which can't depend on each other); payday detection
  defined as "a credit from the same payer seen ≥2 times with a weekly/
  fortnightly/monthly gap"; sparkline reconstructed backwards from today's
  balance when `running_balance` is missing; AER shows "—" (not in the API);
  `SyncStatusStorage` added as a new interface rather than extending
  `UserPreferencesStorage` (would have broken 3 Phase-2 test fakes).
  **One worth tracking, not blocking:** `feature:accounts:data`'s new
  `InfoApi`/`TrueLayerInfoApi` and `feature:auth:data`'s existing
  `UserInfoApi`/`TrueLayerUserInfoApi` both independently call
  `GET /data/v1/info` for different purposes (Home greeting vs. biometric
  welcome). Not a dependency-rule violation (each feature:data module is
  self-contained), but worth consolidating in Phase 7 polish if it's easy.
- **Needs human:** emulator comparison of screens 09/10/11/21 against their
  PNGs (nothing beyond `@Preview` has been checked visually); run
  `DashboardScreenTest`/`AccountsScreensTest` on a device; real sandbox
  credentials to confirm actual direct-debit/standing-order status strings
  and frequency codes, which were mapped defensively without real responses.
- **Left undone (by design, later phases):** Transactions/Analytics/Bills/
  Notifications/Settings/Send screens remain placeholders; no pull-to-refresh
  gesture on Home (the reload action exists); Room cache isn't cleared on
  sign-out (Phase 7); `feature/transactions/domain/MyClass.kt` placeholder
  left for Phase 4.

## Phase 4 — Transactions

Built by a dispatched subagent (general-purpose), continuing from an
interrupted earlier run's uncommitted domain work (audited, kept, tests
added). Independently re-verified in this session.

- **Build/test (reproduced independently):** `./gradlew clean build test`
  hit the known `:app:packageDebug` flake (`BUILD FAILED in 1m 40s`, only
  that task); immediate rerun `./gradlew build test` → `BUILD SUCCESSFUL in
  1m 12s`, 1630 actionable tasks; aggregated JUnit XML `tests=799 skipped=0
  failures=0 errors=0` (603→799). Instrumented `TransactionsScreensTest`
  (7 tests) passed on emulator-5554 per the subagent ("OK (7 tests)").
- **Audit (reproduced independently, 67 changed .kt files):** no
  `kotlin.Result`, `.toUi(`, `@Ignore`, `Double`, `Route` in feature
  modules, hand-built URLs, sensitive logging, feature→feature deps, or
  gradle/catalog version changes; `feature:transactions:domain` uses only
  `clearspend.jvm.library`. £ literals appear only in `@Preview` fixtures and
  KDoc. No secrets in the diff; `local.properties` not staged.
- **States:** Transactions has Skeleton/Empty (incl. no search results)/
  Error/Offline + 401→Connect Bank; Detail has Skeleton/Error/Offline +
  401→Connect Bank (no Empty: a single item; not found = Error).
- **Screenshot comparison (emulator-5554, real composables fed through the
  real use cases + mappers by the instrumented test):** 12, 13, 14, 15, 16
  matched by the subagent; orchestrator personally compared 12 (top +
  groups), 15 and 16 against the PNGs: layout, colours, chips, dashed
  pending card, day nets, DD-only "manage in Bills" row all match. Live
  sandbox data not reachable — TrueLayer returns "Invalid redirect_uri"
  (needs human, below).
- **Deviations (accepted):** "Debit card" without •••• last4 (no card number
  on transactions); In/Out/Net summary covers loaded 30-day windows, not the
  whole period; raw domain rows for client-side search/filter held in a
  private ViewModel field, only touched inside sequential `handleAction`
  flows, reloaded after process death (reviewer may want this moved to a
  Room-backed query later); initial load skipped on restore only when
  restored content exists; "Report an issue" shows a "not available yet"
  toast (no spec); Cash rows show "Cash / ATM" per §6; `Transaction.
  providerReference` added (DB v1→v2, destructive fallback, cache only).
- **Watch:** `TransactionsUiState` holds the full loaded list — with "All"
  and many pages it could approach the Bundle size limit.
- **Left undone:** Bills route still a placeholder (Phase 6); no
  pull-to-refresh gesture (Retry/Refresh actions exist).

## Needs human (cumulative)

- **Register `clearspend://callback` as an allowed redirect URI in the
  TrueLayer Console** — the sandbox auth page currently shows "Invalid
  redirect_uri", blocking live-data checks of every screen.
- An emulator is now attached (`emulator-5554`), so visual checks from
  Phase 4 onward use real on-device captures.

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
