fun main() {

    fun List<String>.transpose() =
        first().indices.map { i ->
            map { line -> line[i] }.joinToString(separator = "")
        }

    data class Beam(val position: Pair<Int, Int>, val rocks: Int)

    data class Beams(val beams: Set<Beam>) {
        val byRow by lazy { beams.groupBy { it.position.first } }
        val byColumns by lazy { beams.groupBy { it.position.second } }
    }

    val beamsRegex = "#".toRegex()
    fun parseLine(lineIndex: Int, line: String): List<Beam> {
        val matches = beamsRegex.findAll(line)
        return matches.map { match ->
            val beamPosition = match.range.last
            val lineAfterBeamPosition = line.substring(beamPosition + 1).substringBefore('#')
            val rocks = lineAfterBeamPosition.count { char -> char == 'O' }
            Beam(beamPosition to lineIndex, rocks)
        }
            .toList()
    }

    fun parseBeams(input: List<String>): List<Beam> {
        val beamRow = "#".repeat(input.first().length + 2)
        return ((listOf(beamRow) + input.map { "#$it#" }) + beamRow)
            .transpose().flatMapIndexed(::parseLine)
    }

    fun load(beam: Beam, panelHeight: Int): Int {
        val distanceFromSouthEdge = panelHeight - beam.position.first
        return (0..<beam.rocks).sumOf { distanceFromSouthEdge - 1 - it }
    }

    fun loadFacingEast(beam: Beam, panelHeight: Int): Int {
        val weight = panelHeight - beam.position.first
        return (weight) * beam.rocks
    }

    fun part1(input: List<String>): Int {
        val beams = parseBeams(input)
        return beams.sumOf { load(it, input.size + 1) }
    }

    fun Beams.tiltWest(): Beams {
        return beams.map { beam ->
            val nextInRow = byRow[beam.position.first]!!.asSequence()
                .map { it.position.second }
                .filter { it > beam.position.second }
                .minOrNull()
            if (nextInRow == null) beam
            else {
                val colRange = (beam.position.second + 1)..<nextInRow
                val beamsInColRange = colRange.asSequence().flatMap { byColumns[it] ?: emptyList() }
                val newRocks = beamsInColRange
                    .filter { it.position.first < beam.position.first }
                    .filter { it.position.first + it.rocks >= beam.position.first }
                    .count()
                Beam(beam.position, newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    fun Beams.tiltSouth(): Beams {
        return beams.map { beam ->
            val nextInColumn = byColumns[beam.position.second]!!.asSequence()
                .map { it.position.first }
                .filter { it < beam.position.first }
                .maxOrNull()
            if (nextInColumn == null) beam
            else {
                val rowRange = (beam.position.first - 1) downTo (nextInColumn + 1)
                val beamsInRowRange = rowRange.asSequence().flatMap { byRow[it] ?: emptyList() }
                val newRocks = beamsInRowRange
                    .filter { it.position.second < beam.position.second }
                    .filter { it.position.second + it.rocks >= beam.position.second }
                    .count()
                Beam(beam.position, newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    fun Beams.tiltEast(): Beams {
        return beams.map { beam ->
            val nextInRow = byRow[beam.position.first]!!.asSequence()
                .map { it.position.second }
                .filter { it < beam.position.second }
                .maxOrNull()
            if (nextInRow == null) beam
            else {
                val colRange = (beam.position.second - 1) downTo (nextInRow + 1)
                val beamsInColRange = colRange.asSequence().flatMap { byColumns[it] ?: emptyList() }
                val newRocks = beamsInColRange
                    .filter { it.position.first > beam.position.first }
                    .filter { it.position.first - it.rocks <= beam.position.first }
                    .count()
                Beam(beam.position, newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    fun Beams.tiltNorth(): Beams {
        return beams.map { beam ->
            val nextInColumn = byColumns[beam.position.second]!!.asSequence()
                .map { it.position.first }
                .filter { it > beam.position.first }
                .minOrNull()
            if (nextInColumn == null) beam
            else {
                val rowRange = (beam.position.first + 1)..<nextInColumn
                val beamsInRowRange = rowRange.asSequence().flatMap { byRow[it] ?: emptyList() }
                val newRocks = beamsInRowRange
                    .filter { it.position.second > beam.position.second }
                    .filter { it.position.second - it.rocks <= beam.position.second }
                    .count()
                Beam(beam.position, newRocks)
            }
        }.toSet().let { Beams(it) }
    }

    val cache = mutableMapOf<Beams, Pair<Beams, Long>>()

    fun Beams.cycle(i: Long): Pair<Beams, Long> =
        cache.getOrPut(this) { tiltNorth().tiltWest().tiltSouth().tiltEast() to i }

    fun Beams.firstCycle() = tiltWest().tiltSouth().tiltEast()

    fun part2(input: List<String>): Int {
        val afterFirst = Beams(parseBeams(input).toSet()).firstCycle()

        val firstCached =
            (2L..1_000_000_000L).asSequence().runningFold(afterFirst to null as Long?) { (beams, _), cycleCount ->
                val cycleResult = beams.cycle(cycleCount)
                val cacheHit = cycleResult.second.takeIf { it != cycleCount }
                cycleResult.first to cacheHit
            }
                .withIndex()
                .first { it.value.second != null }

        val currentCycle = firstCached.index + 1
        val currentBeams = firstCached.value.first
        val period = currentCycle - firstCached.value.second!!
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
