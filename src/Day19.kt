fun main() {

    data class Part(val x: Int, val m: Int, val a: Int, val s: Int) {
        fun rating() = x + m + a + s
    }

    data class Rule(val predicate: (Part) -> Boolean, val result: String)

    data class Workflow(val name: String, val rules: List<Rule>, val defaultResult: String) {
        fun process(part: Part) = rules.firstOrNull { it.predicate(part) }?.result ?: defaultResult
    }

    val intRegex = "\\d+".toRegex()
    fun parsePart(line: String) =
        intRegex.findAll(line)
            .map { it.value.toInt() }
            .toList()
            .let { Part(it[0], it[1], it[2], it[3]) }

    fun parseRule(s: String): Rule {
        val property = when (s[0]) {
            'x' -> Part::x
            'm' -> Part::m
            'a' -> Part::a
            's' -> Part::s
            else -> throw IllegalArgumentException()
        }
        val comparator = when (s[1]) {
            '<' -> Comparator.naturalOrder()
            '>' -> Comparator.naturalOrder<Int>().reversed()
            else -> throw IllegalArgumentException()
        }
        val value = s.substring(2).substringBefore(':').toInt()
        val result = s.substringAfter(':')
        val predicate = { part: Part -> comparator.compare(property.get(part), value) < 0 }
        return Rule(predicate, result)
    }

    fun parseWorkflow(line: String): Workflow {
        val split = line.split('{', ',')
        val rules = split.subList(1, split.indices.last).map { parseRule(it) }
        return Workflow(split[0], rules, split.last().dropLast(1))
    }

    fun Map<String, Workflow>.ultimateResult(part: Part) =
        generateSequence(get("in")!!.process(part)) { result -> get(result)?.process(part) ?: result }
            .first { it == "A" || it == "R" }

    fun part1(input: List<String>): Int {
        val workflows = input.takeWhile { it.isNotEmpty() }.map { parseWorkflow(it) }.associateBy { it.name }
        val parts = input.takeLastWhile { it.isNotEmpty() }.map { parsePart(it) }
        return parts.filter { workflows.ultimateResult(it) == "A" }
            .sumOf { it.rating() }
    }

    fun part2(input: List<String>): Long {
        TODO()
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day19_test")) == 19114)
//    check(part2(readInput("Day01_test2")) == 167409079868000)

    val input = readInput("Day19")
    part1(input).println()
//    part2(input).println()
}
