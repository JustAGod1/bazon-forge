package ru.justagod.serialization

import com.google.common.base.Charsets
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTSizeTracker
import net.minecraft.nbt.NBTTagCompound

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.time.*
import java.util.ArrayList
import java.util.UUID

object Serializers {

    @JvmStatic
    fun writeStringList(list: Collection<String>, output: DataOutput) {
        output.writeInt(list.size)
        for (s in list)
            output.writeUTF(s)
    }

    @JvmStatic
    fun readStringList(input: DataInput): List<String> {
        val size = input.readInt()
        val list = ArrayList<String>(size)
        for (i in 0 until size) {
            list.add(input.readUTF())
        }
        return list
    }

    @JvmStatic
    fun writeNBTTagCompound(tag: NBTTagCompound?, output: DataOutput) {
        if (tag == null) {
            output.writeInt(-1)
        } else {
            val abyte = CompressedStreamTools.compress(tag)
            output.writeInt(abyte.size)
            output.write(abyte)
        }
    }

    @JvmStatic
    fun readNBTTagCompound(input: DataInput): NBTTagCompound? {
        val length = input.readInt()

        if (length < 0) {
            return null
        } else {
            val abyte = ByteArray(length)
            input.readFully(abyte)
            return CompressedStreamTools.func_152457_a(abyte, NBTSizeTracker((1024 * 1024 * 1024).toLong()))
        }
    }

    @JvmStatic
    fun writeUUID(id: UUID, output: DataOutput) {
        output.writeLong(id.mostSignificantBits)
        output.writeLong(id.leastSignificantBits)
    }

    @JvmStatic
    fun readUUID(input: DataInput): UUID {
        return UUID(input.readLong(), input.readLong())
    }


    @JvmStatic
    fun writeLongString(text: String, output: DataOutput) {
        val arr = text.toByteArray(Charsets.UTF_8)
        output.writeInt(arr.size)
        output.write(arr)
    }

    @JvmStatic
    fun readLongString(input: DataInput): String {
        val bytes = ByteArray(input.readInt())
        input.readFully(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    @JvmStatic
    fun writeDuration(duration: Duration, output: DataOutput) {
        output.writeLong(duration.seconds)
        output.writeInt(duration.nano)
    }

    @JvmStatic
    fun readDuration(input: DataInput): Duration {
        return Duration.ofSeconds(input.readLong(), input.readInt().toLong())
    }

    @JvmStatic
    fun writeInstant(instant: Instant, output: DataOutput) {
        output.writeLong(instant.epochSecond)
        output.writeInt(instant.nano)
    }

    @JvmStatic
    fun readInstant(input: DataInput): Instant {
        return Instant.ofEpochSecond(input.readLong(), input.readInt().toLong())
    }

    @JvmStatic
    fun writeLocalDate(localDate: LocalDate, output: DataOutput) {
        output.writeInt(localDate.year)
        output.writeShort(localDate.monthValue)
        output.writeShort(localDate.dayOfMonth)
    }

    @JvmStatic
    fun readLocalDate(input: DataInput): LocalDate {
        return LocalDate.of(input.readInt(), input.readShort().toInt(), input.readShort().toInt())
    }

    @JvmStatic
    fun writeLocalDateTime(localDateTime: LocalDateTime, output: DataOutput) {
        writeLocalDate(localDateTime.toLocalDate(), output)
        writeLocalTime(localDateTime.toLocalTime(), output)
    }

    @JvmStatic
    fun readLocalDateTime(input: DataInput): LocalDateTime {
        return LocalDateTime.of(readLocalDate(input), readLocalTime(input))
    }

    @JvmStatic
    fun writeLocalTime(localTime: LocalTime, output: DataOutput) {
        output.writeByte(localTime.hour)
        output.writeByte(localTime.minute)
        output.writeByte(localTime.second)
        output.writeInt(localTime.nano)
    }

    @JvmStatic
    fun readLocalTime(input: DataInput): LocalTime {
        return LocalTime.of(input.readByte().toInt(), input.readByte().toInt(), input.readByte().toInt(), input.readInt())
    }

    @JvmStatic
    fun writeZoneId(zoneId: ZoneId, output: DataOutput) {
        if (zoneId is ZoneOffset) {
            output.writeBoolean(true)
            writeZoneOffset(zoneId, output)
        } else {
            output.writeBoolean(false)
            output.writeUTF(zoneId.id)
        }
    }

    @JvmStatic
    fun readZoneId(input: DataInput): ZoneId {
        return if (input.readBoolean()) {
            readZoneOffset(input)
        } else {
            ZoneId.of(input.readUTF())
        }
    }

    @JvmStatic
    fun writeZoneOffset(zoneOffset: ZoneOffset, output: DataOutput) {
        output.writeInt(zoneOffset.totalSeconds)
    }

    @JvmStatic
    fun readZoneOffset(input: DataInput): ZoneOffset {
        return ZoneOffset.ofTotalSeconds(input.readInt())
    }

    @JvmStatic
    fun writeZonedDateTime(zonedDateTime: ZonedDateTime, output: DataOutput) {
        writeLocalDateTime(zonedDateTime.toLocalDateTime(), output)
        writeZoneId(zonedDateTime.zone, output)
        writeZoneOffset(zonedDateTime.offset, output)
    }

    @JvmStatic
    fun readZonedDateTime(input: DataInput): ZonedDateTime {
        return ZonedDateTime.ofLocal(readLocalDateTime(input), readZoneId(input), readZoneOffset(input))
    }

    @JvmStatic
    fun writeOffsetTime(offsetTime: OffsetTime, output: DataOutput) {
        writeLocalTime(offsetTime.toLocalTime(), output)
        writeZoneOffset(offsetTime.offset, output)
    }

    @JvmStatic
    fun readOffsetTime(input: DataInput): OffsetTime {
        return OffsetTime.of(readLocalTime(input), readZoneOffset(input))
    }

    @JvmStatic
    fun writeOffsetDateTime(offsetDateTime: OffsetDateTime, output: DataOutput) {
        writeLocalDateTime(offsetDateTime.toLocalDateTime(), output)
        writeZoneOffset(offsetDateTime.offset, output)
    }

    @JvmStatic
    fun readOffsetDateTime(input: DataInput): OffsetDateTime {
        return OffsetDateTime.of(readLocalDateTime(input), readZoneOffset(input))
    }

    @JvmStatic
    fun writeYear(year: Year, output: DataOutput) {
        output.writeInt(year.value)
    }

    @JvmStatic
    fun readYear(input: DataInput): Year {
        return Year.of(input.readInt())
    }

    @JvmStatic
    fun writeYearMonth(yearMonth: YearMonth, output: DataOutput) {
        output.writeInt(yearMonth.year)
        output.writeInt(yearMonth.monthValue)
    }

    @JvmStatic
    fun readYearMonth(input: DataInput): YearMonth {
        return YearMonth.of(input.readInt(), input.readInt())
    }

    @JvmStatic
    fun writeMonthDay(monthDay: MonthDay, output: DataOutput) {
        output.writeInt(monthDay.monthValue)
        output.writeInt(monthDay.dayOfMonth)
    }

    @JvmStatic
    fun readMonthDay(input: DataInput): MonthDay {
        return MonthDay.of(input.readInt(), input.readInt())
    }

    @JvmStatic
    fun writePeriod(period: Period, output: DataOutput) {
        output.writeInt(period.years)
        output.writeInt(period.months)
        output.writeInt(period.days)
    }

    @JvmStatic
    fun readPeriod(input: DataInput): Period {
        return Period.of(input.readInt(), input.readInt(), input.readInt())
    }

}
