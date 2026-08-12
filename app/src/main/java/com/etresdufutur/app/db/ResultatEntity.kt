package com.etresdufutur.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resultats")
data class ResultatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val age: String,
    val sexe: String,
    val metier: String,
    val personnage: String,

    // Décompte détaillé par type
    val decompteAnimiste: Int = 0,
    val decompteVivaliste: Int = 0,
    val decompteNaturaliste: Int = 0,

    val decompteFrugale: Int = 0,
    val decompteCooperation: Int = 0,
    val decompteCroissanceVerte: Int = 0,
    val decompteReparation: Int = 0,

    val decompteGeek: Int = 0,
    val decompteMouvement: Int = 0,
    val decompteSuiveur: Int = 0,
    val decompteMefiant: Int = 0,

    val date: Long = System.currentTimeMillis()
)
