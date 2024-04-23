/*
 * Copyright 2024 David Takač
 *
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.forecast

import androidx.annotation.CallSuper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Objects

open class HourPeriod<T : HourMoment>(private val moments: List<T>) : List<T> by moments {
    init {
        requireNotEmpty()
        requireAscendingAndComplete()
    }

    operator fun get(time: LocalDateTime): T? = momentsFrom(time, takeMoments = 1)?.firstOrNull()

    @CallSuper
    open fun momentsUntil(hourExclusive: LocalDateTime, takeMoments: Int? = null): HourPeriod<T>? {
        require(takeMoments == null || takeMoments > 0) { "Take moments must either be null or positive." }
        val hour = hourExclusive.truncatedTo(ChronoUnit.HOURS).minus(1, ChronoUnit.HOURS)
        val indexOfHour = moments.indexOfFirst { it.hour == hour }
        return if (indexOfHour < 0) null else HourPeriod(
            moments
                .slice(0..indexOfHour)
                .let { if (takeMoments != null) it.takeLast(takeMoments) else it }
        )
    }

    @CallSuper
    open fun momentsFrom(hourInclusive: LocalDateTime, takeMoments: Int? = null): HourPeriod<T>? {
        require(takeMoments == null || takeMoments > 0) { "Take moments must either be null or positive." }
        val hour = hourInclusive.truncatedTo(ChronoUnit.HOURS)
        val indexOfHour = moments.indexOfFirst { it.hour == hour }
        return if (indexOfHour < 0) null else HourPeriod(
            moments
                .subList(indexOfHour, moments.size)
                .let { if (takeMoments != null) it.take(takeMoments) else it }
        )
    }

    @CallSuper
    open fun getDay(day: LocalDate): HourPeriod<T>? =
        daysFrom(day, takeDays = 1)?.firstOrNull()

    @CallSuper
    open fun daysFrom(
        dayInclusive: LocalDate,
        takeDays: Int? = null
    ): List<HourPeriod<T>>? {
        require(takeDays == null || takeDays > 0) { "Take days must either be null or positive." }
        val momentsGroupedIntoDays = moments
            .groupBy { it.hour.toLocalDate() }
            .map { it.key to it.value }
        val indexOfDay = momentsGroupedIntoDays.indexOfFirst { it.first == dayInclusive }
        return if (indexOfDay < 0) null else
            momentsGroupedIntoDays
                .subList(indexOfDay, momentsGroupedIntoDays.size)
                .map { it.second }
                .let { if (takeDays != null) it.take(takeDays) else it }
                .map { HourPeriod(it) }
    }

    @CallSuper
    open fun contDaysFrom(
        dayInclusive: LocalDate,
        takeDays: Int? = null
    ): List<HourPeriod<T>>? {
        val regularDays = daysFrom(dayInclusive, takeDays) ?: return null
        val contDays = mutableListOf<HourPeriod<T>>()
        for (i in regularDays.indices) {
            val currDay = regularDays[i]
            val nextDay = regularDays.getOrNull(i + 1)
            contDays.add(
                if (nextDay != null) HourPeriod(moments = currDay + nextDay.first())
                else currDay
            )
        }
        return contDays
    }

    fun matches(other: HourPeriod<*>): Boolean =
        moments.map { it.hour } == other.moments.map { it.hour }

    private fun requireAscendingAndComplete() {
        if (moments.size == 1) return
        var previousMoment = moments[0]
        for (i in 1..moments.lastIndex) {
            val nextMoment = moments[i]
            require(ChronoUnit.HOURS.between(previousMoment.hour, nextMoment.hour) == 1L) {
                "Moments of HourPeriod be sorted and spaced by one hour, but contained ${previousMoment.hour} before ${nextMoment.hour}."
            }
            previousMoment = nextMoment
        }
    }

    private fun requireNotEmpty() =
        require(moments.isNotEmpty()) { "Moments of HourPeriod must not be empty." }

    override fun equals(other: Any?): Boolean =
        other is HourPeriod<*> && other.moments == moments

    override fun hashCode(): Int = Objects.hash(moments)

    override fun toString(): String = moments.toString()
}

inline fun requireMatching(vararg periods: HourPeriod<*>, lazyMessage: () -> Any = { "Periods must have matching times." }) {
    var previous: HourPeriod<*>? = null
    for (period in periods) {
        if (previous == null) previous = period
        require(previous.matches(period), lazyMessage = lazyMessage)
        previous = period
    }
}