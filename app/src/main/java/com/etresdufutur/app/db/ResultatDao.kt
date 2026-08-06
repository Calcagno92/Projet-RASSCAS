package com.etresdufutur.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ResultatDao {

    @Insert
    suspend fun inserer(resultat: ResultatEntity)

    @Query("SELECT * FROM resultats ORDER BY date DESC")
    suspend fun tousLesResultats(): List<ResultatEntity>
}
