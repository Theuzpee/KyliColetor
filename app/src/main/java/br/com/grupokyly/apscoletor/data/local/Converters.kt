package br.com.grupokyly.apscoletor.data.local

import androidx.room.TypeConverter
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.SkipReason

class Converters {

    @TypeConverter
    fun fromBoxStatus(value: BoxStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBoxStatus(value: String): BoxStatus {
        return try {
            BoxStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            BoxStatus.EM_COLETA
        }
    }

    @TypeConverter
    fun fromItemStatus(value: ItemStatus): String {
        return value.name
    }

    @TypeConverter
    fun toItemStatus(value: String): ItemStatus {
        return try {
            ItemStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ItemStatus.PENDENTE
        }
    }

    @TypeConverter
    fun fromSkipReason(value: SkipReason?): String? = value?.name

    @TypeConverter
    fun toSkipReason(value: String?): SkipReason? =
        value?.let { SkipReason.valueOf(it) }
}
