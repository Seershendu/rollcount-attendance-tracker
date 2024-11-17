package fm.mrc.rollcount.data

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    NONE
}

data class AttendanceDay(
    val date: Long,
    val status: AttendanceStatus = AttendanceStatus.NONE
) 