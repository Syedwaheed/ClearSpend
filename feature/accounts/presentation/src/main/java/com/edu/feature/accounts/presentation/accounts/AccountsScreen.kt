package com.edu.feature.accounts.presentation.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.RobotoMono
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.AccountTypeSavings
import com.edu.core.presentation.ui.EmptyStateScreen
import com.edu.core.presentation.ui.ErrorScreen
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.OfflineBanner
import com.edu.core.presentation.ui.ProviderLogo
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.core.presentation.ui.dashedBorder
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.core.presentation.ui.shimmer
import com.edu.feature.accounts.presentation.R
import com.edu.feature.accounts.presentation.components.MiniCardFace
import com.edu.feature.accounts.presentation.components.StatCell
import com.edu.feature.accounts.presentation.components.UtilisationBar
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.AccountRowUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.model.ConsentUi
import com.edu.feature.accounts.presentation.model.NetPositionUi
import com.edu.feature.accounts.presentation.model.ProviderGroupUi
import com.edu.feature.accounts.presentation.model.ThirdStatUi
import org.koin.compose.viewmodel.koinViewModel

private val BottomNavClearance = 112.dp
private val ChevronGrey = Color(0xFF9AA3AF)

@Composable
fun AccountsScreen(
    onNavigateToAccount: (accountId: String) -> Unit,
    onNavigateToCard: () -> Unit,
    onNavigateToBankSelection: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            is AccountsEffect.NavigateToAccount -> onNavigateToAccount(effect.accountId)
            AccountsEffect.NavigateToCard -> onNavigateToCard()
            AccountsEffect.NavigateToBankSelection -> onNavigateToBankSelection()
            AccountsEffect.NavigateToConnectBank -> onNavigateToConnectBank()
        }
    }
    AccountsContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun AccountsContent(
    state: AccountsUiState,
    onAction: (AccountsAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding(),
    ) {
        TitleRow(isSyncing = state.isRefreshing, onRefresh = { onAction(AccountsAction.RefreshClicked) })
        val content = state.content
        when {
            state.isLoading && content == null -> AccountsSkeleton()
            state.hasError -> ErrorScreen(onRetry = { onAction(AccountsAction.RetryClicked) })
            state.isEmpty -> EmptyStateScreen(
                title = stringResource(R.string.accounts_empty_title),
                subtitle = stringResource(R.string.accounts_empty_body),
                icon = Icons.Outlined.AccountBalance,
                ctaText = stringResource(R.string.accounts_empty_cta),
                onCtaClick = { onAction(AccountsAction.ConnectAnotherBankClicked) },
            )
            content != null -> AccountsBody(state, content, onAction)
            else -> AccountsSkeleton()
        }
    }
}

@Composable
private fun TitleRow(isSyncing: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.accounts_title), color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(SurfaceVariant)
                .clickable(enabled = !isSyncing, role = Role.Button, onClick = onRefresh)
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, tint = Blue, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(if (isSyncing) R.string.accounts_syncing else R.string.accounts_refresh),
                color = Blue,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AccountsBody(state: AccountsUiState, content: AccountsContentUi, onAction: (AccountsAction) -> Unit) {
    val groups = state.visibleGroups
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(AccountsTestTags.CONTENT),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = BottomNavClearance),
    ) {
        content.offlineSyncTime?.let { item(key = "offline") { OfflineBanner(it, Modifier.padding(bottom = 12.dp)) } }
        item(key = "net") { NetPositionHero(content.netPosition) }
        content.consent?.let { consent ->
            item(key = "consent") { ConsentBanner(consent, onRenew = { onAction(AccountsAction.RenewConsentClicked) }) }
        }
        item(key = "chips") { FilterChips(state.selectedFilter, onSelect = { onAction(AccountsAction.FilterSelected(it)) }) }
        if (groups.isEmpty() && !state.showsCards) {
            item(key = "filter-empty") {
                Text(stringResource(R.string.accounts_filter_empty), color = OnSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 24.dp))
            }
        }
        groups.forEach { group ->
            item(key = "group-${group.id}") { GroupHeader(group) }
            items(group.accounts, key = { it.account.accountId }) { row ->
                AccountRow(row, onClick = { onAction(AccountsAction.AccountSelected(row.account.accountId)) })
            }
            item(key = "group-gap-${group.id}") { Spacer(Modifier.height(8.dp)) }
        }
        if (state.showsCards) {
            item(key = "cards-header") { CardsHeader(content.formattedCardsOwed) }
            items(content.cards, key = { "card-${it.cardId}" }) { card ->
                CreditCardRow(card, onClick = { onAction(AccountsAction.CardSelected) })
            }
        }
        item(key = "connect") { ConnectAnotherBank(onClick = { onAction(AccountsAction.ConnectAnotherBankClicked) }) }
    }
}

// --- Net position hero -------------------------------------------------------------

@Composable
private fun NetPositionHero(net: NetPositionUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy, RoundedCornerShape(22.dp))
            .padding(20.dp),
    ) {
        Text(pluralStringResource(R.plurals.accounts_net_position, net.accountCount, net.accountCount), color = OnNavy.copy(alpha = 0.6f), fontSize = 11.5.sp)
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(net.formattedTotal, color = OnNavy, fontSize = 31.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.accounts_this_month, net.formattedChangeThisMonth),
                color = if (net.isChangePositive) Mint else Error,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            net.sparkline.forEachIndexed { index, height ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(height)
                        .background(
                            if (index == net.sparkline.lastIndex) Mint else OnNavy.copy(alpha = 0.22f),
                            RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp),
                        ),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = OnNavy.copy(alpha = 0.14f))
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth()) {
            HeroStat(stringResource(R.string.accounts_current), net.formattedCurrent, OnNavy, Modifier.weight(1f))
            VerticalRule()
            HeroStat(stringResource(R.string.accounts_savings), net.formattedSavings, Mint, Modifier.weight(1f).padding(start = 14.dp))
            VerticalRule()
            HeroStat(stringResource(R.string.accounts_card_owed), net.formattedCardOwed, Color(0xFFFF8080), Modifier.weight(1f).padding(start = 14.dp))
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun HeroStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = OnNavy.copy(alpha = 0.6f), fontSize = 10.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, color = color, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun VerticalRule() {
    Box(Modifier.width(1.dp).height(32.dp).background(OnNavy.copy(alpha = 0.14f)))
}

// --- Consent -------------------------------------------------------------------------

@Composable
private fun ConsentBanner(consent: ConsentUi, onRenew: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
            .border(1.dp, Blue.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).background(Blue.copy(alpha = 0.14f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Blue, modifier = Modifier.size(17.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (consent.daysRemaining > 0) {
                    pluralStringResource(R.plurals.accounts_consent_renews, consent.daysRemaining, consent.daysRemaining)
                } else {
                    stringResource(R.string.accounts_consent_expired)
                },
                color = OnSurface,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                consent.syncedAgo?.let { stringResource(R.string.accounts_synced_ago, it) } ?: stringResource(R.string.accounts_synced_never),
                color = OnSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        Text(
            stringResource(R.string.accounts_renew),
            color = Blue,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onRenew)
                .padding(8.dp),
        )
    }
    Spacer(Modifier.height(18.dp))
}

// --- Filter chips ------------------------------------------------------------------------

@Composable
private fun FilterChips(selected: AccountFilterUi, onSelect: (AccountFilterUi) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
        items(AccountFilterUi.entries) { filter ->
            val isSelected = filter == selected
            Text(
                text = stringResource(
                    when (filter) {
                        AccountFilterUi.ALL -> R.string.accounts_filter_all
                        AccountFilterUi.CURRENT -> R.string.accounts_filter_current
                        AccountFilterUi.SAVINGS -> R.string.accounts_filter_savings
                        AccountFilterUi.BUSINESS -> R.string.accounts_filter_business
                    },
                ),
                color = if (isSelected) Blue else OnSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) Blue.copy(alpha = 0.1f) else Surface)
                    .border(1.dp, if (isSelected) Blue else Outline, RoundedCornerShape(100.dp))
                    .clickable(role = Role.Tab) { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

// --- Provider groups -------------------------------------------------------------------

@Composable
private fun GroupHeader(group: ProviderGroupUi) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        ProviderLogo(group.logoUri, group.initial, group.fallbackColorArgb, contentDescription = group.name)
        Spacer(Modifier.width(9.dp))
        Text(group.name, color = OnSurface, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(9.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Outline))
        Spacer(Modifier.width(9.dp))
        Text(group.formattedSubtotal, color = OnSurfaceVariant, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AccountRow(row: AccountRowUi, onClick: () -> Unit) {
    val typeColor = Color(row.account.accountTypeColorArgb)
    Column(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariant)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(iconFor(row.account.accountTypeIconId), contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        row.account.displayName,
                        color = OnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        row.account.accountTypeBadge,
                        color = typeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                val identifiers = listOfNotNull(row.account.formattedSortCode, row.account.maskedAccountNumber).joinToString("  ·  ")
                if (identifiers.isNotEmpty()) {
                    Text(identifiers, color = OnSurfaceVariant, fontSize = 11.sp, fontFamily = RobotoMono, maxLines = 1)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(row.formattedAvailable ?: stringResource(R.string.accounts_not_available), color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.accounts_avail), color = OnSurfaceVariant, fontSize = 10.5.sp)
            }
        }
        Spacer(Modifier.height(13.dp))
        HorizontalDivider(color = Outline)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatCell(
                stringResource(R.string.accounts_current),
                row.formattedCurrent ?: stringResource(R.string.accounts_not_available),
                OnSurfaceVariant,
                OnSurface,
                Modifier.weight(1f),
            )
            StatCell(
                stringResource(if (row.thirdStat == ThirdStatUi.INTEREST_AER) R.string.accounts_interest_aer else R.string.accounts_overdraft),
                row.thirdStatValue,
                OnSurfaceVariant,
                if (row.thirdStat == ThirdStatUi.INTEREST_AER) AccountTypeSavings else OnSurface,
                Modifier.weight(1f),
            )
            StatCell(
                stringResource(R.string.accounts_thirty_day),
                row.formattedThirtyDayChange ?: stringResource(R.string.accounts_not_available),
                OnSurfaceVariant,
                when {
                    row.formattedThirtyDayChange == null -> OnSurfaceVariant
                    row.isThirtyDayChangePositive -> AccountTypeSavings
                    else -> Error
                },
                Modifier.weight(1f),
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ChevronGrey, modifier = Modifier.size(18.dp))
        }
    }
}

private fun iconFor(iconId: String): ImageVector = when (iconId) {
    "star" -> Icons.Outlined.StarOutline
    "briefcase" -> Icons.Outlined.WorkOutline
    else -> Icons.Outlined.Home
}

// --- Credit cards ------------------------------------------------------------------------

@Composable
private fun CardsHeader(formattedOwed: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        MiniCardFace(22, 22)
        Spacer(Modifier.width(9.dp))
        Text(stringResource(R.string.accounts_credit_cards), color = OnSurface, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(9.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Outline))
        Spacer(Modifier.width(9.dp))
        Text(formattedOwed, color = OnSurfaceVariant, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CreditCardRow(card: CardSummaryUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(bottom = 18.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariant)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniCardFace(42, 30)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(card.displayName, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("${card.maskedNumber}  ·  ${card.network}", color = OnSurfaceVariant, fontSize = 11.sp, fontFamily = RobotoMono)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(card.formattedAvailable ?: stringResource(R.string.accounts_not_available), color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.accounts_avail), color = OnSurfaceVariant, fontSize = 10.5.sp)
            }
        }
        val utilisationLabel = card.utilisationLabel
        if (utilisationLabel != null) {
            Spacer(Modifier.height(13.dp))
            UtilisationBar(card.utilisationFraction, Color(card.utilisationColorArgb))
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    stringResource(R.string.accounts_card_used, utilisationLabel, card.formattedCreditLimit.orEmpty()),
                    color = OnSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                )
                if (card.hasPaymentDue) {
                    Text(
                        stringResource(R.string.accounts_card_due, card.formattedPaymentDue!!, card.formattedPaymentDueDate!!),
                        color = Error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectAnotherBank(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder()
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(17.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(26.dp).background(SurfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Text("+", color = Blue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(9.dp))
        Text(stringResource(R.string.accounts_connect_another_bank), color = Blue, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
    }
}

// --- Loading skeleton ---------------------------------------------------------------------

@Composable
private fun AccountsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag(AccountsTestTags.SKELETON),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(210.dp).shimmer(RoundedCornerShape(22.dp), baseColor = Navy, highlightColor = OnNavy.copy(alpha = 0.12f)))
        SkeletonBlock(Modifier.fillMaxWidth().height(62.dp), RoundedCornerShape(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { SkeletonBlock(Modifier.size(width = 76.dp, height = 34.dp), RoundedCornerShape(100.dp)) }
        }
        repeat(3) { SkeletonBlock(Modifier.fillMaxWidth().height(128.dp), RoundedCornerShape(18.dp)) }
    }
}

object AccountsTestTags {
    const val SKELETON = "accounts_skeleton"
    const val CONTENT = "accounts_content"
}

// --- Previews -----------------------------------------------------------------------------

@Preview(widthDp = 412, heightDp = 1500)
@Composable
private fun AccountsPreview() {
    ClearSpendTheme(darkTheme = false) { AccountsContent(AccountsUiState(content = AccountsPreviewData.content), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun AccountsSyncingPreview() {
    ClearSpendTheme(darkTheme = false) {
        AccountsContent(AccountsUiState(content = AccountsPreviewData.content, isRefreshing = true, selectedFilter = AccountFilterUi.SAVINGS), onAction = {})
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun AccountsLoadingPreview() {
    ClearSpendTheme(darkTheme = false) { AccountsContent(AccountsUiState(isLoading = true), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun AccountsErrorPreview() {
    ClearSpendTheme(darkTheme = false) { AccountsContent(AccountsUiState(hasError = true), onAction = {}) }
}
