package com.wbrawner.trainterval.shared

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.room.*
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
) {
    fun durationForPhase(phase: Phase) = when (phase) {
        Phase.WARM_UP -> warmUpDuration
        Phase.LOW_INTENSITY -> lowIntensityDuration
        Phase.HIGH_INTENSITY -> highIntensityDuration
        Phase.REST -> restDuration
        Phase.COOL_DOWN -> coolDownDuration
    }

    val totalDuration: Long
        get() = warmUpDuration + coolDownDuration + (
                (lowIntensityDuration + highIntensityDuration) * sets * cycles
                ) + (restDuration * cycles) - restDuration
}

enum class Phase(
    @ColorRes val colorRes: Int,
    @StringRes val stringRes: Int,
    val soundFile: String
) {
    WARM_UP(R.color.colorSurface, R.string.phase_warm_up, "warm.mp3"),
    LOW_INTENSITY(R.color.colorSurfaceLowIntensity, R.string.phase_low_intensity, "low.mp3"),
    HIGH_INTENSITY(R.color.colorSurfaceHighIntensity, R.string.phase_high_intensity, "high.mp3"),
    REST(R.color.colorSurfaceRest, R.string.phase_rest, "rest.mp3"),
    COOL_DOWN(R.color.colorSurfaceCoolDown, R.string.phase_cool_down, "cool.mp3"),
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
