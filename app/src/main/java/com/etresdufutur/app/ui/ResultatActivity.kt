package com.etresdufutur.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.etresdufutur.app.R
import com.etresdufutur.app.data.PersonnageRepository
import com.etresdufutur.app.db.AppDatabase
import com.etresdufutur.app.db.ResultatEntity
import kotlinx.coroutines.launch

class ResultatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultat)

        val age = intent.getIntExtra("age", 0)
        val sexe = intent.getStringExtra("sexe") ?: ""
        val metier = intent.getStringExtra("metier") ?: ""
        val rapportVivant = intent.getStringExtra("rapportVivant") ?: ""
        val visionProgres = intent.getStringExtra("visionProgres") ?: ""
        val relationTech = intent.getStringExtra("relationTech") ?: ""

        val personnage = PersonnageRepository.trouverPersonnage(
            this, rapportVivant, visionProgres, relationTech
        )

        val nomPersonnage = findViewById<TextView>(R.id.nomPersonnage)
        val imagePersonnage = findViewById<ImageView>(R.id.imagePersonnage)
        val explicationPersonnage = findViewById<TextView>(R.id.explicationPersonnage)
        val boutonTerminer = findViewById<Button>(R.id.boutonTerminer)

        if (personnage != null) {
            nomPersonnage.text = personnage.nom
            explicationPersonnage.text = personnage.explication
            val resId = PersonnageRepository.resoudreDrawable(this, personnage.imageFile)
            if (resId != 0) imagePersonnage.setImageResource(resId)
        } else {
            /* N'arrive pas si les 48 combinaisons sont bien couvertes, mais on évite un crash
            si jamais une combinaison manque.*/
            nomPersonnage.text = "Profil non trouvé"
            explicationPersonnage.text =
                "Combinaison ($rapportVivant / $visionProgres / $relationTech) absente de personnages.json."
        }
 
        boutonTerminer.setOnClickListener {
            enregistrerResultat(
                age, sexe, metier,
                personnage?.nom ?: "Inconnu"
            )
        }
    }

    private fun enregistrerResultat(age: Int, sexe: String, metier: String, nomPersonnage: String) {
        val extras = intent.extras

        val entite = ResultatEntity(
            age = age,
            sexe = sexe,
            metier = metier,
            personnage = nomPersonnage,
            decompteAnimiste = extras?.getInt("decompte_Animiste", 0) ?: 0,
            decompteVivaliste = extras?.getInt("decompte_Vivaliste", 0) ?: 0,
            decompteNaturaliste = extras?.getInt("decompte_Naturaliste", 0) ?: 0,
            decompteFrugale = extras?.getInt("decompte_Frugale", 0) ?: 0,
            decompteCooperation = extras?.getInt("decompte_Coopération", 0) ?: 0,
            decompteCroissanceVerte = extras?.getInt("decompte_Croissance verte", 0) ?: 0,
            decompteReparation = extras?.getInt("decompte_Réparation", 0) ?: 0,
            decompteGeek = extras?.getInt("decompte_Geek", 0) ?: 0,
            decompteMouvement = extras?.getInt("decompte_Dans le mouvement", 0) ?: 0,
            decompteSuiveur = extras?.getInt("decompte_Suiveur", 0) ?: 0,
            decompteMefiant = extras?.getInt("decompte_Méfiant", 0) ?: 0
        )

        lifecycleScope.launch {
            AppDatabase.getInstance(this@ResultatActivity).resultatDao().inserer(entite)
            retourAccueil()
        }
    }

    private fun retourAccueil() {
        val intent = Intent(this, AccueilActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
