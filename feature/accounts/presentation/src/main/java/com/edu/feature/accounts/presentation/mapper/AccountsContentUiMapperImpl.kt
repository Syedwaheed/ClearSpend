package com.edu.feature.accounts.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.mapper.CardSummaryUiMapper
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.feature.accounts.domain.model.AccountsOverview
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.model.ConsentUi

class AccountsContentUiMapperImpl(
    private val netPositionMapper: NetPositionUiMapper,
    private val groupMapper: ProviderGroupUiMapper,
    private val cardMapper: CardSummaryUiMapper,
    private val syncTimeMapper: SyncTimeUiMapper,
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
) : AccountsContentUiMapper {

    override fun map(domain: AccountsOverview): AccountsContentUi = AccountsContentUi(
        netPosition = netPositionMapper.map(domain),
        consent = domain.consentDaysRemaining?.let { days ->
            ConsentUi(daysRemaining = days, syncedAgo = domain.lastSyncedAt?.let(dateFormatter::formatTimeAgo))
        },
        groups = groupMapper.mapList(domain.groups),
        cards = cardMapper.mapList(domain.cards),
        formattedCardsOwed = amountFormatter.format(domain.netPosition.cardOwed),
        offlineSyncTime = domain.offlineSince?.let(syncTimeMapper::map),
    )
}
