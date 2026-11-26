
package com.example.tiketiin.domain

import java.util.Locale

object LocationMatcher {

    private data class Location(
        val type: String?,
        val name: String
    )

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .replace(Regex("^kab\\.?\\s+"), "kabupaten ")
            .replace(Regex("^kot\\.?\\s+"), "kota ")
    }

    private fun parse(value: String): Location {
        val clean = normalize(value)

        return when {
            clean.startsWith("kabupaten ") -> Location(
                type = "kabupaten",
                name = clean.removePrefix("kabupaten ")
            )

            clean.startsWith("kota ") -> Location(
                type = "kota",
                name = clean.removePrefix("kota ")
            )

            else -> Location(
                type = null,
                name = clean
            )
        }
    }

    fun isSameCity(
        profileCity: String,
        eventCity: String,
        knownLocations: Collection<String> = emptyList(),
        resolvedEventCity: String? = null
    ): Boolean {
        val profile = parse(profileCity)

        val event = parse(
            resolvedEventCity
                ?.takeIf { it.isNotBlank() }
                ?: eventCity
        )

        if (profile.name.isBlank() || event.name.isBlank()) {
            return false
        }

        if (profile.name != event.name) {
            return false
        }

        if (profile.type != null && event.type != null) {
            return profile.type == event.type
        }

        if (profile.type == null && event.type == null) {
            return true
        }

        if (knownLocations.isEmpty()) {
            return false
        }

        val possibleLocations = knownLocations
            .map(::parse)
            .filter { it.name == profile.name }
            .distinct()

        if (possibleLocations.size != 1) {
            return false
        }

        val onlyLocation = possibleLocations.first()

        return (profile.type == null ||
                profile.type == onlyLocation.type) &&
                (event.type == null ||
                        event.type == onlyLocation.type)
    }
}