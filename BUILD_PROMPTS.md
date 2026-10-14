# ClearSpend — Build Prompts

How to use:
1. Start a fresh Claude Code session for each phase.
2. Paste **Part A (Shared Context)** first, then **one** phase prompt from Part B.
3. Don't start the next phase until the current one passes `./gradlew build` and `./gradlew test`. Review the diff, then commit.

Decisions already made (each phase relies on them):
- **Existing code:** audit and refactor it. Don't start from scratch, and don't leave it as it is.
- **Payments:** make real TrueLayer v3 sandbox calls behind `PaymentsApi`. Also add a debug-only `FakePaymentsApi` that can simulate every failure reason.
- **Charts:** draw them by hand with Compose `Canvas`. Add no chart library.
- **Mock provider:** use `uk-cs-mock`, as DESIGN_SPEC.md says. CLAUDE.md §8 still says `mock`, so Phase 2 updates CLAUDE.md to match.

---

## Part A — Shared Context (paste at the start of every phase)

```
You are building ClearSpend, a UK personal-finance Android app (Kotlin, Jetpack
Compose, Material 3) on the TrueLayer sandbox.

SOURCES OF TRUTH (read them all before writing any code):
1. CLAUDE.md: architecture, modules, MVI, Koin, Result type, naming, testing,
   build order. It wins on anything about code.
2. designs/DESIGN_SPEC.md: what each screen shows, where every value comes
   from (API field / Derived / Local / Static), enum lookups, required states,
   and how the prototype data differs from the real API. It wins on data.
3. designs/screens/NN-*.png: the visual source of truth. Open the matching
   PNG(s) with the Read tool before building any screen, and match layout,
   spacing, colors, typography and hierarchy. The screenshot wins on visuals.
4. designs/ClearSpend.dc.html: clickable prototype. Read its markup or styles
   when a screenshot leaves an exact dp, color or interaction unclear (e.g.
   the card shutter animation, sheet transitions, chip behaviour). Never copy
   its sample data. Section 8 of the spec lists how the prototype data differs
   from the API.

NON-NEGOTIABLE RULES (from CLAUDE.md and DESIGN_SPEC.md):
- Clean Architecture enforced by the module graph. Follow the dependency rules
  in CLAUDE.md §5 exactly. No feature depends on another feature.
- domain modules are pure Kotlin. Never mix clearspend.jvm.library with any
  clearspend.android.* plugin.
- Every screen uses MVI: UiState (@Parcelize, all fields defaulted, only
  primitives/enums/@Parcelize UI models), PartialState, Action, Effect
  (BUFFERED Channel), a ViewModel extending MviViewModel, and a Screen.
  The initial state is injected via Koin. Skip the initial load when
  isStateRestored is true.
- ViewModels never map or format. Domain → UI goes only through injected
  UiMapper interfaces and impls. Formatters (Amount/Date/Iban) take Locale and
  Clock in the constructor. Use no .toUi() extensions.
- Use com.edu.core.domain.util.Result<D, E : Error> everywhere, never
  kotlin.Result. Exceptions never cross data → domain.
- Create a UseCase only when it has real logic. Inject a pass-through
  repository directly. All derived values (safe-to-spend, payday, analytics,
  next DD date, monthly normalisation, net position, etc.) are use cases in the
  feature's :domain module, never computed in a Composable or reducer.
- Money is BigDecimal, never Double. Display it as £ with 2 dp in the en-GB
  locale. Dates use the UK format "16 Sep 2026", times are 24h "14:32". Debits
  are NEGATIVE in the TrueLayer API.
- Never hardcode sample values from the screenshots. Every number, name and
  date comes from an API call, a local store or a derivation, as listed in
  DESIGN_SPEC §4.
- Every data screen implements Loading (shimmer skeleton shaped like the
  content), Empty, Error ("Couldn't load your data" + Try again) and Content,
  plus an Offline banner where cached data exists (DESIGN_SPEC §7). A 401 after
  a failed refresh (DataError.NetworkError.UNAUTHORIZED) routes to Connect Bank.
- Feature screens never reference Route. They expose callbacks, and :app maps
  those callbacks to Navigation3 routes.
- All versions go in gradle/libs.versions.toml. Never hardcode a version in a
  module. If a convention plugin is missing something, fix it in build-logic.
- Never log tokens, secrets, auth codes, account numbers or full transaction
  descriptions. Credentials come only from local.properties → buildConfigField
  (sandbox only). Keep secret-dependent calls behind interfaces (AuthApi,
  PaymentsApi).
- Tests ship in the same phase as the code: use cases, repositories, data
  mappers (DTO → domain), UiMapperImpls (fixed Locale and Clock) and
  ViewModels (Action in → UiState out, fake mappers, MainDispatcherRule,
  OTOA/OTOS). Use JUnit4, MockK, Turbine and coroutines-test.

WORKING METHOD:
- Start by listing the files you will create or change per module, and wait for
  my OK only if something in the sources conflicts. Otherwise proceed.
- Before building any screen, confirm that both its DESIGN_SPEC section and its
  screenshot exist. If either is missing, stop and ask.
- After each module, run ./gradlew :<module>:build. At the end of the phase,
  run ./gradlew build and ./gradlew test, and paste the actual result. Don't
  claim success without that output.
- Finish with a short summary: what was built, which screenshots it matches,
  any deviations and why, and anything left for a later phase.
```

---

## Part B — Phase Prompts

### Phase 1 — Foundation: audit, refactor, and core modules

```
PHASE 1: FOUNDATION. Follow CLAUDE.md §15 Phase 1.

STEP 1: AUDIT EXISTING CODE (report first, then fix)
Some code already exists and partly breaks CLAUDE.md. Audit every .kt file
under core/, feature/ and app/, and produce a table:
file | keep / refactor / delete | reason. Known issues to fix:
- core/domain/model/{Account,Balance,Transaction}.kt use Double for money and
  are missing fields that the screens need. Remodel them from DESIGN_SPEC §4/§8
  and the TrueLayer field names: BigDecimal amounts; Transaction with
  timestamp (Instant), description, merchantName?, transactionType
  (CREDIT/DEBIT enum), transactionCategory (enum incl. PURCHASE, DIRECT_DEBIT,
  BILL_PAYMENT, TRANSFER, ATM, CASH, CREDIT, OTHER), classification
  List<String>, runningBalance?, isPending; Account with accountType enum
  (TRANSACTION, SAVINGS, BUSINESS_TRANSACTION, BUSINESS_SAVINGS), provider
  (displayName, logoUri), accountNumber (iban?, sortCode?, number?); Balance
  with available, current, overdraft, updateTimestamp. Also add the domain
  models Card, CardBalance, DirectDebit, StandingOrder and UserInfo, plus
  their repository interfaces in core:domain (Account, Balance, Transaction,
  Card, DirectDebit, StandingOrder, UserInfo repositories).
- core/presentation/ui/CurrencyFormatter.kt is leftover running-app code (km,
  pace). Delete it and replace it with the AmountFormatter, DateFormatter and
  IbanFormatter interfaces and impls in the .formatter package.
- core/domain/util/BaseUseCase.kt (FlowUseCase/SuspendUseCase/NoParams) is not
  part of the CLAUDE.md convention (plain classes with operator fun invoke).
  Remove it unless something legitimately needs it.
- feature/payments/domain uses package com.edu.feature.send. Rename it to
  com.edu.feature.payments.* to match the naming rules.
- Remove the MyClass.kt placeholders that get replaced.
- app/ui/theme/* duplicates the designsystem theme. Remove it and use
  ClearSpendTheme from core:presentation:designsystem.
- Check the existing shared composables (AccountCard, BalanceCard,
  TransactionRow, AmountText, CategoryIcon, ClearSpendBottomNav, LoadingScreen,
  ErrorScreen, EmptyStateScreen) against the screenshots and the "take UI
  models, never domain models or raw values" rule. Refactor them.
- Check that Result.kt, DataError.kt and Error.kt match CLAUDE.md §4 exactly
  (inline onSuccess/onError/map, asEmptyDataResult, EmptyResult).
- Check that the core:network plugins (AuthPlugin, TokenRefreshPlugin),
  core:data EncryptedDataStoreSecureStorage and core:domain
  TokenProvider/SecureDataStorage follow CLAUDE.md §8 Token Management
  (Tink-encrypted, expiry stored, refresh before expiry, UNAUTHORIZED on
  refresh failure). Fix any gaps.

STEP 2: BUILD THE MISSING FOUNDATION
- core:domain: final models, repository interfaces, Result type.
- core:network: Ktor CIO client, JSON, logging (with tokens redacted), base
  URLs from CLAUDE.md §8, a safe-call helper that maps exceptions and HTTP codes
  to DataError.NetworkError, the TrueLayer `{results, status}` envelope
  (DESIGN_SPEC §1.2), auth plus automatic token refresh.
- core:database: Room DB with entities and DAOs for accounts, balances,
  transactions (incl. pending flag), cards, direct debits, standing orders and
  saved payees. Store money as String or minor units, never REAL.
- core:data: secure token storage (Tink + DataStore) and DataStore prefs
  (consent date, last login, biometric enabled, notification toggles).
- core:presentation:designsystem: every color token in DESIGN_SPEC §2 /
  CLAUDE.md §9 (navy, navy2, blue, blueDark, mint, success/error/warning with
  their on-white text variants, surface, surfaceVariant, outline, onSurface,
  onSurfaceVariant), the Roboto + Roboto Mono type scale, shapes (cards 16–22dp,
  pill buttons, 26dp sheet), the 8dp spacing grid with 20dp screen padding,
  and the ClearSpendTheme. Add category colors and account-type colors from
  DESIGN_SPEC §6 as tokens.
- core:presentation:ui:
  * UiStateMachine, UiStateMachineImpl, asUiStateMachine and MviViewModel,
    exactly as in CLAUDE.md §4, with tests (ordering of intents, restoration
    from SavedStateHandle).
  * UiMapper + mapList; shared UI models AccountUi, BalanceUi, TransactionUi
    (@Parcelize, display-ready strings) and their mapper interfaces and impls.
    The enum lookups from DESIGN_SPEC §6 (category → method label / short /
    color; account_type → badge / icon / color) live in the mappers.
  * The formatters, and coreUiModule (Koin).
  * Shared composables from CLAUDE.md §9 matched to the screenshots:
    BalanceCard (09 hero), AccountCard (09 row), TransactionRow (12 rows,
    including the ↑/↓ badge, letter avatar, subtitle, amount and running
    balance), AmountText, CategoryIcon, ClearSpendBottomNav (floating pill
    ~64dp, 26dp radius, 12dp margin, raised 52dp blue Send FAB, active item =
    blue icon and label on a 10% blue pill), a shimmer LoadingScreen/Skeleton,
    ErrorScreen, EmptyState (56dp rounded icon square, title, hint, secondary
    button, as in 13-transactions-empty-state.png), OfflineBanner. Give each one
    a @Preview with fake UI models.

Tests: Result helpers, every formatter (fixed Locale.UK and a fixed Clock;
negative amounts, zero, large values, missing IBAN), every UiMapperImpl,
UiStateMachine, MviViewModel ordering, network error mapping, the token
refresh path (success, plus failure → UNAUTHORIZED).

Gate: ./gradlew build && ./gradlew test must pass.
```

### Phase 2 — Auth, start-up, and the app shell

```
PHASE 2: AUTH + APP SHELL. Follow CLAUDE.md §15 Phase 2.
Screens: 01 Splash (:app), 02–04 Onboarding, 05 Connect Bank, 06 Bank
Selection, 07 Connecting, 08 Biometric Login (all in feature:auth:presentation).
Read each PNG and DESIGN_SPEC §1, §3 and §4 ("01 Splash · 02-04 · 08").

First: update CLAUDE.md §8 to use providers=uk-cs-mock (plus
uk-ob-all uk-oauth-all where DESIGN_SPEC §1.1 says so) instead of "mock", so
the two documents agree.

- feature:auth:domain: AuthRepository (build auth URL, exchange code, has
  valid session, sign out), the consent-date and last-login contracts, and use
  cases only where there is real logic (e.g. DetermineStartDestinationUseCase:
  no token → onboarding/connect; token valid → biometric if enabled, else
  dashboard; expired → refresh → dashboard; refresh failed → connect bank).
- feature:auth:data: AuthApi (the interface that hides the secret, so a backend
  can replace it later), OAuth URL builder (space-separated scopes from
  DESIGN_SPEC §1 incl. info and cards, URL-encoded, random `state`),
  POST /connect/token code exchange, tokens saved through Tink, consent date
  saved to DataStore. Credentials go in local.properties → buildConfigField
  (add the keys and document them in a README note; never commit real
  values).
- Bank Selection (06): a static provider list is fine. Selecting a bank sets
  `providers=`, and sandbox always includes uk-cs-mock.
- Connect Bank (05): opens the URL in a Chrome Custom Tab (androidx.browser is
  already in the catalog).
- :app: Application class + startKoin (appModule, coreUiModule and the
  feature modules); MainActivity handles the clearspend://callback deep link
  (manifest intent-filter from CLAUDE.md §8, no autoVerify), validates `state`
  and forwards the code to the auth ViewModel; edge-to-edge; the Navigation3
  host with the full sealed Route set from CLAUDE.md §10; the start-up flow
  from §10; ClearSpendBottomNav shown only on main routes, with placeholder
  screens for tabs that later phases build.
- Connecting (07): shown while the token exchange runs. It handles error and
  retry, and the state survives process death (the code and state live in
  UiState).
- Biometric (08): add androidx.biometric to the version catalog. Use
  BiometricPrompt. "Last login" is a Local value written on every successful
  unlock. Offer a fallback when biometrics aren't available or aren't
  enrolled.
- Onboarding (02–04): a pager with the static copy from the screenshots and
  the page indicator from the design.

Tests: auth URL builder (scopes encoded, state present), state-mismatch
rejection, token exchange repository (success, 401, network error), the
start-destination use case (every branch), and every ViewModel.

Gate: build + test pass. Then manually log in on an emulator with the
Mock bank (john / doe) and confirm you land on the Dashboard placeholder.
```

### Phase 3 — Accounts, Dashboard, Card, and bottom navigation

```
PHASE 3: DASHBOARD. Follow CLAUDE.md §15 Phase 3.
Screens: 09 Home (feature:dashboard:presentation), 10 Accounts, 11 Account
Detail, 21 Card (feature:accounts:presentation). Read each PNG and DESIGN_SPEC
§4 (09, 10, 11, 21), §6 and §7.

- feature:accounts:data: AccountRepositoryImpl, BalanceRepositoryImpl,
  CardRepositoryImpl (GET /data/v1/cards + /cards/{id}/balance) and
  UserInfoRepositoryImpl (GET /data/v1/info), with DTOs and data mappers.
  These are bound in :app's appModule only. Cache in Room so the app works
  offline, showing the "Showing data from {time}" banner.
- feature:accounts:domain use cases with real logic: net position (Σ available
  − Σ card current), Current/Savings/Card-owed split by account_type, 30-day
  change and 8-week sparkline rebuilt from running_balance, consent days
  remaining (consent date + 90 − today), card utilisation band (<40% green,
  <70% amber, ≥70% red).
- feature:dashboard:domain use cases: GetRecentTransactionsUseCase (primary
  account, newest 5, as in CLAUDE.md §4), safe-to-spend (Σ current-account
  available + overdraft − upcoming bills before payday), days to payday (next
  expected date of the largest recurring CREDIT), the Bills due / Spent / Yours
  bar, money in/out for the month plus the % change vs last month, pending
  count and sum, the next 3 upcoming payments (SO next_payment_date; DD =
  previous_payment_timestamp + 1 month), "Where your money went" (month's
  DEBITs grouped by classification[0]), and the greeting (by device time,
  first word of full_name).
  Load in parallel where DESIGN_SPEC says so, but emit through the sequential
  MVI queue. Don't launch concurrent writers to state.
- Home (09): navy header with avatar → Settings and bell → Notifications; blue
  hero BalanceCard; horizontal account cards ending with "Connect another bank"
  → Bank Selection; quick actions (Send, Request, Analytics, Statements); the
  safe-to-spend navy card with a 3-colour bar and an "N days left" chip; Money
  in/out tiles; the amber pending banner → Transactions; Upcoming Payments →
  Bills; Where your money went → Analytics; the card summary → Card; Recent
  Transactions → Transaction Detail. "Request" and "Statements" may show a
  "Coming soon" snackbar effect, since no spec exists for them.
- Accounts (10): refresh with a "Syncing…" state, net position + sparkline,
  the consent renewal banner (Renew re-runs auth), filter chips by type,
  provider groups (logo via Coil, falling back to an initial and colour),
  account rows with badges, an overdraft cell (AER shows "—" because it's not
  in the API), the credit-card block, and "Connect another bank".
- Account Detail (11): balances, sort code / number / IBAN in Roboto Mono,
  copy buttons (ClipboardManager + "Copied!" for 1.2 s), and the 3 latest
  transactions.
- Card (21): card visual, balance/limit/statement/payment-due, utilisation
  bar, and the tap shutter animation (8 strips, graphicsLayer rotationX, 35 ms
  stagger, colour swap at 46%). Check ClearSpend.dc.html for the exact motion.
- Bottom nav: wire all 5 tabs. Send opens a ModalBottomSheet placeholder
  (built in Phase 5). Each tab keeps its own back stack where sensible.

Tests: every repository and data mapper, every use case above (edge cases: no
accounts, no salary found, no cards, null running_balance, month boundaries),
every new UiMapper, and every ViewModel. Add a Compose UI test for at least the
Home loading → content transition.

Gate: build + test pass. Run on an emulator against the sandbox and compare
each screen to its PNG side by side. List any visual differences you could not
resolve.
```

### Phase 4 — Transactions

```
PHASE 4: TRANSACTIONS. Follow CLAUDE.md §15 Phase 4.
Screens: 12 Transactions, 13 Empty state, 14 Detail (settled), 15 Detail
(pending), 16 Detail (direct debit), in feature:transactions:presentation.
Read the PNGs and DESIGN_SPEC §4 (12, 14-16) and §6.

- feature:transactions:data: TransactionRepositoryImpl
  (GET /accounts/{id}/transactions?from&to and /transactions/pending), DTOs
  and mappers (amount sign kept, running_balance optional, pending flag),
  cached in Room.
- feature:transactions:domain: merge "All accounts" sorted by timestamp;
  period → from/to (This Month, Last Month, Custom Range, All); window
  pagination (30 days, extended on scroll); client-side search over
  merchant_name ∨ description; category filter; group by date with the daily
  net; the In/Out/Net summary and daily spending bars; the merchant insight
  (Σ and count of the same merchant this month).
- Transactions (12): account switcher dropdown, navy summary card with daily
  bars (Canvas), search field, period chips (Custom Range uses the Material 3
  DateRangePicker), category chips, a pending section (dashed amber card, "Not
  settled", no running balance), date-group headers with the day net, and
  TransactionRows. Empty state as in 13 (also for "no search results").
- Transaction Detail (14–16): Settled/Pending badge, merchant, amount, date
  and time, classification chips, account · provider, payment method (§6),
  reference, balance after, merchant insight, "Manage in Bills" only for
  DIRECT_DEBIT (→ Bills route via a callback), and the transaction ID
  (monospace, copyable). The route argument is the transactionId. Load the
  transaction from the repository/cache; never pass domain objects through
  navigation.

Tests: repository, mappers, the pagination/period/search/filter/grouping
use cases, the TransactionUiMapper edge cases (negative, pending, null running
balance, CREDIT), and the ViewModels.

Gate: build + test pass. Compare with 12–16 on the emulator.
```

### Phase 5 — Send Money (Payments API v3)

```
PHASE 5: SEND MONEY. Follow CLAUDE.md §15 Phase 5.
Screens 24–38 (feature:payments:presentation), shown as a ModalBottomSheet over
the current tab (26dp top corners). Read every PNG 24–38 and DESIGN_SPEC §5 in
full. Its v3 correction overrides older wording in CLAUDE.md §8.

DATA LAYER (feature:payments:data)
- PaymentsApi interface (the backend-swap seam), with two implementations:
  1. TrueLayerPaymentsApi (real sandbox): client_credentials token (scope
     payments), POST /v3/payments with the exact body in DESIGN_SPEC §5, a
     Tl-Signature header (signing key from local.properties via
     buildConfigField; use TrueLayer's request-signing library if it's
     available on Maven, otherwise implement ES512 JWS per their docs), an
     Idempotency-Key header, GET /v3/payments/{id} polling, and
     GET /v3/payments-providers.
  2. FakePaymentsApi (debug builds only): simulates the status progression
     and can be forced into insufficient_funds, canceled,
     invalid_account_details or provider_rejected.
  :app binds the fake when BuildConfig.DEBUG is true and
  TRUELAYER_SIGNING_KEY is missing. Otherwise it binds the real one.
- Authorisation: add the TrueLayer Android payments SDK
  (com.truelayer.payments:ui) to the version catalog and launch it with id +
  resource_token. If the SDK can't be resolved, fall back to the Hosted
  Payment Page in a Custom Tab, and tell me which one you used.
- SavedPayeeRepository (Room), and recent payees derived from TRANSFER DEBITs.

DOMAIN (feature:payments:domain)
- Amount validation: OK (≤ available) / OVERDRAFT (amber, still allowed) /
  OVER_LIMIT (> available + overdraft; button disabled).
- Sort code / account number validation; a local sort-code → bank lookup table
  (hide the line if not found).
- Confirmation-of-Payee result: MATCH / CLOSE_MATCH / UNVERIFIED, where
  UNVERIFIED is the default because sandbox doesn't support CoP.
- Status mapping: authorization_required → authorizing → authorized →
  executed → settled, or failed + failure_reason → the 4 failure kinds in the
  DESIGN_SPEC §5 table.
- Generate an Idempotency-Key once per payment attempt and REUSE it on "Try
  again" after a network failure.

PRESENTATION: one SendMoneyViewModel drives all 6 steps. The UiState holds
the step, from-account, payee fields, amount, reference, paymentId,
idempotency key and status, so a payment in progress survives process death.
- Step 1 Payee (24 empty, 25 verified, 26 name mismatch, 27 from-account
  picker with TRANSACTION accounts only), plus a save-payee toggle.
- Step 2 Amount (28 ok, 29 overdraft, 30 over limit).
- Step 3 Confirm (31): summary, Faster Payments, Free, "Usually within
  seconds", "Approve in {bank}". The failure-reason debug chips at the bottom
  of 31 appear ONLY in debug builds with FakePaymentsApi, and they choose the
  simulated outcome.
- Step 4 Authorising / Executing (32, 33): status timeline driven by polling.
- Step 5 Success (34): amount, "Executed · settling", new balance (re-fetch
  it), payment ID.
- Step 6 Failure (35–38): title, body and primary button per reason, routing
  back to step 2, step 3 or step 1, or "Contact support", as in the table.
- Effects: close the sheet, open the SDK/HPP, show a snackbar.

Tests: every validation and mapping use case, idempotency-key reuse, status
polling (Turbine), both PaymentsApi impls (MockK engine for the real one), and
the ViewModel step transitions incl. restoration mid-flow.

Gate: build + test pass. Walk through all 15 states (24–38) on the emulator
with the fake, then at least one real sandbox payment if a signing key is set
up.
```

### Phase 6 — Analytics and Bills

```
PHASE 6: ANALYTICS. Follow CLAUDE.md §15 Phase 6.
Screens: 17 Analytics, 18 Analytics (category focused), 19 Bills (direct
debits), 20 Bills (standing orders), in feature:analytics:presentation. Read
the PNGs and DESIGN_SPEC §4 (17-18, 19-20).

- feature:analytics:data: fetch 6 months of transactions once (from = today
  − 6 months), plus direct_debits and standing_orders, all cached in Room.
  Reuse the core:domain repository interfaces; implementations stay in their
  owning feature's data module, wired in :app.
- feature:analytics:domain: one use case per derivation in the DESIGN_SPEC 17-18
  table: month switcher range; saved and % of income; in/out bars; the biggest
  % change insight; the category donut (DEBITs by classification[0]) with Δ%
  vs last month; spending pace (cumulative daily DEBIT, this month vs last);
  the last 6 months in/out/saved; bills vs everyday + per-day everyday; the
  top 5 merchants. For Bills: DD next date = previous_payment_timestamp + 1
  month; SO rows; the monthly total normalised by frequency.
- Charts are all hand-drawn with Compose Canvas: donut (drawArc, tap a segment
  to focus → screen 18), pace (Path, solid vs dashed), 6-month grouped bars
  (tap selects a month), progress bars. Add contentDescription / semantics
  summaries for accessibility.
- Analytics (17/18) and Bills (19/20, tabs: Direct Debits / Standing Orders,
  with status badges). Bills is reached from Home "Upcoming payments" and
  Transaction Detail "Manage in Bills".

Tests: every analytics use case with fixed-clock fixtures (month boundaries,
leap years, empty months, no income → avoid dividing by zero, ties in top
merchants), the frequency normalisation, mappers, and ViewModels.

Gate: build + test pass. Compare 17–20 on the emulator.
```

### Phase 7 — Polish: Settings, Notifications, hardening, CI

```
PHASE 7: POLISH. Follow CLAUDE.md §15 Phase 7.
Screens: 22 Notifications, 23 Profile & Settings (both in :app). Read the PNGs
and DESIGN_SPEC §4 (22, 23).

- Settings (23): GET /data/v1/info → full_name, emails; DataStore toggles
  (biometric, notifications, thresholds); connected banks = distinct provider
  display names; Sign out = delete tokens + clear the Room cache + reset
  consent → Connect Bank, with a confirmation dialog.
- Notifications (22): a WorkManager periodic sync job (koin-androidx-workmanager
  is already in the catalog) that diffs new transactions and balances and
  creates local notifications: large DEBIT over the threshold, CREDIT
  received, DD/SO due tomorrow, payment status changed. Store them in Room,
  group them Today / Yesterday / This Week, mark them read, and drive the
  unread dot on the Home bell. Request POST_NOTIFICATIONS on API 33+.
- Hardening: biometric re-lock after N minutes in the background; offline-first
  everywhere (Room as the source of truth, with a network refresh); a single
  UNAUTHORIZED → re-auth path; consistent Error/Empty/Offline states on every
  screen; edge-to-edge insets checked on every screen; a basic TalkBack pass;
  dark status-bar legibility on navy headers.
- Audit: grep for Double money, kotlin.Result, .toUi(), formatting in
  ViewModels or Composables, hardcoded sample values, Timber logs of sensitive
  data, feature → feature dependencies, and hardcoded versions. Fix every hit.
- GitHub Actions: a workflow on PRs that runs ./gradlew build test with JDK 17
  and Gradle caching, injecting dummy local.properties values.
- README: setup (local.properties keys), the sandbox login (john/doe), and the
  security note that client_secret and the signing key must move to a backend
  for production.

Gate: build + test pass, CI green, and a full manual pass through all 38
screenshots on an emulator.
```
