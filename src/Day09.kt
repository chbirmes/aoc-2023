fun main() {

    fun derivative(series: List<Int>) = series.windowed(2).map { it[1] - it[0] }

    fun nextValue(series: List<Int>): Int =
        if (series.all { it == 0 })
            0
        else
            series.last() + nextValue(derivative(series))

    fun previousValue(series: List<Int>): Int =
        if (series.all { it == 0 })
            0
        else
            series.first() - previousValue(derivative(series))

    fun parseSeries(line: String) = line.split(' ').map { it.toInt() }

    fun part1(input: List<String>) =
        input.map { parseSeries(it) }.sumOf { nextValue(it) }

    fun part2(input: List<String>) =
        input.map { parseSeries(it) }.sumOf { previousValue(it) }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 114)
    check(part2(testInput) == 2)

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}
