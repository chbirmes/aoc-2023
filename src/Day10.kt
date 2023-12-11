fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1
    val allDirections = setOf(north, east, south, west)

    val symbolConnections = mapOf(
        'S' to allDirections,
        '|' to setOf(north, south),
        '-' to setOf(east, west),
        'L' to setOf(north, east),
        'J' to setOf(north, west),
        '7' to setOf(south, west),
        'F' to setOf(south, east)
    )

    data class PipeMaze(val lines: List<String>) {

        val rowRange = lines.indices
        val columnRange = lines.first().indices

        operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)
        operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = (first - other.first) to (second - other.second)

        fun Pair<Int, Int>.isInRange() = first in rowRange && second in columnRange

        fun Pair<Int, Int>.tile() = lines[first][second]

        fun Char.connectsIn(direction: Pair<Int, Int>) = symbolConnections[this]?.contains(direction) ?: false

        fun Pair<Int, Int>.connectsTo(other: Pair<Int, Int>): Boolean {
            val direction = other - this
            val oppositeDirection = this - other
            return this.tile().connectsIn(direction) && other.tile().connectsIn(oppositeDirection)
        }

        val start: Pair<Int, Int> by lazy {
            lines.asSequence()
                .mapIndexed { rowIndex, line -> rowIndex to line.indexOf('S') }
                .first { it.second != -1 }
        }

        fun Pair<Int, Int>.connections() =
            allDirections
                .map { this + it }
                .filter { it.isInRange() && it.connectsTo(this) }

        fun Pair<Int, Int>.nextConnectionComingFrom(previous: Pair<Int, Int>): Pair<Int, Int> =
            connections().filterNot { it == previous }.single()

        fun findLoop(): Set<Pair<Int, Int>> {
            val startConnection = start.connections().first().let { start to it }
            val stepSequence: Sequence<Pair<Pair<Int, Int>, Pair<Int, Int>>> =
                generateSequence(startConnection) { (previous, current) ->
                    current to current.nextConnectionComingFrom(previous)
                }
            return stepSequence.map { it.second }.takeWhile { it != start }.toSet() + start
        }

        fun clearNonLoopPipesAndStart(): PipeMaze {
            val loop = findLoop()
            val newLines = lines.mapIndexed { rowIndex, line ->
                line.mapIndexed { columnIndex, char ->
                    val position = rowIndex to columnIndex
                    when (position) {
                        start -> replacementForStart()
                        in loop -> char
                        else -> '.'
                    }
                }
                    .joinToString(separator = "")
            }
            return PipeMaze(newLines)
        }

        fun replacementForStart(): Char {
            val startDirections = start.connections().map { it - start }.toSet()
            return symbolConnections.filterValues { it == startDirections }.keys.single()
        }

        val horizontalTransition = "\\||F-*J|L-*7".toRegex()

        fun insideTileCount(line: String): Int {
            val matches = horizontalTransition.findAll(line).toList()
            return if (matches.isEmpty())
                0
            else {
                val insideRanges = matches.chunked(2) { (it[0].range.last + 1)..<(it[1].range.first) }
                insideRanges.sumOf { range -> line.substring(range).count { it == '.' } }
            }
        }

        fun countInsideTiles() = lines.sumOf { insideTileCount(it) }
    }

    fun part1(input: List<String>) = PipeMaze(input).findLoop().size / 2

    fun part2(input: List<String>) = PipeMaze(input).clearNonLoopPipesAndStart().countInsideTiles()

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day10_test")) == 8)
    check(part2(readInput("Day10_test2")) == 10)

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}
