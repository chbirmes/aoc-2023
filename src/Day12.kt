fun main() {

    fun takenCharsFromArrangements(charGroup: String, count: Int): List<Int> {
        val spareSpace = charGroup.length - count
        return if (spareSpace < 0) emptyList()
        else if (spareSpace == 0) listOf(charGroup.length)
        else {
            (0..spareSpace).map { i ->
                ".".repeat(i) + "#".repeat(count) + ".".repeat((spareSpace - i).coerceIn(0, 1))
            }
                .mapIndexed { index, mask ->
                    val matches = mask.indices.all { charGroup[it] == mask[it] || charGroup[it] == '?' }
                    if (matches) index else null
                }
                .filterNotNull()
                .map { (it + count + 1).coerceAtMost(charGroup.length) }
        }
    }

    fun numberOfArrangements(charGroup: String, count: Int, lastGroup: Boolean = false): Long {
        return takenCharsFromArrangements(charGroup, count)
            .filter { !lastGroup || !charGroup.substring(it).contains('#') }
            .size.toLong()
    }

    val cache = mutableMapOf<Pair<List<String>, List<Int>>, Long>()

    fun solve(charGroups: List<String>, groupCounts: List<Int>): Long {
        val cacheKey = charGroups to groupCounts
        return cache.getOrPut(cacheKey) {
            if (charGroups.isEmpty() && groupCounts.isNotEmpty()) 0
            else if (groupCounts.size == 1) {
                val groupsWithHashes = charGroups.filter { it.contains('#') }
                if (groupsWithHashes.size > 1) {
                    0
                } else if (groupsWithHashes.size == 1) {
                    numberOfArrangements(groupsWithHashes[0], groupCounts[0], true)
                } else {
                    charGroups.sumOf { numberOfArrangements(it, groupCounts[0]) }
                }
            } else {
                val nextCharGroup = charGroups[0]
                val takenChars: List<Int> = takenCharsFromArrangements(nextCharGroup, groupCounts[0])
                val onlyQuestionMarks = !nextCharGroup.contains('#')
                takenChars.sumOf { taken ->
                    if (taken == nextCharGroup.length) {
                        solve(charGroups.drop(1), groupCounts.drop(1))
                    } else {
                        val newCharGroups = listOf(nextCharGroup.substring(taken)) + charGroups.drop(1)
                        solve(newCharGroups, groupCounts.drop(1))
                    }
                } + (if (onlyQuestionMarks) solve(charGroups.drop(1), groupCounts) else 0)
            }
        }
    }

    val dots = "\\.+".toRegex()
    fun parseRow(line: String) =
        line.split(' ').let { parts ->
            parts[0].split(dots) to parts[1].split(',').map { it.toInt() }
        }

    fun part1(input: List<String>): Long {
        return input.map { parseRow(it) }.sumOf {
            cache.clear()
            solve(it.first, it.second)
        }
    }

    fun quintuple(line: String) =
        line.split(' ').let { parts ->
            listOf(
                List(5) { parts[0] }.joinToString(separator = "?"),
                List(5) { parts[1] }.joinToString(separator = ",")
            )
        }
            .joinToString(separator = " ")

    fun part2(input: List<String>): Long {
        return input
            .map { quintuple(it) }
            .map { parseRow(it) }
            .sumOf {
                cache.clear()
                solve(it.first, it.second)
            }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 21L)
    check(part2(testInput) == 525152L)

    val input = readInput("Day12")
    part1(input).println()
    part2(input).println()
}
