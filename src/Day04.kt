import kotlin.math.min

fun main() {

    data class Scratchcard(val winningNumbers: Set<Int>, val myNumbers: Set<Int>) {

        fun pointValue() = matchCount().let {
            if (it == 0) {
                0
            } else {
                1 shl (it - 1)
            }
        }

        fun matchCount() = winningNumbers.intersect(myNumbers).size
    }

    val whitespaceRegex = "\\s+".toRegex()
    fun parseInts(s: String) = s.trim().split(whitespaceRegex).map { it.toInt() }.toSet()
    fun parseScratchcard(line: String) =
        line.split(':', '|').let { parts ->
            Scratchcard(parseInts(parts[1]), parseInts(parts[2]))
        }

    fun part1(input: List<String>) =
        input.map { parseScratchcard(it) }.sumOf { it.pointValue() }

    fun List<Pair<Scratchcard, Int>>.scoreLine(index: Int): List<Pair<Scratchcard, Int>> {
        val original = this
        val matchCount = get(index).first.matchCount()
        val copies = get(index).second
        return buildList {
            addAll(original.take(index + 1))
            val fromIndex = index + 1
            val toIndex = min(fromIndex + matchCount, original.size)
            val cardsWithExtraCopies = original.subList(fromIndex, toIndex)
                .map { it.first to it.second + copies }
            addAll(cardsWithExtraCopies)
            addAll(original.drop(toIndex))
        }
    }

    fun part2(input: List<String>): Int {
        val scratchcardPile = input.map { parseScratchcard(it) to 1 }
        val finalPile = scratchcardPile.foldIndexed(scratchcardPile) { index, pile, _ -> pile.scoreLine(index) }
        return finalPile.sumOf { it.second }
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day04_test")) == 13)
    check(part2(readInput("Day04_test2")) == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
