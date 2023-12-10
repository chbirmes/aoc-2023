fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1
    val directions = listOf(north, east, south, west)

    data class PipeMaze(val lines: List<String>) {

        val rowRange = lines.indices
        val columnRange = lines.first().indices

        operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)
        operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = (first - other.first) to (second - other.second)

        fun Pair<Int, Int>.isInRange() = first in rowRange && second in columnRange

        fun Pair<Int, Int>.tile() = lines[first][second]

        fun Char.connectsIn(direction: Pair<Int, Int>) =
            when (this) {
                'S' -> true
                '|' -> direction == north || direction == south
                '-' -> direction == east || direction == west
                'L' -> direction == north || direction == east
                'J' -> direction == north || direction == west
                '7' -> direction == south || direction == west
                'F' -> direction == south || direction == east
                else -> false
            }

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
            directions
                .map { this + it }
                .filter { it.isInRange() && it.connectsTo(this) }

        fun Pair<Int, Int>.nextConnectionComingFrom(previous: Pair<Int, Int>): Pair<Int, Int> =
            connections().filterNot { it == previous }.single()

        fun findLoop(): Set<Pair<Int, Int>> {
            val startConnection = start.connections().map { start to it }.first()
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
                    if (position == start)
                        if (start + north in loop)
                            if (start + east in loop) 'L'
                            else if (start + south in loop) '|'
                            else 'J'
                        else if (start + east in loop)
                            if (start + south in loop) 'F'
                            else '-'
                        else '7'
                    else if (position in loop) char
                    else '.'
                }
                    .joinToString(separator = "")
            }
            return PipeMaze(newLines)
        }

        val horizontalTransition = "\\||F-*J|L-*7".toRegex()
        fun Pair<Int, Int>.horizontalTransitions() =
            horizontalTransition.findAll(lines[first].substring(0, second)).count()

        val verticalTransition = "-|F\\|*J|7\\|*L".toRegex()
        fun Pair<Int, Int>.verticalTransitions() =
            lines.asSequence()
                .take(first)
                .map { it[second] }
                .joinToString(separator = "")
                .let { verticalTransition.findAll(it).count() }

        fun countInsideTiles() =
            rowRange.asSequence()
                .flatMap { rowIndex -> columnRange.asSequence().map { rowIndex to it } }
                .filter { it.tile() == '.' && it.horizontalTransitions() % 2 == 1 && it.verticalTransitions() % 2 == 1 }
                .count()
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
