package fm.mrc.rollcount.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fm.mrc.rollcount.data.AttendanceStatus

class AttendanceViewModel : ViewModel() {
    var attendanceState by mutableStateOf(mapOf<Long, AttendanceStatus>())
        private set

    fun markAttendance(date: Long, status: AttendanceStatus) {
        attendanceState = attendanceState.toMutableMap().apply {
            put(date, status)
        }
    }

    fun getAttendancePercentage(): Float {
        if (attendanceState.isEmpty()) return 0f
        val presentDays = attendanceState.values.count { it == AttendanceStatus.PRESENT }
        return (presentDays.toFloat() / attendanceState.size) * 100
    }

    fun getAttendanceCounts(): Pair<Int, Int> {
        val presentDays = attendanceState.values.count { it == AttendanceStatus.PRESENT }
        val absentDays = attendanceState.values.count { it == AttendanceStatus.ABSENT }
        return Pair(presentDays, absentDays)
    }
} 