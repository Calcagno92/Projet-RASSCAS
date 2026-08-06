package com.etresdufutur.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.etresdufutur.app.R
import com.etresdufutur.app.db.AppDatabase
import com.etresdufutur.app.db.ResultatEntity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran de consultation du contenu de la base de données (accessible uniquement
 * depuis l'icône menue de l'écran d'accueil). Affiche toutes les entrées enregistrées
 * et permet de les exporter en CSV.
 **/
class HistoriqueActivity : AppCompatActivity() {

    private lateinit var listeResultats: LinearLayout
    private lateinit var texteVide: TextView
    private var resultatsCourants: List<ResultatEntity> = emptyList()

    private val formatDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historique)

        listeResultats = findViewById(R.id.listeResultats)
        texteVide = findViewById(R.id.texteVide)

        findViewById<ImageButton>(R.id.boutonFermer).setOnClickListener { finish() }
        findViewById<Button>(R.id.boutonExporter).setOnClickListener { exporterEnCsv() }

        chargerResultats()
    }

    private fun chargerResultats() {
        lifecycleScope.launch {
            val resultats = AppDatabase.getInstance(this@HistoriqueActivity).resultatDao().tousLesResultats()
            resultatsCourants = resultats
            afficherResultats(resultats)
        }
    }

    private fun afficherResultats(resultats: List<ResultatEntity>) {
        listeResultats.removeAllViews()

        if (resultats.isEmpty()) {
            texteVide.visibility = android.view.View.VISIBLE
            return
        }
        texteVide.visibility = android.view.View.GONE

        resultats.forEach { r ->
            listeResultats.addView(creerCarteResultat(r))
        }
    }

    private fun creerCarteResultat(r: ResultatEntity): TextView {
        val decompte = buildString {
            append("Rapport au vivant — Animiste: ${r.decompteAnimiste}, Vivaliste: ${r.decompteVivaliste}, Naturaliste: ${r.decompteNaturaliste}\n")
            append("Vision du progrès — Frugale: ${r.decompteFrugale}, Coopération: ${r.decompteCooperation}, Croissance verte: ${r.decompteCroissanceVerte}, Réparation: ${r.decompteReparation}\n")
            append("Relations aux technologies — Geek: ${r.decompteGeek}, Dans le mouvement: ${r.decompteMouvement}, Suiveur: ${r.decompteSuiveur}, Méfiant: ${r.decompteMefiant}")
        }

        val texte = "🧑 Âge: ${r.age}  •  Sexe: ${r.sexe}  •  Métier: ${r.metier}\n" +
                "🎭 Personnage: ${r.personnage}\n" +
                "📅 ${formatDate.format(Date(r.date))}\n\n" +
                decompte

        return TextView(this).apply {
            text = texte
            textSize = 14f
            setTextColor(0xFF212121.toInt())
            setPadding(dpVersPx(16), dpVersPx(12), dpVersPx(16), dpVersPx(12))
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpVersPx(10).toFloat()
                setColor(0xCCFFFFFF.toInt())
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpVersPx(6), 0, dpVersPx(6))
            }
        }
    }

    private fun dpVersPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    //  Export CSV

    private fun exporterEnCsv() {
        if (resultatsCourants.isEmpty()) {
            Toast.makeText(this, "Aucune donnée à exporter", Toast.LENGTH_SHORT).show()
            return
        }

        val entete = listOf(
            "age", "sexe", "metier", "personnage", "date",
            "animiste", "vivaliste", "naturaliste",
            "frugale", "cooperation", "croissance_verte", "reparation",
            "geek", "dans_le_mouvement", "suiveur", "mefiant"
        ).joinToString(";")

        val lignes = resultatsCourants.joinToString("\n") { r ->
            listOf(
                r.age, echapperCsv(r.sexe), echapperCsv(r.metier), echapperCsv(r.personnage),
                formatDate.format(Date(r.date)),
                r.decompteAnimiste, r.decompteVivaliste, r.decompteNaturaliste,
                r.decompteFrugale, r.decompteCooperation, r.decompteCroissanceVerte, r.decompteReparation,
                r.decompteGeek, r.decompteMouvement, r.decompteSuiveur, r.decompteMefiant
            ).joinToString(";")
        }

        val contenuCsv = "$entete\n$lignes"

        try {
            val dossierExport = File(getExternalFilesDir(null), "exports").apply { mkdirs() }
            val nomFichier = "resultats_${System.currentTimeMillis()}.csv"
            val fichier = File(dossierExport, nomFichier)
            // BOM UTF-8 pour qu'Excel affiche correctement les accents à l'ouverture
            fichier.writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + contenuCsv.toByteArray(Charsets.UTF_8))

            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", fichier)

            val intentPartage = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intentPartage, "Exporter le CSV vers..."))
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de l'export : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Échappe les points-virgules/guillemets/retours à la ligne pour rester un CSV valide. **/
    private fun echapperCsv(valeur: String): String {
        return if (valeur.contains(";") || valeur.contains("\"") || valeur.contains("\n")) {
            "\"${valeur.replace("\"", "\"\"")}\""
        } else {
            valeur
        }
    }
}
