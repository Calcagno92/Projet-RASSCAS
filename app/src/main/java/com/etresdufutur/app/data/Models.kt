package com.etresdufutur.app.data

/**
 Les 3 thèmes du questionnaire, dans l'ordre d'affichage.
 **/
enum class Theme {
    RAPPORT_VIVANT,
    VISION_PROGRES,
    RELATIONS_TECH
}

data class Reponse(
    val texte: String,
    val type: String // ex: "Animiste", "Frugale", "Geek"...
)

data class Question(
    val id: Int,
    val theme: Theme,
    val texte: String,
    val reponses: List<Reponse>
)

data class Personnage(
    val rapportVivant: String,
    val visionProgres: String,
    val relationTech: String,
    val nom: String,
    val explication: String,
    val imageFile: String // nom de fichier, ex: "panoramix.png"
)
