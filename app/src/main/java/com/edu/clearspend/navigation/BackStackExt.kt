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

/** The route whose tab is highlighted (ignores overlays such as the Send sheet). */
internal fun List<NavKey>.currentTabIndex(): Int {
    val top = lastOrNull { it != Route.SendMoney }
    val index = bottomNavTabs.indexOf(top)
    return if (index >= 0) index else 0
}

internal fun List<NavKey>.showsBottomNav(): Boolean =
    lastOrNull { it != Route.SendMoney } in bottomNavRoutes
