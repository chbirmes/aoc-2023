import kotlin.math.max

fun main() {

    val maxCubes = mapOf("red" to 12, "green" to 13, "blue" to 14)
    val colors = maxCubes.keys

    data class Game(val id: Int, val cubeSets: List<Map<String, Int>>) {

        fun Map<String, Int>.possible() = all { (color, count) -> count <= (maxCubes[color] ?: 0) }

        fun possible() = cubeSets.all { it.possible() }

        fun minimumSet(): Map<String, Int> =
            cubeSets.fold(emptyMap()) { x, y ->
                colors.associateWith { max(x[it] ?: 0, y[it] ?: 0) }
            }
    }

    fun parseGame(line: String) =
        line.split(':', ';')
            .let { gameTagAndTurns ->
                val gameId = gameTagAndTurns.first().substringAfter(' ').toInt()
                val cubeSets = gameTagAndTurns
                    .drop(1)
                    .map { turn ->
                        turn.split(',')
                            .associate { countAndColor ->
                                countAndColor.trim()
                                    .split(' ')
                                    .let { it[1] to it[0].toInt() }
                            }
                    }
                Game(gameId, cubeSets)
            }

    fun part1(input: List<String>) = input.map { parseGame(it) }.filter { it.possible() }.sumOf { it.id }

    fun Map<String, Int>.power() = colors.map { get(it) ?: 0 }.fold(1) { x, y -> x * y }

    fun part2(input: List<String>) = input.map { parseGame(it) }.sumOf { it.minimumSet().power() }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 8)
    check(part2(testInput) == 2286)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
