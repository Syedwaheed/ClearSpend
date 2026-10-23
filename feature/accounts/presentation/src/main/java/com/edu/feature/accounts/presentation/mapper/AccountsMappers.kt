package com.edu.feature.accounts.presentation.mapper

import com.edu.core.presentation.ui.mapper.UiMapper
import com.edu.feature.accounts.domain.model.AccountDetail
import com.edu.feature.accounts.domain.model.AccountOverviewItem
import com.edu.feature.accounts.domain.model.AccountsOverview
import com.edu.feature.accounts.domain.model.ProviderGroup
import com.edu.feature.accounts.presentation.model.AccountDetailUi
import com.edu.feature.accounts.presentation.model.AccountRowUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.model.NetPositionUi
import com.edu.feature.accounts.presentation.model.ProviderGroupUi

interface AccountRowUiMapper : UiMapper<AccountOverviewItem, AccountRowUi>
interface ProviderGroupUiMapper : UiMapper<ProviderGroup, ProviderGroupUi>
interface NetPositionUiMapper : UiMapper<AccountsOverview, NetPositionUi>
interface AccountsContentUiMapper : UiMapper<AccountsOverview, AccountsContentUi>
interface AccountDetailUiMapper : UiMapper<AccountDetail, AccountDetailUi>
