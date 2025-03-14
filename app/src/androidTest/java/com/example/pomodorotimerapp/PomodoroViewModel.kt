import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PomodoroViewModel @Inject constructor(private val repository: PomodoroRepository) : ViewModel() {
    fun insert(session: PomodoroSession) {
        viewModelScope.launch {
            repository.insert(session)
        }
    }

    fun getSessionsForDay(startOfDay: Long, endOfDay: Long) {
        viewModelScope.launch {
            val sessions = repository.getSessionsForDay(startOfDay, endOfDay)
            // Update UI with sessions
        }
    }

    fun getSessionsForWeek(startOfWeek: Long, endOfWeek: Long) {
        viewModelScope.launch {
            val sessions = repository.getSessionsForWeek(startOfWeek, endOfWeek)
            // Update UI with sessions
        }
    }

    fun getSessionsForMonth(startOfMonth: Long, endOfMonth: Long) {
        viewModelScope.launch {
            val sessions = repository.getSessionsForMonth(startOfMonth, endOfMonth)
            // Update UI with sessions
        }
    }
}