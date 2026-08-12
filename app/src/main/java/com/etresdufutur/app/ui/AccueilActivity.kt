package com.etresdufutur.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.etresdufutur.app.R

class AccueilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accueil)

        val champAge = findViewById<Spinner>(R.id.champAge)
        val champMetier = findViewById<Spinner>(R.id.champMetier)
        val champSexe = findViewById<Spinner>(R.id.champSexe)
        val boutonCommencer = findViewById<Button>(R.id.boutonCommencer)

        val boutonMenu = findViewById<android.widget.ImageButton>(R.id.boutonMenu)
        boutonMenu.setOnClickListener {
            startActivity(Intent(this, HistoriqueActivity::class.java))
        }

        val optionsAge = listOf("Âge", "5-11 ans", "12-17 ans", "18-24 ans", "25-34 ans", "35-44 ans",
            "45-54 ans", "55-64 ans", "65-74 ans", "75+"
        )
        champAge.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, optionsAge)

        val optionsMetier = listOf("Métier", "Ingénieur", "Médecin", "Pompier", "Agriculteur",
            "Étudiant/Élève", "Retraité", "Professeur", "RH", "Technicien", "Psychologue",
            "Chercheur", "Manager", "Comptable", "Plombier", "Électricien", "Militaire", "Chauffeur"
            , "Livreur", "Vendeur", "Barman", "Journaliste", "Graphiste", "Designer", "Policier",
            "Autre/Ne préfère pas le dire"
        )
        champMetier.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, optionsMetier)

        val optionsSexe = listOf("Genre", "Femme", "Homme", "Autre / Préfère ne pas dire")
        champSexe.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, optionsSexe)

        boutonCommencer.setOnClickListener {
            if (optionsAge[champAge.selectedItemPosition] == "Âge") {
                Toast.makeText(this, "Veuillez sélectionner un âge", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (optionsMetier[champMetier.selectedItemPosition] == "Métier") {
                Toast.makeText(this, "Veuillez sélectionner un métier", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (optionsSexe[champSexe.selectedItemPosition] == "Genre") {
                Toast.makeText(this, "Veuillez sélectionner un genre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, QuestionnaireActivity::class.java).apply {
                putExtra("age", optionsAge[champAge.selectedItemPosition])
                putExtra("metier", optionsMetier[champMetier.selectedItemPosition])
                putExtra("sexe", optionsSexe[champSexe.selectedItemPosition])
            }
            startActivity(intent)
        }
    }
}