fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1
    val allDirections = setOf(north, east, south, west)

    operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)

    data class PositionInInfiniteFarm(val tileRow: Int, val tileColumn: Int, val farmRow: Int, val farmColumn: Int) {
        operator fun plus(direction: Pair<Int, Int>) = (tileRow to tileColumn) + direction
    }

    class Farm(val tiles: List<String>) {

        fun Pair<Int, Int>.isInBounds() = first in tiles.indices && second in tiles.first().indices

        fun Pair<Int, Int>.tile() = tiles[first][second]

        val start = tiles.mapIndexed { rowIndex, line -> rowIndex to line.indexOf('S') }.single { it.second != -1 }

        fun Pair<Int, Int>.validNeighbors() =
            allDirections.asSequence()
                .map { this + it }
                .filter { it.isInBounds() }
                .filter { it.tile() != '#' }
                .toSet()

        fun reachableGardenTiles(steps: Int): Set<Pair<Int, Int>> {
            return (1..steps).fold(setOf(start)) { reached, _ ->
                reached.flatMap { it.validNeighbors() }.toSet()
            }
        }

        fun PositionInInfiniteFarm.validNeighbors() =
            allDirections.asSequence()
                .map { this + it }
                .map { position ->
                    val newTileRow = (position.first + tiles.size) % tiles.size
                    val newTileColumn = (position.second + tiles.first().length) % tiles.first().length
                    val newFarmRow =
                        if (position.first < 0) farmRow - 1
                        else if (position.first > tiles.indices.last) farmRow + 1
                        else farmRow
                    val newFarmColumn =
                        if (position.second < 0) farmColumn - 1
                        else if (position.second > tiles.first().indices.last) farmColumn + 1
                        else farmColumn
                    PositionInInfiniteFarm(newTileRow, newTileColumn, newFarmRow, newFarmColumn)
                }
                .filter { (it.tileRow to it.tileColumn).tile() != '#' }
                .toSet()

        fun reachableGardenTilesInInfiniteFarm(steps: Int): Set<PositionInInfiniteFarm> {
            val startInInfiniteFarm = PositionInInfiniteFarm(start.first, start.second, 0, 0)
            return (1..steps).fold(setOf(startInInfiniteFarm)) { reached, _ ->
                reached.flatMap { it.validNeighbors() }.toSet()
            }
        }

    }

    fun part1(input: List<String>, steps: Int): Int = Farm(input).reachableGardenTiles(steps).size

    fun part2(input: List<String>): Long {

        val farm = Farm(input)
        assert(input.size == input.first().length)
        assert(input.size % 2 == 1)
        assert(farm.start.let { it.first == it.second })
        val center = farm.start.first
        assert(center == (input.size - 1) / 2)
        (1..center).forEach {
            assert(input[center - it][center] == '.')
            assert(input[center + it][center] == '.')
            assert(input[center][center - it] == '.')
            assert(input[center][center + it] == '.')
        }
        assert(input.first().all { it == '.' })
        assert(input.last().all { it == '.' })
        assert(input.map { it.first() }.all { it == '.' })
        assert(input.map { it.last() }.all { it == '.' })

        val steps = 26501365

        assert((steps - center) % input.size == 0)
        val repetitions = ((steps - center) / input.size).toLong()
        assert(repetitions % 2 == 0L)

        val groups = farm.reachableGardenTilesInInfiniteFarm(
            input.size * 3 + center
        )
            .groupBy { it.farmRow to it.farmColumn }

        return repetitions * repetitions * groups[0 to 0]!!.size +
                (repetitions - 1) * (repetitions - 1) * groups[0 to 1]!!.size +
                repetitions * groups[-3 to -1]!!.size +
                repetitions * groups[3 to -1]!!.size +
                repetitions * groups[3 to 1]!!.size +
                repetitions * groups[-3 to 1]!!.size +
                (repetitions - 1) * groups[-2 to -1]!!.size +
                (repetitions - 1) * groups[2 to -1]!!.size +
                (repetitions - 1) * groups[2 to 1]!!.size +
                (repetitions - 1) * groups[-2 to 1]!!.size +
                groups[-3 to 0]!!.size +
                groups[0 to -3]!!.size +
                groups[3 to 0]!!.size +
                groups[0 to 3]!!.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test")
    check(part1(testInput, 6) == 16)

    val input = readInput("Day21")
    part1(input, 64).println()
    part2(input).println()
}
