fun main() {

    data class PartNumber(val value: Int, val row: Int, val columns: IntRange) {

        val adjacentPositions =
            buildSet {
                (-1..1).forEach {
                    add((row + it) to (columns.first - 1))
                    add((row + it) to (columns.last + 1))
                }
                columns.forEach {
                    add((row - 1) to it)
                    add((row + 1) to it)
                }
            }

        fun hasAdjacentSymbolIn(engineSchematic: List<String>) =
            adjacentPositions
                .filter { it.first in engineSchematic.indices && it.second in engineSchematic.first().indices }
                .map { engineSchematic[it.first][it.second] }
                .any { it != '.' && !it.isDigit() }
    }

    val intRegex = "\\d+".toRegex()
    fun findPartNumbers(engineSchematic: List<String>) =
        engineSchematic.flatMapIndexed { rowIndex, line ->
            intRegex.findAll(line)
                .map {
                    PartNumber(it.value.toInt(), rowIndex, it.range)
                }
        }

    fun part1(input: List<String>) =
        findPartNumbers(input)
            .filter { it.hasAdjacentSymbolIn(input) }
            .sumOf { it.value }

    fun findGearPositions(engineSchematic: List<String>) =
        engineSchematic.flatMapIndexed { rowIndex, line ->
            line.indices
                .filter { line[it] == '*' }
                .map { rowIndex to it }
        }

    fun part2(input: List<String>): Int {
        val gearPositions = findGearPositions(input)
        val partNumbers = findPartNumbers(input)
        val gearPositionsToAdjacentPartNumbers = gearPositions.associateWith { gearPosition ->
            partNumbers.filter { gearPosition in it.adjacentPositions }
        }
        return gearPositionsToAdjacentPartNumbers.values
            .filter { it.size == 2 }
            .sumOf { it[0].value * it[1].value }
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day03_test")) == 4361)
    check(part2(readInput("Day03_test")) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
