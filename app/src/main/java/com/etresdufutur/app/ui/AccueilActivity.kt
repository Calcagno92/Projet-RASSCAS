package com.etresdufutur.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.etresdufutur.app.R

class AccueilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accueil)

        val champAge = findViewById<EditText>(R.id.champAge)
        val champMetier = findViewById<EditText>(R.id.champMetier)
        val champSexe = findViewById<Spinner>(R.id.champSexe)
        val boutonCommencer = findViewById<Button>(R.id.boutonCommencer)

        val boutonMenu = findViewById<android.widget.ImageButton>(R.id.boutonMenu)
        boutonMenu.setOnClickListener {
            startActivity(Intent(this, HistoriqueActivity::class.java))
        }

        val optionsSexe = listOf("Genre", "Femme", "Homme", "Autre / Préfère ne pas dire")
        champSexe.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, optionsSexe)

        boutonCommencer.setOnClickListener {
            val ageTexte = champAge.text.toString()
            val metier = champMetier.text.toString().trim()

            if (ageTexte.isBlank() || metier.isBlank()) {
                Toast.makeText(this, "Merci de remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageTexte.toIntOrNull()
            if (age == null || age <= 0) {
                Toast.makeText(this, "Âge invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (optionsSexe[champSexe.selectedItemPosition] == "Genre") {
                Toast.makeText(this, "Veuillez sélectionner un genre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken ?: champMetier.windowToken, 0)

            val intent = Intent(this, QuestionnaireActivity::class.java).apply {
                putExtra("age", age)
                putExtra("metier", metier)
                putExtra("sexe", optionsSexe[champSexe.selectedItemPosition])
            }
            startActivity(intent)
        }
    }
}