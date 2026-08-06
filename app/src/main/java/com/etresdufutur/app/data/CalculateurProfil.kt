package com.etresdufutur.app.data

/**
 Accumule les réponses données par l'utilisateur et calcule, pour chaque thème,
 le type de réponse majoritaire.

 Règle en cas d'égalité (validée avec le client) : on garde le PREMIER type
 à avoir atteint le score maximal au fil du questionnaire, c'est-à-dire le
 type choisi en premier par l'utilisateur parmi ceux qui terminent à égalité.
 **/
class CalculateurProfil {

    // theme (type → nombre de fois choisi), on garde aussi l'ordre d'apparition
    private val comptages: MutableMap<Theme, LinkedHashMap<String, Int>> =
        Theme.values().associateWith { LinkedHashMap<String, Int>() }.toMutableMap()

    fun enregistrerReponse(theme: Theme, type: String) {
        val map = comptages.getValue(theme)
        map[type] = (map[type] ?: 0) + 1
    }

    /** Renvoie le type dominant pour un thème (1er type ayant le score max, en cas d'égalité,
      on garde celui qui a été choisi en premier — l'ordre d'insertion dans le LinkedHashMap
      reflète l'ordre de première sélection).
     **/
    fun typeDominant(theme: Theme): String? {
        val map = comptages.getValue(theme)
        if (map.isEmpty()) return null
        val maxScore = map.values.max()
        // .entries conserve l'ordre d'insertion (LinkedHashMap) → le premier trouvé
        // avec le score max est bien le premier type sélectionné par l'utilisateur.
        return map.entries.first { it.value == maxScore }.key
    }

    fun decompteComplet(): Map<Theme, Map<String, Int>> = comptages

    fun reinitialiser() {
        comptages.values.forEach { it.clear() }
    }
}
