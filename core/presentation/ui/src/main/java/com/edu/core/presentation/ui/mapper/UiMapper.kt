package com.edu.core.presentation.ui.mapper

interface UiMapper<in DOMAIN, out UI> {
    fun map(domain: DOMAIN): UI
}

fun <D, U> UiMapper<D, U>.mapList(list: List<D>): List<U> = list.map(::map)
