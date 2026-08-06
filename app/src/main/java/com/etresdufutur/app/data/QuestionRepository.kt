package com.etresdufutur.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object QuestionRepository {

    private var cache: List<Question>? = null

    fun getQuestions(context: Context): List<Question> {
        cache?.let { return it }

        val json = context.assets.open("questions.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val type = object : TypeToken<List<Question>>() {}.type
        val questions: List<Question> = Gson().fromJson(json, type)

        // Trie par thème dans l'ordre voulu, puis par id, pour garantir l'ordre d'affichage
        val ordreThemes = listOf(Theme.RAPPORT_VIVANT, Theme.VISION_PROGRES, Theme.RELATIONS_TECH)
        val trie = questions.sortedWith(
            compareBy({ ordreThemes.indexOf(it.theme) }, { it.id })
        )

        cache = trie
        return trie
    }

    fun getQuestionsParTheme(context: Context, theme: Theme): List<Question> =
        getQuestions(context).filter { it.theme == theme }
}
