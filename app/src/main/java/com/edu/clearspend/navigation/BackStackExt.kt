package com.edu.clearspend.navigation

import androidx.navigation3.runtime.NavKey

/** Replaces the whole stack without ever leaving it empty (NavDisplay needs ≥ 1 entry). */
internal fun MutableList<NavKey>.replaceAll(vararg routes: NavKey) {
    require(routes.isNotEmpty())
    val previous = size
    addAll(routes)
    repeat(previous) { removeAt(0) }
}

/** Pops back to [route] if present, otherwise makes it the only entry. */
internal fun MutableList<NavKey>.popTo(route: NavKey) {
    val index = lastIndexOf(route)
    if (index >= 0) {
        while (size > index + 1) removeAt(lastIndex)
    } else {
        replaceAll(route)
    }
}

/** Switches bottom-nav tab: Home is always the root, other tabs sit on top of it. */
internal fun MutableList<NavKey>.selectTab(tab: Route) {
    if (tab == Route.Dashboard) replaceAll(Route.Dashboard) else replaceAll(Route.Dashboard, tab)
}

/** The bottom-nav tab whose section of the stack is showing (Home when nothing else is). */
internal fun List<NavKey>.currentTab(): Route {
    val second = getOrNull(1)
    return if (first() == Route.Dashboard && second is Route && second in bottomNavRoutes) second else Route.Dashboard
}

/** The entries that belong to [currentTab], without the Send sheet overlay. */
private fun List<NavKey>.currentTabSection(): List<NavKey> {
    val section = if (currentTab() == Route.Dashboard) this else drop(1)
    return section.filter { it != Route.SendMoney }
}

/**
 * Bottom-nav switch that keeps each tab's own back stack (CLAUDE.md §10 /
 * phase brief): the leaving tab's section is saved into [histories], the
 * target tab's saved section is restored on top of Home. Re-selecting the
 * showing tab pops it back to its root.
 */
internal fun MutableList<NavKey>.switchTab(target: Route, histories: Map<Route, MutableList<NavKey>>) {
    val current = currentTab()
    if (target == current) {
        histories[target]?.replaceAll(target)
        selectTab(target)
        return
    }
    histories[current]?.let { history ->
        val section = currentTabSection()
        if (section.isNotEmpty()) history.replaceAll(*section.toTypedArray())
    }
    val restored = histories[target]?.toList()?.takeIf { it.firstOrNull() == target } ?: listOf(target)
    if (target == Route.Dashboard) replaceAll(*restored.toTypedArray()) else replaceAll(Route.Dashboard, *restored.toTypedArray())
}

/** The route whose tab is highlighted (ignores overlays such as the Send sheet). */
internal fun List<NavKey>.currentTabIndex(): Int {
    val top = lastOrNull { it != Route.SendMoney }
    val index = bottomNavTabs.indexOf(top)
    return if (index >= 0) index else 0
}

internal fun List<NavKey>.showsBottomNav(): Boolean =
    lastOrNull { it != Route.SendMoney } in bottomNavRoutes
