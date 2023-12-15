fun main() {

    fun List<String>.transpose() =
        first().indices.map { i ->
            map { line -> line[i] }.joinToString(separator = "")
        }

    data class Beam(val row:Int, val column:Int, val rocks: Int)

    data class Beams(val beams: Set<Beam>) {
        val byRow by lazy { beams.groupBy { it.row } }
        val byColumns by lazy { beams.groupBy { it.column } }
    }

    val beamsRegex = "#".toRegex()
    fun parseLine(lineIndex: Int, line: String): List<Beam> {
        val matches = beamsRegex.findAll(line)
        return matches.map { match ->
            val beamPosition = match.range.last
            val lineAfterBeamPosition = line.substring(beamPosition + 1).substringBefore('#')
            val rocks = lineAfterBeamPosition.count { char -> char == 'O' }
            Beam(beamPosition , lineIndex, rocks)
        }
            .toList()
    }

    fun parseBeams(input: List<String>): List<Beam> {
        val beamRow = "#".repeat(input.first().length + 2)
        return ((listOf(beamRow) + input.map { "#$it#" }) + beamRow)
            .transpose().flatMapIndexed(::parseLine)
    }

    fun load(beam: Beam, panelHeight: Int): Int {
        val distanceFromSouthEdge = panelHeight - beam.row
        return (0..<beam.rocks).sumOf { distanceFromSouthEdge - 1 - it }
    }

    fun loadFacingEast(beam: Beam, panelHeight: Int): Int {
        val weight = panelHeight - beam.row
        return (weight) * beam.rocks
    }

    fun part1(input: List<String>): Int {
        val beams = parseBeams(input)
        return beams.sumOf { load(it, input.size + 1) }
    }

    fun Beams.tiltWest(): Beams {
        return beams.map { beam ->
            val nextInRow = byRow[beam.row]!!.asSequence()
                .map { it.column }
                .filter { it > beam.column }
                .minOrNull()
            if (nextInRow == null) beam
            else {
                val colRange = (beam.column + 1)..<nextInRow
                val beamsInColRange = colRange.asSequence().flatMap { byColumns[it] ?: emptyList() }
                val newRocks = beamsInColRange
                    .filter { it.row < beam.row }
                    .filter { it.row + it.rocks >= beam.row }
                    .count()
                beam.copy(rocks = newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    fun Beams.tiltSouth(): Beams {
        return beams.map { beam ->
            val nextInColumn = byColumns[beam.column]!!.asSequence()
                .map { it.row }
                .filter { it < beam.row }
                .maxOrNull()
            if (nextInColumn == null) beam
            else {
                val rowRange = (beam.row - 1) downTo (nextInColumn + 1)
                val beamsInRowRange = rowRange.asSequence().flatMap { byRow[it] ?: emptyList() }
                val newRocks = beamsInRowRange
                    .filter { it.column < beam.column }
                    .filter { it.column + it.rocks >= beam.column }
                    .count()
                beam.copy(rocks = newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    fun Beams.tiltEast(): Beams {
        return beams.map { beam ->
            val nextInRow = byRow[beam.row]!!.asSequence()
                .map { it.column }
                .filter { it < beam.column }
                .maxOrNull()
            if (nextInRow == null) beam
            else {
                val colRange = (beam.column - 1) downTo (nextInRow + 1)
                val beamsInColRange = colRange.asSequence().flatMap { byColumns[it] ?: emptyList() }
                val newRocks = beamsInColRange
                    .filter { it.row > beam.row }
                    .filter { it.row - it.rocks <= beam.row }
                    .count()
                beam.copy(rocks = newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    fun Beams.tiltNorth(): Beams {
        return beams.map { beam ->
            val nextInColumn = byColumns[beam.column]!!.asSequence()
                .map { it.row }
                .filter { it > beam.row }
                .minOrNull()
            if (nextInColumn == null) beam
            else {
                val rowRange = (beam.row + 1)..<nextInColumn
                val beamsInRowRange = rowRange.asSequence().flatMap { byRow[it] ?: emptyList() }
                val newRocks = beamsInRowRange
                    .filter { it.column > beam.column }
                    .filter { it.column - it.rocks <= beam.column }
                    .count()
                beam.copy(rocks = newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    val cache = mutableMapOf<Beams, Pair<Beams, Long>>()

    fun Beams.cycle(i: Long): Pair<Beams, Long> =
        cache.getOrPut(this) { tiltNorth().tiltWest().tiltSouth().tiltEast() to i }

    fun Beams.firstCycle() = tiltWest().tiltSouth().tiltEast()

    fun part2(input: List<String>): Int {
        val afterFirst = Beams(parseBeams(input).toSet()).firstCycle()

        val (index, cacheHit) = (2L..1_000_000_000L).asSequence().runningFold(afterFirst to null as Long?) { (beams, _), cycleCount ->
            val cycleResult = beams.cycle(cycleCount)
            val cacheHitOrigin = cycleResult.second.takeIf { it != cycleCount }
            cycleResult.first to cacheHitOrigin
        }
            .withIndex()
            .first { it.value.second != null }

        val currentCycle = index + 1
        val currentBeams = cacheHit.first
        val period = currentCycle - cacheHit.second!!
        val cyclesLeft = (1_000_000_000 - currentCycle) % period
        val finalBeams = (1..cyclesLeft).fold(currentBeams) { bms, _ -> bms.cycle(-1).first }
        return finalBeams.beams.sumOf { loadFacingEast(it, input.size + 1) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 136)
    check(part2(testInput) == 64)

    val input = readInput("Day14")
    part1(input).println()
    part2(input).println()
}
