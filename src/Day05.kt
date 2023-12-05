fun main() {

    fun LongRange.intersect(other: LongRange): LongRange {
        val thisFirstLess = this.first < other.first
        val min = if (thisFirstLess) this else other
        val max = if (thisFirstLess) other else this
        return if (max.first > min.last) {
            LongRange.EMPTY
        } else if (max.first == min.first) {
            min.first..kotlin.math.min(min.last, max.last)
        } else {
            max.first..kotlin.math.min(min.last, max.last)
        }
    }

    fun LongRange.shiftBy(offset: Long) = (start + offset)..(endInclusive + offset)

    data class SectionMapping(val sourceRange: LongRange, val destinationRangeStart: Long) {
        val offset = destinationRangeStart - sourceRange.first
        fun map(source: Long) =
            if (source in sourceRange)
                source + offset
            else
                throw IllegalArgumentException("source $source not in range $sourceRange")

        fun mapRange(range: LongRange): Pair<LongRange, List<LongRange>> {
            val intersection = range.intersect(sourceRange).shiftBy(offset)
            val unmapped =
                if (intersection.isEmpty())
                    listOf(range)
                else
                    buildList {
                        if (range.first < sourceRange.first) {
                            add(range.first..<sourceRange.first)
                        }
                        if (sourceRange.last < range.last) {
                            add((sourceRange.last + 1)..range.last)
                        }
                    }
            return intersection to unmapped
        }
    }

    fun parseSectionMapping(line: String) =
        line.split(' ')
            .map { it.toLong() }
            .let { SectionMapping(it[1]..<(it[1] + it[2]), it[0]) }

    data class CategoryMapping(val sectionMappings: List<SectionMapping>) {

        fun map(source: Long) =
            sectionMappings.find { source in it.sourceRange }
                ?.map(source)
                ?: source

        fun mapRange(range: LongRange) =
            sectionMappings.fold(listOf<LongRange>() to listOf(range)) { (mapped, unmapped), mapping ->
                val pairs = unmapped.map { mapping.mapRange(it) }
                val newMapped = mapped + pairs.map { it.first }.filterNot { it.isEmpty() }
                val newUnmapped = pairs.flatMap { it.second }
                newMapped to newUnmapped
            }
                .let { it.first + it.second }
    }

    fun parseCategoryMappings(lines: List<String>): List<CategoryMapping> {
        val nonEmptyLines = lines.filter { it.isNotEmpty() }
        val titleIndices = nonEmptyLines.indices
            .filter { nonEmptyLines[it].first().isLetter() }
        return (titleIndices + nonEmptyLines.size).windowed(2)
            .map {
                nonEmptyLines.subList(it[0] + 1, it[1])
                    .map { line -> parseSectionMapping(line) }
                    .sortedBy { sectionMapping -> sectionMapping.sourceRange.first }
            }
            .map { CategoryMapping(it) }
    }

    fun part1(input: List<String>): Long {
        val mappingList = parseCategoryMappings(input.drop(2))
        val seeds = input.first()
            .substringAfter(": ")
            .split(' ')
            .map { it.toLong() }

        val locations = seeds.map { seed ->
            mappingList.fold(seed) { position, mapping -> mapping.map(position) }
        }
        return locations.min()

    }

    fun part2(input: List<String>): Long {
        val mappingList = parseCategoryMappings(input.drop(2))
        val seedRanges = input.first()
            .substringAfter(": ")
            .split(' ')
            .chunked(2) { it[0].toLong()..<(it[0].toLong() + it[1].toLong()) }

        val locationRanges = seedRanges.flatMap { seedRange ->
            mappingList.fold(listOf(seedRange)) { ranges, mapping -> ranges.flatMap { mapping.mapRange(it) } }
        }

        return locationRanges.minOf { it.first }
    }

// test if implementation meets criteria from the description, like:
    check(part1(readInput("Day05_test")) == 35L)
    check(part2(readInput("Day05_test")) == 46L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}
