import kotlin.math.min

fun main() {

    fun List<String>.transpose() =
        first().indices.map { i ->
            map { line -> line[i] }.joinToString(separator = "")
        }

    fun hasReflectionAfterIndex(pattern: List<String>, index: Int): Boolean {
        val rowsToCheck = min(index + 1, pattern.size - index - 1)
        return (0..<rowsToCheck).all { rtc ->
            pattern[index - rtc] == pattern[index + rtc + 1]
        }
    }

    fun differences(a: String, b: String) = a.indices.count { a[it] != b[it] }

    fun hasReflectionAfterIndexWithSmudge(pattern: List<String>, index: Int): Boolean {
        val rowsToCheck = min(index + 1, pattern.size - index - 1)
        val sumOfDifferences = (0..<rowsToCheck).sumOf { rtc ->
            differences(pattern[index - rtc], pattern[index + rtc + 1])
        }
        return sumOfDifferences == 1
    }

    fun rowsAboveReflection(pattern: List<String>, checker: (List<String>, Int) -> Boolean): Int =
        (0..<pattern.size - 1).firstOrNull { checker(pattern, it) }?.plus(1) ?: 0

    fun columnsLeftOfReflection(pattern: List<String>, checker: (List<String>, Int) -> Boolean): Int =
        rowsAboveReflection(pattern.transpose(), checker)

    fun splitByEmptyLines(input: List<String>): List<List<String>> =
        input.fold(listOf(emptyList())) { listOfLists, line ->
            if (line.isEmpty()) listOfLists.plusElement(emptyList())
            else listOfLists.dropLast(1).plusElement(listOfLists.last() + line)
        }

    fun part1(input: List<String>): Int {
        val checker = ::hasReflectionAfterIndex
        return splitByEmptyLines(input)
            .sumOf { 100 * rowsAboveReflection(it, checker) + columnsLeftOfReflection(it, checker) }
    }

    fun part2(input: List<String>): Int {
        val checker = ::hasReflectionAfterIndexWithSmudge
        return splitByEmptyLines(input)
            .sumOf { 100 * rowsAboveReflection(it, checker) + columnsLeftOfReflection(it, checker) }

    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 405)
    check(part2(testInput) == 400)

    val input = readInput("Day13")
    part1(input).println()
    part2(input).println()
}
