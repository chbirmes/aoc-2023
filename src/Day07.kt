fun main() {

    val cardLabels = "AKQJT98765432"
    val cardLabelsWithJoker = "AKQT98765432J"

    val typePredicates = listOf<(Collection<Int>, Int) -> Boolean>(
        { counts, jokers -> (counts.maxOrNull() ?: 0) + jokers == 5 },
        { counts, jokers -> counts.max() + jokers == 4 },
        { counts, jokers -> counts.sorted().takeLast(2).sum() + jokers == 5 },
        { counts, jokers -> counts.max() + jokers == 3 },
        { counts, jokers -> counts.sorted().takeLast(2).sum() + jokers == 4 },
        { counts, jokers -> counts.contains(2) || jokers == 1 },
        { _, _ -> true },
    )

    data class HandWithBid(val hand: String, val bid: Int) {

        val counts = hand.groupingBy { it }.eachCount()

        val typeValue: Int by lazy {
            typePredicates.indexOfFirst { predicate ->
                predicate(counts.values, 0)
            }
        }

        val typeValueWithJokers: Int by lazy {
            typePredicates.indexOfFirst { predicate ->
                predicate(counts.filterKeys { it != 'J' }.values, counts['J'] ?: 0)
            }
        }
    }

    fun buildComparator(labels: String, typeValueFunction: (HandWithBid) -> Int) =
        Comparator.comparingInt(typeValueFunction)
            .thenComparing(
                { handWithBid -> handWithBid.hand.asSequence().map { labels.indexOf(it) } },
                { indexSequence1, indexSequence2 ->
                    indexSequence1.zip(indexSequence2, Int::compareTo)
                        .firstOrNull { it != 0 } ?: 0
                }
            )

    val comparator = buildComparator(cardLabels) { it.typeValue }
    val comparatorWithJokers = buildComparator(cardLabelsWithJoker) { it.typeValueWithJokers }

    fun parseHandWithBid(line: String) =
        line.split(' ', limit = 2)
            .let { HandWithBid(it[0], it[1].trim().toInt()) }

    fun List<HandWithBid>.totalWinnings(comparator: Comparator<HandWithBid>) =
        sortedWith(comparator)
            .asSequence()
            .mapIndexed { index, handWithBid -> (index + 1) * handWithBid.bid }
            .sum()

    fun part1(input: List<String>) =
        input.map { parseHandWithBid(it) }
            .totalWinnings(comparator.reversed())

    fun part2(input: List<String>) =
        input.map { parseHandWithBid(it) }
            .totalWinnings(comparatorWithJokers.reversed())

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 6440)
    check(part2(testInput) == 5905)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}
