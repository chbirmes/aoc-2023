fun main() {

    data class SpaceImage(val lines: List<String>, val expansionFactor: Long) {

        val emptyRowIndices = lines.indices.filterNot { lines[it].contains('#') }.toSet()

        val emptyColumnIndices = lines.first().indices
            .filter { columnIndex ->
                lines.map { it[columnIndex] }
                    .none { it == '#' }
            }
            .toSet()

        val galaxies: List<Pair<Int, Int>> by lazy {
            lines.flatMapIndexed { rowIndex, line ->
                line.mapIndexed { columnIndex, char ->
                    if (char == '#') rowIndex to columnIndex else null
                }
                    .filterNotNull()
            }
        }

        operator fun Pair<Int, Int>.compareTo(other: Pair<Int, Int>) =
            this.first.compareTo(other.first)
                .takeIf { it != 0 } ?: this.second.compareTo(other.second)

        fun galaxyPairs() =
            galaxies.asSequence()
                .flatMap { firstGalaxy ->
                    galaxies.asSequence()
                        .filter { firstGalaxy < it }
                        .map { firstGalaxy to it }
                }

        fun rangeBetween(a: Int, b: Int) = if (a <= b) a..b else b..a

        fun Pair<Pair<Int, Int>, Pair<Int, Int>>.distance(): Long {
            val rowRange = rangeBetween(first.first, second.first)
            val columnRange = rangeBetween(first.second, second.second)
            val expandedRows = emptyRowIndices.count { it in rowRange }
            val expandedColumns = emptyColumnIndices.count { it in columnRange }
            return (rowRange.last - rowRange.first) +
                    (columnRange.last - columnRange.first) +
                    (expandedRows + expandedColumns) * (expansionFactor - 1)
        }

        fun pairwiseDistanceSum() = galaxyPairs().sumOf { it.distance() }
    }


    fun part1(input: List<String>) = SpaceImage(input, 2).pairwiseDistanceSum()

    fun part2(input: List<String>, expansionFactor: Long) = SpaceImage(input, expansionFactor).pairwiseDistanceSum()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 374L)
    check(part2(testInput, 10) == 1030L)
    check(part2(testInput, 100) == 8410L)

    val input = readInput("Day11")
    part1(input).println()
    part2(input, 1_000_000).println()
}
