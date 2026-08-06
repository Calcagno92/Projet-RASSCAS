package com.etresdufutur.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PersonnageRepository {

    private var cache: List<Personnage>? = null

    fun getPersonnages(context: Context): List<Personnage> {
        cache?.let { return it }

        val json = context.assets.open("personnages.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val type = object : TypeToken<List<Personnage>>() {}.type
        val personnages: List<Personnage> = Gson().fromJson(json, type)
        cache = personnages
        return personnages
    }

    /**
     Trouve le personnage correspondant à la combinaison des 3 profils dominants.
     Retourne null si aucune correspondance n'est trouvée
     **/
    fun trouverPersonnage(
        context: Context,
        rapportVivant: String,
        visionProgres: String,
        relationTech: String
    ): Personnage? {
        return getPersonnages(context).firstOrNull {
            it.rapportVivant.equals(rapportVivant, ignoreCase = true) &&
            it.visionProgres.equals(visionProgres, ignoreCase = true) &&
            it.relationTech.equals(relationTech, ignoreCase = true)
        }
    }

    /**
     * Résout le nom de fichier (ex : "panoramix.png") en identifiant de ressource drawable.
     * Les images sont placées dans res/drawable/ avec le même nom.
     **/
    fun resoudreDrawable(context: Context, imageFile: String): Int {
        val nomSansExtension = imageFile.substringBeforeLast(".")
        val resId = context.resources.getIdentifier(
            nomSansExtension, "drawable", context.packageName
        )
        return resId // 0 si non trouvé
    }
}
