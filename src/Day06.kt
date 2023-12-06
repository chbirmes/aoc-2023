import kotlin.math.pow
import kotlin.math.sqrt

fun main() {

    data class Race(val time: Long, val record: Long) {

        fun recordBeatingButtonTimes(): LongRange? {
            val timeHalves = time.toDouble() / 2
            val r = timeHalves.pow(2) - record
            return if (r < 0)
                null
            else {
                val lower = (timeHalves - sqrt(r)).toLong() + 1
                val upper = (timeHalves + sqrt(r)).let {
                    if (it.rem(1) == 0.0) it - 1 else it
                }
                    .toLong()
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
        return races.mapNotNull { it.recordBeatingButtonTimes() }
            .map { it.last - it.first + 1 }
            .fold(1) { x, y -> x * y }
    }

    fun parseSingleRace(lines: List<String>) =
        lines.map { line -> line.filter { it.isDigit() }.toLong() }
            .let { Race(it[0], it[1]) }

    fun part2(input: List<String>) =
        parseSingleRace(input).recordBeatingButtonTimes()!!
            .let { it.last - it.first + 1 }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288L)
    check(part2(testInput) == 71503L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
