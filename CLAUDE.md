# ClearSpend

## 1. Project Overview

ClearSpend is a personal-finance Android app
(package `com.edu.clearspend`). It connects to
TrueLayer Open Banking API to pull account,
balance, and transaction data, then stores and
encrypts it locally. Features: dashboard,
transaction lists, send money (payments), and
spend analytics.

**Current state:** Multi-module skeleton and Gradle
build system fully built. Almost no feature code
exists yet. Every module below `app` currently
contains only Android Studio default template files.
Treat this file as the intended architecture and
conventions to build into, not finished features.

**App name:** ClearSpend
**Package:** com.edu.clearspend
**Banking API:** TrueLayer (Open Banking, UK)
**Target market:** UK personal finance

**Security note (read first):** In this project the
TrueLayer client secret and the payments signing key
are read from `local.properties` into `BuildConfig`.
That is acceptable for sandbox learning only, because
anything in the APK can be extracted. For production,
the token exchange, token refresh, payment creation and
request signing MUST move to a backend that holds the
secret. Do not treat the on-device approach as the final
design. Keep all secret-dependent calls behind
interfaces (e.g. `AuthApi`, `PaymentsApi`) so the
backend swap only touches `*:data` modules.

---

## 2. Module Structure

25 Gradle modules total (`app` + 24 library modules).

:app
Composition root, Activity, DI wiring (Koin),
Navigation host, Bottom navigation, Route definitions

:core:domain
Shared domain models, Result type, repository
interfaces, shared use cases
Pure Kotlin — no Android dependencies

:core:database
Room database, DAOs, entities

:core:network
Ktor HttpClient setup, base API config,
TrueLayer auth interceptor, token refresh

:core:data
Shared repository implementations
Depends on domain/database/network

:core:presentation:designsystem
Compose theme, color tokens, typography,
base Material3 widgets

:core:presentation:ui
UiStateMachine, MviViewModel, UiMapper interface,
formatters, shared UI models and shared UI mappers,
shared composables built on designsystem

:feature:auth:domain
:feature:auth:data
:feature:auth:presentation
TrueLayer OAuth2 flow, Connect Bank screen,
Bank Selection screen, Biometric login

:feature:dashboard:domain
:feature:dashboard:data
:feature:dashboard:presentation
Home screen, balance hero card,
account cards, recent transactions

:feature:payments:domain
:feature:payments:data
:feature:payments:presentation
Send money flow, TrueLayer Payments API v3,
6-step payment journey

:feature:accounts:domain
:feature:accounts:data
:feature:accounts:presentation
Account list, account detail,
balance breakdown, IBAN display, card screen

:feature:transactions:domain
:feature:transactions:data
:feature:transactions:presentation
Transaction list, search, filter by category,
date range, transaction detail

:feature:analytics:domain
:feature:analytics:data
:feature:analytics:presentation
Spending pie chart, monthly bar chart,
top merchants, income vs expenses,
bills and subscriptions screen

---

## 3. Tech Stack & Dependencies

- **Language:** Kotlin 2.4.10, JVM target 17
- **Build:** AGP 9.3.1, Gradle version catalogs
  (`gradle/libs.versions.toml`), build-logic
  convention plugins. All dependency versions live in
  the catalog — never hardcode versions in modules.
- **UI:** Jetpack Compose (BOM 2026.06.01),
  Material3, Navigation3 (androidx.navigation3),
  adaptive layouts
- **DI:** Koin (koin-core, koin-android,
  koin-androidx-compose, koin-compose-viewmodel,
  koin-compose-navigation3)
  ⚠️ Not yet initialized — no startKoin or
  Application class exists yet
- **Networking:** Ktor client (CIO engine,
  content negotiation, kotlinx-serialization JSON,
  logging, auth plugin)
  Used for ALL TrueLayer API calls
- **Persistence:** Room (core:database),
  DataStore Preferences for key/value
- **Security:** Google Tink (tink-android)
  Encrypts tokens and sensitive data at rest
- **Async:** Kotlin Coroutines + Flow
- **Images:** Coil (Compose integration)
- **Logging:** Timber (never log tokens, secrets,
  account numbers or full transaction descriptions)
- **Crash/Distribution:** Firebase (BoM 34.16.0),
  Firebase App Distribution, Google Services
- **Testing:** JUnit4, MockK, Turbine,
  kotlinx-coroutines-test, Espresso,
  Compose UI test

### Convention Plugins (build-logic/convention)

| Plugin | Applies | Used by |
|--------|---------|---------|
| `clearspend.jvm.library` | kotlin/jvm, JVM17, test deps | `*:domain` modules |
| `clearspend.android.library` | com.android.library, Kotlin | base Android lib modules |
| `clearspend.android.data` | android.library + Koin | `core:data` |
| `clearspend.android.room` | androidx.room + KSP | `core:database` |
| `clearspend.android.library.compose` | android.library + Compose + kotlin-parcelize | `core:presentation:*` |
| `clearspend.android.ui.feature` | library.compose + ui + designsystem + Koin + Nav3 + kotlin-parcelize | every `*:presentation` |
| `clearspend.jvm.ktor` | serialization + ktor bundle | every `*:data` + `core:network` |
| `clearspend.android.application(.compose)` | com.android.application + Compose | `:app` only |

⚠️ IMPORTANT: Never mix `clearspend.jvm.library`
with any `clearspend.android.*` plugin in the same
module — both try to register the Kotlin Gradle
Plugin's `kotlin` extension and will fail with:
`Cannot add extension with name 'kotlin'`

⚠️ `kotlin-parcelize` is applied by the
`clearspend.android.library.compose` and
`clearspend.android.ui.feature` convention plugins, so
no `*:presentation` module needs to add it manually.
If it is missing from those plugins, add it there
(build-logic), not in individual modules.

---

## 4. Architecture

**Clean Architecture** enforced by module graph.
**MVI pattern** for all screens.

### Layer Rules
- **domain** — pure Kotlin, no Android.
  Models, Result type, repository interfaces, use cases.
  Never imports `android.os.Parcelable` or
  `kotlinx.parcelize`.
- **data** — Android library. Implements domain
  repository interfaces, owns Ktor network calls
  and Room/DataStore access, maps DTOs to domain models.
- **presentation** — Android + Compose.
  Screens, ViewModels, UI state, UI models, and UI mappers.
  Domain models are pure Kotlin and NOT Parcelable, so
  UiState never holds them directly. Presentation defines
  @Parcelize UI models (AccountUi, TransactionUi,
  BalanceUi, ...) and converts domain → UI ONLY through
  injected `UiMapper` classes. ViewModels never convert.

---

### Result Type (core:domain)

Every use case and repository method that can fail
returns `Result<D, E>`, generic over both the success
type and a typed error (`E : Error`). Exceptions never
cross the data → domain boundary: repository
implementations catch them and return `Result.Error`.

```kotlin
// core/domain/.../util/Error.kt
package com.edu.core.domain.util

interface Error

// core/domain/.../util/DataError.kt
package com.edu.core.domain.util

sealed interface DataError : Error {
    enum class NetworkError : DataError {
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
        NOT_FOUND,
    }
    enum class LocalError : DataError {
        DISKFULL,
    }
}

// core/domain/.../util/Result.kt
package com.edu.core.domain.util

sealed interface Result<out D, out E> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E>(val error: E) : Result<Nothing, E>
}

// These MUST be inline (and their lambdas NOT crossinline)
// so that suspend calls such as flow { emit(...) } work
// inside the lambdas.
inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T, E : Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

inline fun <T, E : Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
    }
}

fun <T, E : Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map { }
}

typealias EmptyResult<E> = Result<Unit, E>
```

Rules:
- This type shadows `kotlin.Result`. Always import
  `com.edu.core.domain.util.Result` explicitly. Never use
  `kotlin.Result` in this project.
- `onError`'s lambda receives the unwrapped error value
  (`E`), not a `Result.Error` wrapper — call sites write
  `.onError { error -> ... }` where `error` is e.g. a
  `DataError` constant.
- `DataError.NetworkError.UNAUTHORIZED` (refresh failed /
  401) is what triggers the forced re-authentication flow.
- Use cases are plain classes with `operator fun invoke`
  (suspend where needed) returning `Result<T, DataError>`
  (or another `Error` subtype where appropriate).
- Only create a UseCase when it carries real behavior —
  combining multiple repositories, filtering/limiting,
  picking a primary account, validation, business rules.
  A UseCase that just forwards one call to one repository
  method with no added logic must be eliminated: inject
  the `Repository` interface (from `core:domain` or the
  feature's own `:domain` module) into the ViewModel
  directly instead.

---

### MVI Pattern (follow for every new screen)

Three production rules apply to every ViewModel
in this project:

**Rule 1 — Never hardcode initial UiState.**
Always inject it via constructor so tests can
start from any state without setter chains.
Follows OTOA (one test, one assertion) and
OTOS (one test, one state) principles.

**Rule 2 — Never use MutableStateFlow to hold
screen state.**
MutableStateFlow does not survive process death.
Use UiStateMachine (backed by SavedStateHandle)
instead. This is critical in a banking app where
losing payment or auth state mid-flow is a real
user-facing risk.

**Rule 3 — Never launch concurrent coroutines
that write to the same state.**
MutableStateFlow.update() is atomic but not
ordered. Two concurrent jobs writing to the same
state produce a race condition where the last
network response wins, not the last user action.
Use the MviViewModel base class with intent
queuing and a pure reduceUiState function to
guarantee submission order.

**Rule 4 — ViewModels never map or format.**
A ViewModel never builds a UI model, never formats an
amount or date, and never calls `.toUi()`-style
extensions. It calls injected `UiMapper` instances
(see "UI Models and UiMappers" below).

---

### UiStateMachine (core:presentation:ui)

Lives in :core:presentation:ui so every
*:presentation module can access it via the
clearspend.android.ui.feature plugin with
no extra wiring.

Every UiState class must be @Parcelize so
SavedStateHandle can serialise it to Bundle
(kotlin-parcelize comes from the convention plugins).

```kotlin
// Interface
interface UiStateMachine<UI_STATE : Parcelable> :
    ReadOnlyProperty<Any?, StateFlow<UI_STATE>> {

    val isStateRestored: Boolean

    fun update(block: UI_STATE.() -> UI_STATE)
}

// Implementation
class UiStateMachineImpl<UI_STATE : Parcelable>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    fallback: UI_STATE,
) : UiStateMachine<UI_STATE> {

    override val isStateRestored: Boolean
    private val uiState: MutableStateFlow<UI_STATE>

    init {
        val restoredUiState: UI_STATE? = savedStateHandle[key]
        val currentUiState = restoredUiState ?: fallback
        isStateRestored = restoredUiState != null
        uiState = MutableStateFlow(currentUiState)
    }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): StateFlow<UI_STATE> = uiState

    override fun update(block: UI_STATE.() -> UI_STATE) {
        val newState = uiState.value.block()
        uiState.value = newState
        savedStateHandle[key] = newState
    }
}

// Builder extension on SavedStateHandle
fun <UI_STATE : Parcelable> SavedStateHandle.asUiStateMachine(
    fallback: UI_STATE,
    key: String = "ui_state",
): UiStateMachine<UI_STATE> = UiStateMachineImpl(
    savedStateHandle = this,
    key = key,
    fallback = fallback,
)
```

---

### MviViewModel Base Class (core:presentation:ui)

Abstract base class all ViewModels extend.
Lives in :core:presentation:ui alongside
UiStateMachine.

Uses flatMapConcat (not flatMapConcurrently)
to process intents sequentially in submission
order. This avoids the race condition where two
concurrent coroutines write state in network
completion order rather than user action order.
flatMapConcat comes from kotlinx.coroutines and is
marked @FlowPreview, so the base class opts in with
`@OptIn(FlowPreview::class)`. No unmerged or external
dependencies are required.

Effects (one-off events like navigation and
snackbars) use a separate Channel and are NOT
folded into UiState. The base class does not
manage effects — each ViewModel declares its
own effect Channel as needed. This is intentional:
effects are fire-and-forget, not restorable state.

```kotlin
@OptIn(FlowPreview::class)
abstract class MviViewModel<
    UI_STATE : Parcelable,
    UI_PARTIAL_STATE,
    UI_ACTION,
>(
    savedStateHandle: SavedStateHandle,
    initialState: UI_STATE,
) : ViewModel() {

    private val actions = Channel<UI_ACTION>(Channel.UNLIMITED)

    private val uiStateMachine: UiStateMachine<UI_STATE> =
        savedStateHandle.asUiStateMachine(initialState)

    // Exposed to subclasses so they can skip the initial
    // load after process-death restoration. The state
    // machine itself stays private.
    protected val isStateRestored: Boolean
        get() = uiStateMachine.isStateRestored

    val uiState: StateFlow<UI_STATE> by uiStateMachine

    init {
        viewModelScope.launch {
            actions.receiveAsFlow()
                .flatMapConcat(transform = ::handleAction)
                .scan(uiState.value, ::reduceUiState)
                .collect { newState ->
                    uiStateMachine.update { newState }
                }
        }
    }

    fun onAction(action: UI_ACTION) {
        viewModelScope.launch { actions.send(action) }
    }

    protected abstract fun handleAction(
        action: UI_ACTION
    ): Flow<UI_PARTIAL_STATE>

    protected abstract fun reduceUiState(
        previousState: UI_STATE,
        partialState: UI_PARTIAL_STATE,
    ): UI_STATE
}
```

⚠️ Keep reduceUiState pure — no suspend calls,
no side effects, no repository access, no mapping
inside it. Any async work belongs in handleAction.

⚠️ Skip the initial data load when state is
restored to avoid overwriting recovered state:

```kotlin
init {
    if (!isStateRestored) {
        onAction(DashboardAction.LoadData)
    }
}
```

---

### Per-Screen MVI Structure

Every screen has exactly these files:

[Feature]UiState.kt — screen state (Parcelable)
[Feature]PartialState.kt — intermediate results from handleAction
[Feature]Action.kt — user intents
[Feature]Effect.kt — one-off navigation/snackbar events
[Feature]ViewModel.kt — extends MviViewModel
[Feature]Screen.kt — Composable, collects state and effects

---

#### UiState Rules

```kotlin
// 1. Must be @Parcelize — required for UiStateMachine
// 2. All fields must have defaults — required for restoration
// 3. Immutable — no var fields, only val
// 4. Fields may only be primitives, Strings, enums,
//    or @Parcelize UI models. NEVER domain models.

@Parcelize
data class DashboardUiState(
    val isLoading: Boolean = false,
    val accounts: List<AccountUi> = emptyList(),
    val balance: BalanceUi? = null,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val error: String? = null
) : Parcelable
```

---

### UI Models and UiMappers

Domain models live in :core:domain (pure Kotlin, not
Parcelable). Presentation code uses @Parcelize UI
models. Conversion is done by small, single-purpose,
injected mapper classes. This keeps ViewModels free of
mapping and formatting logic (SRP) and lets tests swap
mappers (DIP).

**Placement**

- A UI model or mapper used by more than one feature
  lives in `:core:presentation:ui` (features cannot
  depend on each other). Shared: `AccountUi`, `BalanceUi`,
  `TransactionUi` and their mappers.
- A UI model or mapper used by one feature only lives in
  that feature's `:presentation` module.
- Packages: `com.edu.core.presentation.ui.model`,
  `.mapper`, `.formatter` for shared code;
  `com.edu.feature.<feature>.presentation.model` and
  `.mapper` for feature code.
- `:core:presentation:ui` may depend on `:core:domain`
  (mapper inputs) and `:core:presentation:designsystem`.

**The contract (core:presentation:ui)**

```kotlin
interface UiMapper<in DOMAIN, out UI> {
    fun map(domain: DOMAIN): UI
}

fun <D, U> UiMapper<D, U>.mapList(list: List<D>): List<U> =
    list.map(::map)
```

**One interface + one implementation per model.**
Each model gets its own small interface extending
`UiMapper`, so Koin can bind it without generic-type
erasure clashes and ViewModels depend on abstractions.

```kotlin
// core/presentation/ui/.../model/AccountUi.kt
@Parcelize
data class AccountUi(
    val accountId: String,
    val displayName: String,
    val currency: String,
    val maskedIban: String?,
) : Parcelable

// core/presentation/ui/.../mapper/AccountUiMapper.kt
interface AccountUiMapper : UiMapper<Account, AccountUi>

// core/presentation/ui/.../mapper/AccountUiMapperImpl.kt
class AccountUiMapperImpl(
    private val ibanFormatter: IbanFormatter,
) : AccountUiMapper {
    override fun map(domain: Account) = AccountUi(
        accountId = domain.accountId,
        displayName = domain.displayName,
        currency = domain.currency,
        maskedIban = domain.iban?.let(ibanFormatter::mask),
    )
}
```

**Formatters** (`core:presentation:ui/.../formatter`):
`AmountFormatter`, `DateFormatter`, `IbanFormatter` are
interfaces with implementations that take `Locale` and
`Clock` through the constructor. They never take an
Android `Context`. Mappers receive them by constructor.

**Rules**

- Mappers are pure: no Android context, no coroutines,
  no repository or network calls, no static formatter
  calls.
- Mappers own ALL formatting (amount strings, dates,
  masked IBAN, category labels). ViewModels and
  Composables contain none.
- UI models hold display-ready values (Strings, epoch
  millis, enums), never raw `Instant`/`BigDecimal`.
- ViewModels receive mapper interfaces through the
  constructor and only call `map(...)` / `mapList(...)`
  when building a PartialState. No `.toUi()` extension
  functions anywhere.
- PartialState and UiState only ever see UI models.

---

#### PartialState Rules

```kotlin
// Carries one result from handleAction
// Sealed so reduceUiState is exhaustive
// Named after what changed, not what caused it
// Holds UI models only

sealed interface DashboardPartialState {
    data class AccountsLoaded(val accounts: List<AccountUi>) : DashboardPartialState
    data class BalanceLoaded(val balance: BalanceUi) : DashboardPartialState
    data class TransactionsLoaded(val transactions: List<TransactionUi>) : DashboardPartialState
    data class LoadingChanged(val isLoading: Boolean) : DashboardPartialState
    data class ErrorOccurred(val message: String) : DashboardPartialState
}
```

---

#### Action Rules

```kotlin
// Sealed so handleAction is exhaustive
// Named after user intent, not implementation

sealed interface DashboardAction {
    data object LoadData : DashboardAction
    data object RefreshData : DashboardAction
    data class AccountSelected(
        val accountId: String
    ) : DashboardAction
    data class TransactionSelected(
        val transactionId: String
    ) : DashboardAction
}
```

---

#### Effect Rules

```kotlin
// Separate from UiState — not persisted
// Channel-based, not StateFlow
// Declared directly in ViewModel, not in base class
// Use a BUFFERED channel so sending never blocks the
// sequential intent queue while the UI is not collecting.

sealed interface DashboardEffect {
    data class NavigateToAccount(
        val accountId: String
    ) : DashboardEffect
    data class NavigateToTransaction(
        val transactionId: String
    ) : DashboardEffect
    data class ShowError(val message: String)
        : DashboardEffect
}
```

Navigation effects are handled in the Screen through
callbacks (e.g. `onNavigateToAccount(accountId)`)
supplied by `:app`. Features never reference `Route`.

---

### Complete ViewModel Example

```kotlin
// DashboardViewModel import sources:
// AccountRepository            → :core:domain ✅ (interface; no UseCase — pass-through only, see section 4 rules)
// BalanceRepository            → :core:domain ✅ (interface; no UseCase — pass-through only, see section 4 rules)
// GetRecentTransactionsUseCase → :feature:dashboard:domain ✅ (kept — real logic: primary account + limit 5)
// AccountUiMapper, BalanceUiMapper, TransactionUiMapper
//                              → :core:presentation:ui ✅
// Result, onSuccess, onError   → :core:domain ✅
// All rules from section 5 satisfied — no feature-to-feature deps

class DashboardViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: DashboardUiState,
    private val accountRepository: AccountRepository,
    private val balanceRepository: BalanceRepository,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val accountMapper: AccountUiMapper,
    private val balanceMapper: BalanceUiMapper,
    private val transactionMapper: TransactionUiMapper,
) : MviViewModel<
    DashboardUiState,
    DashboardPartialState,
    DashboardAction,
>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<DashboardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) {
            onAction(DashboardAction.LoadData)
        }
    }

    override fun handleAction(
        action: DashboardAction
    ): Flow<DashboardPartialState> = when (action) {
        is DashboardAction.LoadData -> loadData()
        is DashboardAction.RefreshData -> loadData()
        is DashboardAction.AccountSelected ->
            sendEffect(DashboardEffect.NavigateToAccount(action.accountId))
        is DashboardAction.TransactionSelected ->
            sendEffect(DashboardEffect.NavigateToTransaction(action.transactionId))
    }

    override fun reduceUiState(
        previousState: DashboardUiState,
        partialState: DashboardPartialState,
    ): DashboardUiState = when (partialState) {
        is DashboardPartialState.AccountsLoaded ->
            previousState.copy(accounts = partialState.accounts)
        is DashboardPartialState.BalanceLoaded ->
            previousState.copy(balance = partialState.balance)
        is DashboardPartialState.TransactionsLoaded ->
            previousState.copy(recentTransactions = partialState.transactions)
        is DashboardPartialState.LoadingChanged ->
            previousState.copy(isLoading = partialState.isLoading)
        is DashboardPartialState.ErrorOccurred ->
            previousState.copy(error = partialState.message, isLoading = false)
    }

    // Effects are sent from inside a cold flow, not from a
    // launched coroutine, so ordering stays sequential.
    private fun sendEffect(
        effect: DashboardEffect
    ): Flow<DashboardPartialState> = flow {
        _effect.send(effect)
    }

    private fun loadData(): Flow<DashboardPartialState> = flow {
        emit(DashboardPartialState.LoadingChanged(true))

        // Step 1 — Load accounts
        // AccountRepository interface lives in :core:domain
        // AccountRepositoryImpl lives in :feature:accounts:data
        // Koin in :app binds impl to interface at runtime.
        // No UseCase here — a single repository call with no
        // extra logic doesn't earn one (see section 4 rules).
        accountRepository.getAccounts()
            .onSuccess { accounts ->
                emit(
                    DashboardPartialState.AccountsLoaded(
                        accountMapper.mapList(accounts)
                    )
                )

                val primaryAccount = accounts.firstOrNull()
                if (primaryAccount != null) {

                    // Step 2 — Load balance for primary account
                    // BalanceRepositoryImpl lives in :feature:accounts:data
                    // No UseCase — same pass-through rule as Step 1
                    balanceRepository.getBalance(primaryAccount.accountId)
                        .onSuccess { balance ->
                            emit(
                                DashboardPartialState.BalanceLoaded(
                                    balanceMapper.map(balance)
                                )
                            )
                        }
                        .onError { error ->
                            emit(DashboardPartialState.ErrorOccurred(error.toString()))
                        }

                    // Step 3 — Load recent transactions
                    // GetRecentTransactionsUseCase lives in
                    // :feature:dashboard:domain because it is
                    // dashboard-specific — limits to 5 items
                    // and always uses the primary account only.
                    // TransactionRepositoryImpl lives in
                    // :feature:transactions:data
                    getRecentTransactionsUseCase(
                        accountId = primaryAccount.accountId
                    )
                        .onSuccess { transactions ->
                            emit(
                                DashboardPartialState.TransactionsLoaded(
                                    transactionMapper.mapList(transactions)
                                )
                            )
                        }
                        .onError { error ->
                            emit(DashboardPartialState.ErrorOccurred(error.toString()))
                        }
                }
            }
            .onError { error ->
                emit(DashboardPartialState.ErrorOccurred(error.toString()))
            }

        emit(DashboardPartialState.LoadingChanged(false))
    }
}
```

---

### Koin Setup

Each ViewModel's Koin module provides the initial state
so it can be overridden in tests, and resolves mappers
by their interfaces.

Note how DashboardModule only references a Repository
interface or UseCase from :core:domain / :feature:dashboard:domain,
and mapper interfaces from :core:presentation:ui — never
anything from :feature:accounts:* or :feature:transactions:*.
Repository implementations are bound in :app's root Koin
module, not here.

`SavedStateHandle` is injected with `get()` inside the
`viewModel { }` definition (Koin provides it).

```kotlin
// core/presentation/ui/di/CoreUiModule.kt
// Shared formatters and shared mappers. Included by :app.
val coreUiModule = module {
    single<AmountFormatter> { AmountFormatterImpl(Locale.UK) }
    single<DateFormatter> { DateFormatterImpl(Locale.UK, Clock.systemDefaultZone()) }
    single<IbanFormatter> { IbanFormatterImpl() }

    factory<AccountUiMapper> { AccountUiMapperImpl(get()) }
    factory<BalanceUiMapper> { BalanceUiMapperImpl(get()) }
    factory<TransactionUiMapper> { TransactionUiMapperImpl(get(), get()) }
}

// feature/dashboard/presentation/di/DashboardModule.kt
val dashboardModule = module {

    // Initial state provided by DI
    // Can be overridden in tests without
    // touching the ViewModel
    factory { DashboardUiState() }

    viewModel {
        DashboardViewModel(
            savedStateHandle = get(),
            initialState = get(),
            accountRepository = get(),           // :core:domain interface
            balanceRepository = get(),           // :core:domain interface
            getRecentTransactionsUseCase = get(),// :feature:dashboard:domain
            accountMapper = get(),               // :core:presentation:ui
            balanceMapper = get(),               // :core:presentation:ui
            transactionMapper = get(),           // :core:presentation:ui
        )
    }
}

// app/di/AppModule.kt
// This is the ONLY place repository implementations are
// bound to interfaces, and where use cases are registered
// (domain modules are pure Kotlin and contain no Koin).
// :app is the sole module that sees everything.
val appModule = module {

    // Repositories
    single<AccountRepository> {
        AccountRepositoryImpl(apiService = get(), tokenStorage = get())
    }
    single<BalanceRepository> {
        BalanceRepositoryImpl(apiService = get(), tokenStorage = get())
    }
    single<TransactionRepository> {
        TransactionRepositoryImpl(apiService = get(), tokenStorage = get())
    }

    // Use cases — only ones with real business logic are
    // registered. GetAccountsUseCase / GetBalanceUseCase were
    // eliminated as pass-through wrappers; the repositories
    // above are injected into ViewModels directly instead.
    factory { GetRecentTransactionsUseCase(get()) }
}

// :app startKoin { modules(appModule, coreUiModule, dashboardModule, ...) }
```

---

### Testing Approach for ViewModels

Presentation tests verify Action in → UiState out.
Mappers are replaced with tiny fakes, so ViewModel tests
never depend on formatting.

```kotlin
// Fake mappers (test source set) — deterministic, no formatting
class FakeAccountUiMapper : AccountUiMapper {
    override fun map(domain: Account) = fakeAccountUi(domain.accountId)
}

// Test factory — initialState defaulted so
// existing tests need no changes when new
// fields are added to DashboardUiState
private fun createViewModel(
    initialState: DashboardUiState = DashboardUiState(),
) = DashboardViewModel(
    savedStateHandle = SavedStateHandle(),
    initialState = initialState,
    accountRepository = mockAccountRepository,
    balanceRepository = mockBalanceRepository,
    getRecentTransactionsUseCase = mockGetRecentTransactionsUseCase,
    accountMapper = FakeAccountUiMapper(),
    balanceMapper = FakeBalanceUiMapper(),
    transactionMapper = FakeTransactionUiMapper(),
)

// Reusable test states — shared across tests
// so Given blocks stay at one line (OTOS rule)
object DashboardTestState {
    val withAccounts = DashboardUiState(
        accounts = listOf(fakeAccountUi),
        balance = fakeBalanceUi
    )
    val loading = DashboardUiState(isLoading = true)
    val error = DashboardUiState(error = "Failed to load")
}

// Single assertion test — OTOA + OTOS compliant
// One state update (Given), one action (When),
// one assertion (Then)
@Test
fun `Should show error When accounts fail to load`() = runTest {
    // Given — start exactly at the state we need
    coEvery { mockAccountRepository.getAccounts() } returns
        Result.Error(DataError.NetworkError.SERVER_ERROR)
    viewModel = createViewModel()

    // When — single action
    viewModel.onAction(DashboardAction.LoadData)
    advanceUntilIdle()

    // Then — single assertion
    assertTrue(viewModel.uiState.value.error != null)
}

@Test
fun `Should show accounts When load succeeds`() = runTest {
    // Given
    coEvery { mockAccountRepository.getAccounts() } returns
        Result.Success(listOf(fakeAccount))
    coEvery {
        mockBalanceRepository.getBalance(fakeAccount.accountId)
    } returns Result.Success(fakeBalance)
    coEvery {
        mockGetRecentTransactionsUseCase(fakeAccount.accountId)
    } returns Result.Success(emptyList())
    viewModel = createViewModel()

    // When
    viewModel.onAction(DashboardAction.LoadData)
    advanceUntilIdle()

    // Then
    assertEquals(
        listOf(fakeAccountUi(fakeAccount.accountId)),
        viewModel.uiState.value.accounts
    )
}
```

ViewModel tests need a `MainDispatcherRule`
(`Dispatchers.setMain(StandardTestDispatcher())`) because
`viewModelScope` uses `Dispatchers.Main.immediate`.

---

## 5. Module Dependency Rules

The graph is a DAG (zero cycles). Rules:

1. `domain` → only `core:domain`. Never data,
   presentation, Android, or Compose.
2. `data` → `core:domain`, `core:data`,
   `core:database`, `core:network`, own feature
   domain. Never any presentation module.
3. `presentation` → own feature domain,
   `core:domain`, `core:presentation:ui`,
   `core:presentation:designsystem`.
   Never data directly.
4. No feature module depends on another feature.
   Cross-feature via `core:domain`, `core:presentation:ui`
   (shared UI models/mappers) or app navigation.
5. `core:*` never depends on `feature:*`.
6. `:app` is the only module that sees everything.
   Nothing depends on `:app`.

---

## 6. Naming Conventions

- **Packages:** `com.edu.<core|feature>.<path>`
- **Screens:** `[Feature]Screen.kt`
- **ViewModels:** `[Feature]ViewModel.kt`
- **UiState:** `[Feature]UiState.kt`
- **PartialState:** `[Feature]PartialState.kt`
- **Actions:** `[Feature]Action.kt`
- **Effects:** `[Feature]Effect.kt`
- **UseCases:** `Get[Resource]UseCase.kt`
  e.g. `GetAccountsUseCase.kt`
- **Repositories (interface):**
  `[Resource]Repository.kt`
- **Repositories (impl):**
  `[Resource]RepositoryImpl.kt`
- **Room entities:** `[Resource]Entity.kt`
- **DTOs:** `[Resource]Dto.kt`
- **Data mappers (DTO/Entity → domain):** `[Resource]Mapper.kt`
- **UI models:** `[Resource]Ui.kt` (package `model`)
- **UI mapper interface:** `[Resource]UiMapper.kt` (package `mapper`)
- **UI mapper impl:** `[Resource]UiMapperImpl.kt` (package `mapper`)
- **Formatters:** `[Name]Formatter.kt` / `[Name]FormatterImpl.kt`
  (package `formatter`)
- **Koin modules:** `[Feature]Module.kt`

---

## 7. Build & Run

```bash
./gradlew build                 # full build
./gradlew :app:assembleDebug    # debug APK
./gradlew :app:installDebug     # install on device
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # instrumented tests
./gradlew clean                 # clean build dirs
```

Requirements:
- JDK 17
- Android SDK compileSdk/targetSdk 37
- minSdk 24
- local.properties must have sdk.dir

---

## 8. TrueLayer API Integration

### Credentials Setup
Add to `local.properties` (never commit):

```
TRUELAYER_CLIENT_ID=sandbox-personalfinanceandroidapp-bb7d59
TRUELAYER_CLIENT_SECRET=your_secret_here
TRUELAYER_REDIRECT_URI=clearspend://callback
TRUELAYER_SANDBOX=true
```

Expose via buildConfigField in relevant
module's build.gradle.kts. See the security note in
section 1: production must not ship the secret or
signing key in the APK.

### Base URLs

Sandbox:
```
Data API:     https://api.truelayer-sandbox.com
Auth URL:     https://auth.truelayer-sandbox.com
Payments API: https://api.truelayer-sandbox.com/v3
```

Production (future):
```
Data API:     https://api.truelayer.com
Auth URL:     https://auth.truelayer.com
Payments API: https://api.truelayer.com/v3
```

### OAuth2 Auth Flow (feature:auth:data)

Step 1 — Build auth URL:
```
https://auth.truelayer-sandbox.com/
  ?response_type=code
  &client_id={CLIENT_ID}
  &scope=info accounts balance cards transactions direct_debits standing_orders offline_access
  &redirect_uri=clearspend://callback
  &providers=mock
  &state={RANDOM_STATE}
```
(scopes are space-separated and URL-encoded when the URL
is built; generate a random `state`, keep it in memory or
SavedStateHandle, and reject the callback if it does not
match)

Step 2 — Open Chrome Custom Tab with URL

Step 3 — User logs in with Mock Bank:
Username: john
Password: doe

Step 4 — TrueLayer redirects to:
`clearspend://callback?code=AUTH_CODE_HERE&state=...`

Step 5 — App catches deep link in MainActivity,
validates `state`, extracts code, sends to AuthViewModel

Step 6 — Exchange code for tokens:
```
POST https://auth.truelayer-sandbox.com/connect/token
grant_type=authorization_code
client_id={CLIENT_ID}
client_secret={CLIENT_SECRET}
redirect_uri=clearspend://callback
code={AUTH_CODE}
```

Step 7 — Save tokens securely via Tink encryption

### Deep Link Setup (AndroidManifest.xml)
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data android:scheme="clearspend"
          android:host="callback"/>
</intent-filter>
```
(`autoVerify` is only for http/https App Links and does
not apply to a custom scheme, so it is omitted.)

### Token Management (core:network)

Access token: expires in 3600 seconds (1 hour)
Refresh token: expires in 90 days

Strategy:
- Store both tokens encrypted via Tink
- Store token expiry timestamp
- Before every API call check if token expired
- If expired → call refresh endpoint automatically
- If refresh fails → return `Result.Error(DataError.NetworkError.UNAUTHORIZED)`
  and force user to re-authenticate

Refresh endpoint:
```
POST https://auth.truelayer-sandbox.com/connect/token
grant_type=refresh_token
client_id={CLIENT_ID}
client_secret={CLIENT_SECRET}
refresh_token={SAVED_REFRESH_TOKEN}
```

### Data API Endpoints (feature:accounts:data
and feature:transactions:data)

```
GET /data/v1/accounts
Returns: account_id, account_type,
display_name, currency, account_number
(iban, sort_code, number), provider

GET /data/v1/accounts/{account_id}/balance
Returns: available, current,
overdraft, currency

GET /data/v1/accounts/{account_id}/transactions
?from={date}&to={date}
Returns: transaction_id, timestamp,
description, amount, currency,
transaction_type (DEBIT/CREDIT),
transaction_category, running_balance

GET /data/v1/accounts/{account_id}/direct_debits
Returns: recurring outgoing payments

GET /data/v1/accounts/{account_id}/standing_orders
Returns: user-scheduled recurring transfers

GET /data/v1/cards
Returns: cards for the Card screen (scope: cards)

GET /data/v1/info
Returns: account-holder identity info (scope: info)
```

### Payments API Endpoints (feature:payments:data)

Authentication — DIFFERENT from Data API:
```
POST https://auth.truelayer-sandbox.com/connect/token
grant_type=client_credentials
client_id={CLIENT_ID}
client_secret={CLIENT_SECRET}
scope=payments
```

Every payments request also needs:
- `Tl-Signature` header (request signed with your signing key)
- `Idempotency-Key` header (unique per create request)

```
POST /v3/payments
Creates a payment
Returns: id, resource_token, user.id, status
(NOT auth_uri — v3 does not return one)

GET /v3/payments/{id}
Check payment status

GET /v3/payments-providers
Lists available banks for payment
```

Authorising the payment: use the TrueLayer Android SDK
(`com.truelayer.payments:ui`) with the `id` and
`resource_token`, or the Hosted Payment Page.

v3 statuses:
`authorization_required → authorizing → authorized → executed → settled`
or `failed` (with `failure_stage` + `failure_reason`).
There is no `rejected` status. Map `failed` reasons to the
four failure screens in the design.

Signing key and client secret must live on a backend in
production (see section 1).

### Transaction Categories

```
PURCHASE → Shopping, food, entertainment
DIRECT_DEBIT → Bills, subscriptions
BILL_PAYMENT → Rent, utilities
TRANSFER → Person to person
ATM → Cash withdrawal
CASH → Cash deposit/withdrawal
CREDIT → Income, refunds, tax credits
```

---

## 9. UI/UX Design Reference

### Design Files Location

```
designs/
├── DESIGN_SPEC.md          → per-screen API mapping, derived values, states (read before building any screen)
├── README.md
├── ClearSpend.dc.html      → clickable prototype of every screen
├── Screen Gallery.dc.html  → every screen/state side by side
├── BottomNav.dc.html, android-frame.jsx, support.js → needed by the prototype
└── screens/                → 38 screenshots (see table below)
```

Preview: `cd designs && npx serve .` → http://localhost:3000
Precedence: this CLAUDE.md wins on architecture and code; `designs/DESIGN_SPEC.md` wins on what a screen shows and where each value comes from; the screenshot wins on visuals.

⚠️ Before building any screen, confirm that
`designs/DESIGN_SPEC.md` and the matching screenshot in
`designs/screens/` exist. If either is missing, stop and
ask instead of guessing the design.

### Design System (`core:presentation:designsystem`)

Colors:
- Navy (dark surfaces, hero cards): `#0A1628` · Navy 2 (icon buttons on navy): `#12213D`
- Electric Blue (primary action): `#2D6EF5` · Blue dark (gradient end): `#1B4FC7`
- Mint (accent on dark): `#00D4AA`
- Error/Debit: `#FF4B4B` (text on white `#E03B3B`)
- Credit/Income: `#00C853` (text on white `#00A043`)
- Warning/Pending: `#FFB020` (text on white `#B36B00`)
- Surface: `#FFFFFF` · Surface variant (cards on white): `#F5F7FA` · Outline: `#E7EAEE`
- Text primary: `#0A1628` on white, `#F5F7FA` on navy
- Text secondary: `#6B7684` on white, `rgba(245,247,250,0.6)` on navy

Typography (Roboto + Roboto Mono for sort codes, account numbers, IDs):
- Display: 34–36sp bold → balance amounts
- Headline: 22sp bold → screen titles
- Title: 15–17sp bold → section/card titles
- Body: 13–14sp → transaction rows
- Caption: 11–12sp → timestamps, labels

Spacing & shape:
- Base grid 8dp · screen padding 20dp · card padding 16–20dp
- Card radius 16–22dp · button radius 22–24dp (pill) · bottom sheet 26dp top corners
- Bottom nav: floating pill ~64dp + 12dp margin (≈80dp footprint), raised 52dp Send FAB

### Screen to Module Map

| Screen | Screenshots (`designs/screens/`) | Module |
|--------|-----------|--------|
| Splash | 01-splash | app |
| Onboarding 1–3 | 02-onboarding-1 … 04-onboarding-3 | feature:auth:presentation |
| Connect Bank | 05-auth-connect-bank | feature:auth:presentation |
| Bank Selection | 06-bank-selection | feature:auth:presentation |
| Connecting | 07-connecting-loading | feature:auth:presentation |
| Biometric Login | 08-biometric-login | feature:auth:presentation |
| Dashboard | 09-home | feature:dashboard:presentation |
| Accounts | 10-accounts | feature:accounts:presentation |
| Account Detail | 11-account-detail | feature:accounts:presentation |
| Transactions | 12-transactions, 13-transactions-empty-state | feature:transactions:presentation |
| Transaction Detail | 14-…-settled, 15-…-pending, 16-…-direct-debit | feature:transactions:presentation |
| Analytics | 17-analytics, 18-analytics-category-focused | feature:analytics:presentation |
| Bills | 19-bills-direct-debits, 20-bills-standing-orders | feature:analytics:presentation |
| Card | 21-card | feature:accounts:presentation |
| Notifications | 22-notifications | app |
| Settings | 23-profile-settings | app |
| Send Money (6 steps) | 24 → 38 (payee, amount, confirm, authorising, success, 4 failure types) | feature:payments:presentation |

### Bottom Navigation (5 tabs)
Home → route: home → feature:dashboard:presentation
Accounts → route: accounts → feature:accounts:presentation
Send (FAB) → route: send → feature:payments:presentation ← centre, larger, opens as a bottom sheet
Transactions → route: transactions → feature:transactions:presentation
Analytics → route: analytics → feature:analytics:presentation

### Shared Composables (core:presentation:ui)

ClearSpendTheme.kt → colors, typography (lives in designsystem, re-exported for features)
BalanceCard.kt → balance hero card
AccountCard.kt → account list card
TransactionRow.kt → transaction list row
ClearSpendBottomNav.kt → 5-tab floating bottom nav with FAB
LoadingScreen.kt → shared skeleton loading state
ErrorScreen.kt → shared error/empty state
AmountText.kt → DEBIT red / CREDIT green
CategoryIcon.kt → transaction category icon

Shared composables take UI models (AccountUi,
TransactionUi, BalanceUi) as parameters, never domain
models and never raw values that need formatting.

---

## 10. Navigation Structure

Routes live in `:app` (the navigation host). Feature
screens never reference `Route`; they expose callbacks
and `:app` maps them to routes. Navigation3 keys must be
`@Serializable` and implement `NavKey`. Routes with
arguments are data classes, not objects.

### Routes
```kotlin
sealed interface Route : NavKey {
    // Start-up
    @Serializable data object Splash : Route
    @Serializable data object Onboarding : Route

    // Auth
    @Serializable data object ConnectBank : Route
    @Serializable data object BankSelection : Route
    @Serializable data object Connecting : Route
    @Serializable data object BiometricLogin : Route

    // Main (with bottom nav)
    @Serializable data object Dashboard : Route
    @Serializable data object Accounts : Route
    @Serializable data class AccountDetail(val accountId: String) : Route
    @Serializable data object Card : Route
    @Serializable data object SendMoney : Route      // bottom sheet
    @Serializable data object Transactions : Route
    @Serializable data class TransactionDetail(val transactionId: String) : Route
    @Serializable data object Analytics : Route
    @Serializable data object Bills : Route
    @Serializable data object Notifications : Route
    @Serializable data object Settings : Route
}
```

The OAuth callback (`clearspend://callback`) is handled in
`MainActivity` as a deep link and forwarded to the auth
ViewModel; it is not a Route.

### Navigation Flow

```
App Start
   ↓
Check token in DataStore (encrypted via Tink)
   ↓
No token?  → Onboarding / ConnectBank screen
Token exists? → Check expiry
   ↓
Not expired? → BiometricLogin (if enabled) → Dashboard (main nav)
Expired?     → Refresh silently → Dashboard
Refresh failed? → ConnectBank screen
```

---

## 11. Secrets

`local.properties` currently only has `sdk.dir`.
It is correctly git-ignored.

Add TrueLayer credentials here when implementing:

```
sdk.dir=/path/to/android/sdk
TRUELAYER_CLIENT_ID=sandbox-personalfinanceandroidapp-bb7d59
TRUELAYER_CLIENT_SECRET=your_secret
TRUELAYER_REDIRECT_URI=clearspend://callback
```

Never hardcode credentials in .kt files.
Never commit local.properties.
Never print credentials, tokens or auth codes in logs.
Use buildConfigField to expose to code (sandbox only —
see the security note in section 1).

---

## 12. Code Style

- `kotlin.code.style=official` in gradle.properties
- JVM target 17 everywhere via convention plugins
- Keep domain modules Android-free
- Favor `implementation` over `api` for module deps
- Exception: `core:presentation:designsystem`
  exposes Material3 via `api`
- Prefer constructor injection and interfaces over
  static/extension helpers for anything with logic
  (mappers, formatters, clocks)

---

## 13. Adding a New Feature Module

Example: adding `budgets` feature.

1. Add to settings.gradle.kts:
```kotlin
include(":feature:budgets:domain")
include(":feature:budgets:data")
include(":feature:budgets:presentation")
```

2. feature/budgets/domain/build.gradle.kts:
```kotlin
plugins { alias(libs.plugins.clearspend.jvm.library) }
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.core.domain)
}
```

3. feature/budgets/data/build.gradle.kts:
```kotlin
plugins {
    alias(libs.plugins.clearspend.android.library)
    alias(libs.plugins.clearspend.jvm.ktor)
}
android { namespace = "com.edu.feature.budgets.data" }
dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.feature.budgets.domain)
    implementation(libs.bundles.koin)
}
```

4. feature/budgets/presentation/build.gradle.kts:
```kotlin
plugins { alias(libs.plugins.clearspend.android.ui.feature) }
android { namespace = "com.edu.feature.budgets.presentation" }
dependencies {
    implementation(projects.feature.budgets.domain)
    implementation(projects.core.domain)
}
```
(kotlin-parcelize comes from the convention plugin.)

5. Wire into app/build.gradle.kts
6. Register Koin module, repository bindings, use cases and
   nav route in :app

---

## 14. Testing Approach

- **Unit tests** → JUnit4 + MockK + Turbine +
  coroutines-test
- **Instrumented tests** → JUnit4 + Espresso +
  Compose UI test
- Test domain (use cases) and data (repositories)
  most heavily
- **Mappers:** one test class per `[Resource]UiMapperImpl`
  (domain in → UI model out), covering edge cases:
  negative amounts, missing IBAN, pending transactions,
  empty lists. Formatters are injected with a fixed
  `Locale` and `Clock` so results are deterministic.
- **Data mappers** (`[Resource]Mapper`): one test class
  each (DTO/Entity → domain).
- Presentation (ViewModel) tests focus on:
  Action in → UiState out transitions, using fake
  mappers (see section 4 testing example)
- Every use case, repository, mapper and ViewModel ships
  with tests in the same phase it is built
- All modules currently have only placeholder tests
  Add real tests as features are implemented

---

## 15. Implementation Priority Order

Build in this order — each step depends on previous.
After each phase: `./gradlew build` and `./gradlew test`
must pass before moving on.

Phase 1 — Foundation
├── core:domain (Result type, models, repository interfaces,
│   shared use cases)
├── core:network (Ktor client, token interceptor)
├── core:database (Room setup, token storage with Tink)
├── core:data (shared repository plumbing)
├── core:presentation:designsystem (theme, colors)
└── core:presentation:ui (UiStateMachine, MviViewModel,
    UiMapper, formatters, shared UI models + mappers,
    shared composables, coreUiModule)

Phase 2 — Auth
├── feature:auth:domain (models, interfaces)
├── feature:auth:data (TrueLayer OAuth)
├── feature:auth:presentation (Onboarding, Connect Bank,
│   Bank Selection, Connecting, Biometric screens)
└── :app (Application class + startKoin, Navigation3 host,
    deep link, start-up flow)

Phase 3 — Dashboard
├── feature:accounts:domain + data + presentation
├── feature:dashboard:domain + data
├── feature:dashboard:presentation
└── bottom navigation

Phase 4 — Transactions
├── feature:transactions:domain + data
└── feature:transactions:presentation

Phase 5 — Payments
├── feature:payments:domain + data
└── feature:payments:presentation

Phase 6 — Analytics
├── feature:analytics:domain + data
└── feature:analytics:presentation

Phase 7 — Polish
├── Biometric login hardening
├── Offline-first (Room caching)
├── Error handling throughout
├── Settings and Notifications screens
└── GitHub Actions CI/CD