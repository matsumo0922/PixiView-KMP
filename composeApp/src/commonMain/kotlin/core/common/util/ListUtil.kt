package core.common.util

object ListUtil {

    fun <T> List<T>.diff(list: List<T>): List<T> {
        val diff1 = this - list.toSet()
        val diff2 = list - this.toSet()
        return diff1 + diff2
    }
}
