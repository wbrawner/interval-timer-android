package com.wbrawner.trainterval.model

import androidx.annotation.ColorRes
import androidx.room.*
import com.wbrawner.trainterval.R
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

@Entity(tableName = "interval_timer")
data class IntervalTimer(
    @PrimaryKey val id: Long? = null,
    val name: String = "",
    val description: String = "",
    val warmUpDuration: Long = TimeUnit.MINUTES.toMillis(5),
    val lowIntensityDuration: Long = TimeUnit.SECONDS.toMillis(30),
    val highIntensityDuration: Long = TimeUnit.MINUTES.toMillis(1),
    val restDuration: Long = TimeUnit.MINUTES.toMillis(1),
    val coolDownDuration: Long = TimeUnit.MINUTES.toMillis(5),
    val sets: Int = 4,
    val cycles: Int = 1
)

enum class Phase(@ColorRes val colorRes: Int) {
    WARM_UP(R.color.colorSurface),
    LOW_INTENSITY(R.color.colorSurfaceLowIntensity),
    HIGH_INTENSITY(R.color.colorSurfaceHighIntensity),
    REST(R.color.colorSurfaceRest),
    COOL_DOWN(R.color.colorSurfaceCoolDown),
}

@Dao
interface IntervalTimerDao {
    @Query("SELECT * FROM interval_timer")
    fun getAll(): Flow<List<IntervalTimer>>

    @Query("SELECT * FROM interval_timer WHERE id = :id")
    suspend fun getById(id: Long): IntervalTimer

    @Insert
    suspend fun insert(timer: IntervalTimer): Long

    @Update
    suspend fun update(timer: IntervalTimer)

    @Delete
    suspend fun delete(timer: IntervalTimer)
}
