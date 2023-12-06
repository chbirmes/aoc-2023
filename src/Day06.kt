import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {

    fun LongRange.length() = last - first + 1

    data class Race(val time: Long, val record: Long) {

        fun recordBeatingButtonTimes(): LongRange? {
            val timeHalves = time.toDouble() / 2
            val r = timeHalves.pow(2) - (record + 1)
            return if (r < 0)
                null
            else {
                val lower = ceil(timeHalves - sqrt(r)).toLong()
                val upper = floor(timeHalves + sqrt(r)).toLong()
                lower..upper
            }
        }
    }

    val whitespace = "\\s+".toRegex()
    fun parseRaces(lines: List<String>): List<Race> {
        val numberLists = lines.map { line ->
            line.substringAfter(':')
                .trim()
                .split(whitespace)
                .map { it.toLong() }
        }
        return numberLists[0].zip(numberLists[1]) { time, record -> Race(time, record) }
    }

    fun part1(input: List<String>): Long {
        val races = parseRaces(input)
        return races.mapNotNull { it.recordBeatingButtonTimes()?.length() }
            .fold(1) { x, y -> x * y }
    }

    fun parseSingleRace(lines: List<String>) =
        lines.map { line -> line.filter { it.isDigit() }.toLong() }
            .let { Race(it[0], it[1]) }

    fun part2(input: List<String>) =
        parseSingleRace(input).recordBeatingButtonTimes()!!.length()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288L)
    check(part2(testInput) == 71503L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
