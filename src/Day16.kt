fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1

    val vertical = setOf(north, south)
    val horizontal = setOf(east, west)

    val symbolRedirections: Map<Char, (Pair<Int, Int>) -> Set<Pair<Int, Int>>> = mapOf(
        '.' to { setOf(it) },
        '|' to { if (it in vertical) setOf(it) else vertical },
        '-' to { if (it in horizontal) setOf(it) else horizontal },
        '/' to {
            setOf(
                when (it) {
                    north -> east
                    east -> north
                    south -> west
                    west -> south
                    else -> throw IllegalArgumentException()
                }
            )
        },
        '\\' to {
            setOf(
                when (it) {
                    north -> west
                    west -> north
                    south -> east
                    east -> south
                    else -> throw IllegalArgumentException()
                }
            )
        }
    )

    class MirrorRoom(val lines: List<String>) {

        fun countEnergized(startPosition: Pair<Int, Int> = 0 to 0, startDirection: Pair<Int, Int> = east): Int =
            visit(startPosition, startDirection, mutableSetOf())
                .map { it.first }
                .toSet()
                .count()

        fun countMaxEnergized(): Int {
            val columnIndices = lines.first().indices
            val starts = lines.indices.map { (it to 0) to east } +
                    lines.indices.map { (it to columnIndices.last) to west } +
                    columnIndices.map { (0 to it) to south } +
                    columnIndices.map { (lines.indices.last to it) to north }
            return starts.maxOf { countEnergized(it.first, it.second) }
        }

        operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)

        fun visit(
            position: Pair<Int, Int>,
            direction: Pair<Int, Int>,
            visited: MutableSet<Pair<Pair<Int, Int>, Pair<Int, Int>>>
        ): Set<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
            val current = (position to direction)
            return if ((current in visited)) visited
            else {
                val nextPositionsToDirections = nextPositionsToDirections(position, direction)
                visited.add(current)
                if (nextPositionsToDirections.isEmpty()) visited
                else nextPositionsToDirections.flatMap { visit(it.first, it.second, visited) }.toSet()
            }
        }

        fun nextPositionsToDirections(
            position: Pair<Int, Int>,
            direction: Pair<Int, Int>
        ): Set<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
            val symbol = lines[position.first][position.second]
            val nextDirections = symbolRedirections[symbol]!!(direction)
            val nextPositionsToDirection = nextDirections.associateBy { position + it }
            return nextPositionsToDirection
                .filterKeys { inRange(it) }
                .entries.map { (k, v) -> k to v }.toSet()
        }

        fun inRange(position: Pair<Int, Int>): Boolean =
            position.first in lines.indices && position.second in lines.first().indices

    }

    fun part1(input: List<String>): Int = MirrorRoom(input).countEnergized()

    fun part2(input: List<String>): Int = MirrorRoom(input).countMaxEnergized()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 46)
    check(part2(testInput) == 51)

    val input = readInput("Day16")
    part1(input).println()
    part2(input).println()
}
