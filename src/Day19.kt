fun main() {

    data class Part(val x: Int, val m: Int, val a: Int, val s: Int) {
        fun rating() = x + m + a + s

        operator fun get(char: Char) =
            when (char) {
                'x' -> x
                'm' -> m
                'a' -> a
                's' -> s
                else -> throw IllegalArgumentException()
            }
    }

    data class PartGroup(val x: IntRange, val m: IntRange, val a: IntRange, val s: IntRange) {

        fun IntRange.split(comparator: Char, value: Int): Pair<IntRange, IntRange> =
            if (comparator == '<') {
                if (value < first) IntRange.EMPTY to this
                else if (last < value) this to IntRange.EMPTY
                else first..<value to value..last
            } else {
                if (value > last) IntRange.EMPTY to this
                else if (first > value) this to IntRange.EMPTY
                else (value + 1)..last to first..value
            }

        fun IntRange.size() = last - first + 1

        fun split(property: Char, comparator: Char, value: Int) =
            when (property) {
                'x' -> x.split(comparator, value)
                    .let { (matched, unmatched) -> copy(x = matched) to copy(x = unmatched) }

                'm' -> m.split(comparator, value)
                    .let { (matched, unmatched) -> copy(m = matched) to copy(m = unmatched) }

                'a' -> a.split(comparator, value)
                    .let { (matched, unmatched) -> copy(a = matched) to copy(a = unmatched) }

                's' -> s.split(comparator, value)
                    .let { (matched, unmatched) -> copy(s = matched) to copy(s = unmatched) }

                else -> throw IllegalArgumentException()
            }

        fun isEmpty() = sequenceOf(x, m, a, s).any { it.isEmpty() }

        fun size() = x.size().toLong() * m.size().toLong() * a.size().toLong() * s.size().toLong()

    }

    data class Rule(val property: Char, val comparator: Char, val value: Int, val result: String) {

        fun evaluate(part: Part): Boolean =
            if (comparator == '<') part[property] < value
            else part[property] > value

        fun evaluate(partGroup: PartGroup) = partGroup.split(property, comparator, value)
    }

    data class Workflow(val name: String, val rules: List<Rule>, val defaultResult: String) {
        fun process(part: Part) = rules.firstOrNull { it.evaluate(part) }?.result ?: defaultResult

        fun evaluate(partGroup: PartGroup): List<Pair<PartGroup, String>> {
            val start: Pair<List<PartGroup>, List<Pair<PartGroup, String>>> = listOf(partGroup) to emptyList()
            val (unfinished, finished) = rules.fold(start) { (ongoing, done), rule ->
                val evaluated = ongoing.map { rule.evaluate(it) }
                val matched = evaluated
                    .map { it.first }
                    .filterNot { it.isEmpty() }
                    .map { it to rule.result }
                val unmatched = evaluated
                    .map { it.second }
                    .filterNot { it.isEmpty() }
                unmatched to (done + matched)
            }
            return finished + unfinished.map { it to defaultResult }
        }
    }

    val intRegex = "\\d+".toRegex()
    fun parsePart(line: String) =
        intRegex.findAll(line)
            .map { it.value.toInt() }
            .toList()
            .let { Part(it[0], it[1], it[2], it[3]) }

    fun parseRule(s: String): Rule {
        val value = s.substring(2).substringBefore(':').toInt()
        val result = s.substringAfter(':')
        return Rule(s[0], s[1], value, result)
    }

    fun parseWorkflow(line: String): Workflow {
        val split = line.split('{', ',')
        val rules = split.subList(1, split.indices.last).map { parseRule(it) }
        return Workflow(split[0], rules, split.last().dropLast(1))
    }

    fun Map<String, Workflow>.ultimateResult(part: Part) =
        generateSequence(get("in")!!.process(part)) { result -> get(result)?.process(part) ?: result }
            .first { it == "A" || it == "R" }

    fun Map<String, Workflow>.acceptedCombinations(partGroup: PartGroup): Long {
        val start = get("in")!!.evaluate(partGroup)
        return generateSequence(start) { previous ->
            previous
                .flatMap { (group, result) -> get(result)?.evaluate(group) ?: listOf(group to result) }
                .filter { it.second != "R" }
        }
            .first { it.all { (_, result) -> result == "A" } }
            .sumOf { it.first.size() }
    }

    fun part1(input: List<String>): Int {
        val workflows = input.takeWhile { it.isNotEmpty() }.map { parseWorkflow(it) }.associateBy { it.name }
        val parts = input.takeLastWhile { it.isNotEmpty() }.map { parsePart(it) }
        return parts.filter { workflows.ultimateResult(it) == "A" }
            .sumOf { it.rating() }
    }

    fun part2(input: List<String>): Long {
        val workflows = input.takeWhile { it.isNotEmpty() }.map { parseWorkflow(it) }.associateBy { it.name }
        val ratingRange = 1..4000
        return workflows.acceptedCombinations(PartGroup(ratingRange, ratingRange, ratingRange, ratingRange))
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day19_test")) == 19114)
    check(part2(readInput("Day19_test")) == 167409079868000)

    val input = readInput("Day19")
    part1(input).println()
    part2(input).println()
}
