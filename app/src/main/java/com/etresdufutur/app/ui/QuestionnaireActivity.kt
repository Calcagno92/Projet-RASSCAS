package com.etresdufutur.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.etresdufutur.app.R
import com.etresdufutur.app.data.CalculateurProfil
import com.etresdufutur.app.data.Question
import com.etresdufutur.app.data.QuestionRepository
import kotlin.math.ceil

/**
 * Écran du questionnaire.
 *
 * Navigation :
 *  - flèches HAUT/BAS : déplacent la boule dans la colonne courante
 *  - flèches GAUCHE/DROITE : changent de colonne (gauche/droite de l'écran)
 *  - ENTRÉE (ou clic sur une réponse) : valide la réponse survolée
 *  - bouton retour (bas gauche) ou touche ESCAPE : revient à la question précédente
 *
 *  La Raspberry Pi émule un clavier HID (USB ou Bluetooth), donc ces mêmes
 *  KeyEvent sont reçus qu'importe le mode de connexion utilisé.
 **/
class QuestionnaireActivity : AppCompatActivity() {

    private lateinit var questions: List<Question>
    private lateinit var reponsesDonnees: Array<String?>   // type choisi pour chaque question, ou null
    private lateinit var reponsesIndex: Array<Int?>         // index de réponse choisi, pour restaurer le curseur au retour

    private var indexQuestion = 0
    private var curseur = 0 // index de la réponse actuellement survolée dans la question courante

    /* Anti-rebond : évite qu'un seul appui physique (Entrée, Suppr...) ne déclenche deux fois l'action
    si Android envoie l'événement en double (comportement observé sur certains claviers/émulateurs).*/
    private var dernierTraitementMs = 0L
    private val delaiAntiRebondMs = 250L

    private fun actionAutorisee(): Boolean {
        val maintenant = System.currentTimeMillis()
        if (maintenant - dernierTraitementMs < delaiAntiRebondMs) return false
        dernierTraitementMs = maintenant
        return true
    }

    // Coordonnées (colonne, ligne) pour chaque index de réponse de la question courante
    private var positions: List<Pair<Int, Int>> = emptyList()

    private lateinit var texteQuestion: TextView
    private lateinit var colonneGauche: LinearLayout
    private lateinit var colonneDroite: LinearLayout
    private lateinit var boule: View
    private lateinit var boutonRetour: View

    // Infos utilisateur transmises depuis l'accueil
    private var age = 0
    private var metier = ""
    private var sexe = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)

        age = intent.getIntExtra("age", 0)
        metier = intent.getStringExtra("metier") ?: ""
        sexe = intent.getStringExtra("sexe") ?: ""


        texteQuestion = findViewById(R.id.texteQuestion)
        colonneGauche = findViewById(R.id.colonneGauche)
        colonneDroite = findViewById(R.id.colonneDroite)
        boule = findViewById(R.id.boule)

        boutonRetour = findViewById(R.id.boutonRetour)
        boutonRetour.isFocusable = false
        boutonRetour.isFocusableInTouchMode = false

        questions = QuestionRepository.getQuestions(this)
        reponsesDonnees = arrayOfNulls(questions.size)
        reponsesIndex = arrayOfNulls(questions.size)

        boutonRetour.setOnClickListener { if (actionAutorisee()) questionPrecedente() }

        afficherQuestion(0)
    }

    //  Affichage d'une question

    private fun afficherQuestion(index: Int) {
        indexQuestion = index
        val question = questions[index]
        // Compteur conservé volontairement (utile pour se repérer pendant les tests).
        texteQuestion.text = "(Question ${index + 1} / ${questions.size})\n\n${titreTheme(question.theme)}\n\n${question.texte}"

        colonneGauche.removeAllViews()
        colonneDroite.removeAllViews()

        val nbReponses = question.reponses.size
        val nbGauche = ceil(nbReponses / 2.0).toInt()

        positions = question.reponses.indices.map { i ->
            if (i < nbGauche) Pair(0, i) else Pair(1, i - nbGauche)
        }

        question.reponses.forEachIndexed { i, reponse ->
            val vue = creerVueReponse(reponse.texte, i)
            if (positions[i].first == 0) colonneGauche.addView(vue) else colonneDroite.addView(vue)
        }

        // Restaure le curseur sur la réponse précédemment choisie si on revient en arrière,
        // sinon on démarre sur la première réponse.
        curseur = reponsesIndex[index] ?: 0

        boutonRetour.visibility = if (index == 0) View.INVISIBLE else View.VISIBLE

        mettreAJourSurbrillance()
    }

    // --- Aide : conversion dp → pixels, et fond visuel des blocs de réponse -------

    /** Titre affiché pour chaque thème, entre le compteur et le texte de la question. **/
    private fun titreTheme(theme: com.etresdufutur.app.data.Theme): String = when (theme) {
        com.etresdufutur.app.data.Theme.RAPPORT_VIVANT -> "Quel est ton rapport au vivant ?"
        com.etresdufutur.app.data.Theme.VISION_PROGRES -> "Qu'attends-tu du progrès ?"
        com.etresdufutur.app.data.Theme.RELATIONS_TECH -> "Quel est ton rapport aux technologies ?"
    }

    private fun dpVersPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    /**Fond arrondi d'une réponse : blanc semi-opaque au repos (lisible sur l'image de fond),
     rouge translucide avec bordure si survolée. **/
    private fun fondReponse(survolee: Boolean): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = dpVersPx(12).toFloat()
            setColor(if (survolee) 0x55E53935 else 0xCCFFFFFF.toInt())
            if (survolee) {
                setStroke(dpVersPx(2), 0xFFE53935.toInt())
            }
        }
    }

    private fun creerVueReponse(texte: String, index: Int): TextView {
        return TextView(this).apply {
            this.text = texte
            textSize = 18f                    // taille raisonnable, pas trop grande
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpVersPx(18), dpVersPx(14), dpVersPx(18), dpVersPx(14))
            background = fondReponse(survolee = false)

            // Hauteur qui s'adapte au contenu (pas de remplissage forcé de tout l'espace) :
            // chaque bloc ne prend que la taille nécessaire à son texte, avec une marge
            // raisonnable entre les réponses.
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpVersPx(10), 0, dpVersPx(10))
            }

            // IMPORTANT : une vue cliquable devient focusable par défaut, ce qui fait que le
            // système Android intercepte les touches DPAD pour déplacer le focus natif AVANT
            // qu'elles n'atteignent Activity.onKeyDown(). On désactive donc explicitement le
            // focus ici : la navigation est gérée entièrement "à la main" via le curseur/boule.
            isFocusable = false
            isFocusableInTouchMode = false
            setOnClickListener {
                curseur = index
                mettreAJourSurbrillance()
                if (actionAutorisee()) validerReponseCourante()
            }
        }
    }

    // Navigation par touches (flèches du Raspberry Pi en HID)

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { deplacer(deltaLigne = -1) ; return true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { deplacer(deltaLigne = 1) ; return true }
            KeyEvent.KEYCODE_DPAD_LEFT -> { changerColonne(0) ; return true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { changerColonne(1) ; return true }

            // Touches de VALIDATION
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_SPACE -> {
                if (actionAutorisee()) validerReponseCourante()
                return true
            }

            // Touches de RETOUR
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_FORWARD_DEL,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE -> {
                if (actionAutorisee()) questionPrecedente()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun deplacer(deltaLigne: Int) {
        val (col, ligne) = positions[curseur]
        val cible = positions.indexOfFirst { it.first == col && it.second == ligne + deltaLigne }
        if (cible != -1) {
            curseur = cible
            mettreAJourSurbrillance()
        }
    }

    private fun changerColonne(colCible: Int) {
        val (_, ligne) = positions[curseur]
        // On cherche la même ligne dans la colonne cible ; sinon la ligne la plus proche disponible.
        val candidats = positions.withIndex().filter { it.value.first == colCible }
        if (candidats.isEmpty()) return
        val meilleur = candidats.minByOrNull { Math.abs(it.value.second - ligne) }
        if (meilleur != null) {
            curseur = meilleur.index
            mettreAJourSurbrillance()
        }
    }

    // --- Surbrillance + déplacement visuel de la boule -----------------------------

    private fun mettreAJourSurbrillance() {
        val vue = vueDeIndex(curseur) ?: return
        vue.background = fondReponse(survolee = true)
        // Retire le fond "survolé" des autres réponses
        toutesLesVuesReponses().forEach { if (it !== vue) it.background = fondReponse(survolee = false) }

        // Positionne la boule à côté de la réponse sélectionnée, une fois le layout mesuré.
        vue.post {
            val posVue = IntArray(2)
            val posRacine = IntArray(2)
            vue.getLocationOnScreen(posVue)
            (boule.parent as View).getLocationOnScreen(posRacine)

            val y = (posVue[1] - posRacine[1] + vue.height / 2 - boule.height / 2).toFloat()
            val (col, _) = positions[curseur]
            val x = if (col == 0) {
                (posVue[0] - posRacine[0] + vue.width + 8).toFloat()   // juste à droite de la réponse gauche
            } else {
                (posVue[0] - posRacine[0] - boule.width - 8).toFloat() // juste à gauche de la réponse droite
            }
            boule.animate().x(x).y(y).setDuration(120).start()
        }
    }

    private fun vueDeIndex(index: Int): View? {
        val (col, ligne) = positions[index]
        val colonne = if (col == 0) colonneGauche else colonneDroite
        return (0 until colonne.childCount)
            .map { colonne.getChildAt(it) }
            .getOrNull(ligne)
    }

    private fun toutesLesVuesReponses(): List<View> =
        (0 until colonneGauche.childCount).map { colonneGauche.getChildAt(it) } +
                (0 until colonneDroite.childCount).map { colonneDroite.getChildAt(it) }

    // --- Validation / navigation entre questions ------------------------------------

    private fun validerReponseCourante() {
        val question = questions[indexQuestion]
        val reponse = question.reponses[curseur]

        reponsesDonnees[indexQuestion] = reponse.type
        reponsesIndex[indexQuestion] = curseur

        if (indexQuestion < questions.size - 1) {
            afficherQuestion(indexQuestion + 1)
        } else {
            terminerQuestionnaire()
        }
    }

    private fun questionPrecedente() {
        if (indexQuestion > 0) {
            afficherQuestion(indexQuestion - 1)
        }
    }

    private fun terminerQuestionnaire() {
        val calculateur = CalculateurProfil()
        questions.forEachIndexed { i, question ->
            val type = reponsesDonnees[i]
            if (type != null) {
                calculateur.enregistrerReponse(question.theme, type)
            }
        }

        val rapportVivant = calculateur.typeDominant(com.etresdufutur.app.data.Theme.RAPPORT_VIVANT) ?: ""
        val visionProgres = calculateur.typeDominant(com.etresdufutur.app.data.Theme.VISION_PROGRES) ?: ""
        val relationTech = calculateur.typeDominant(com.etresdufutur.app.data.Theme.RELATIONS_TECH) ?: ""

        val decompte = calculateur.decompteComplet()

        val intent = Intent(this, ResultatActivity::class.java).apply {
            putExtra("age", age)
            putExtra("sexe", sexe)
            putExtra("metier", metier)
            putExtra("rapportVivant", rapportVivant)
            putExtra("visionProgres", visionProgres)
            putExtra("relationTech", relationTech)
            // Décomptes détaillés, transmis un par un pour l'enregistrement en base
            decompte.forEach { (_, map) ->
                map.forEach { (type, valeur) -> putExtra("decompte_$type", valeur) }
            }
        }
        startActivity(intent)
        finish()
    }
}