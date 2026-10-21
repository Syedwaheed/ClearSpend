package com.edu.feature.auth.presentation.mapper

import com.edu.core.presentation.ui.mapper.UiMapper
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.presentation.model.WelcomeUi

interface WelcomeUiMapper : UiMapper<LoginProfile, WelcomeUi>
