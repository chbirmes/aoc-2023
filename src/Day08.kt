import kotlin.math.max
import kotlin.math.min

fun main() {

    fun parseInstructions(line: String) = generateSequence(0) { it + 1 }.map { line[it % line.length] }

    fun parseNodes(lines: List<String>) =
        lines.associate { line ->
            line.dropLast(1)
                .split(" = (", ", ")
                .let { it[0] to (it[1] to it[2]) }
        }

    fun Map<String, Pair<String, String>>.nextNode(node: String, instruction: Char) =
        get(node)!!.let { if (instruction == 'L') it.first else it.second}

    fun part1(input: List<String>): Int {
        val instructions = parseInstructions(input.first())
        val nodes = parseNodes(input.drop(2))
        val path = instructions.runningFold("AAA", nodes::nextNode)
        return path.indexOfFirst { it == "ZZZ" }
    }

    fun leastCommonMultiple(x: Long, y: Long): Long {
        val greater = max(x, y)
        val lesser = min(x, y)
        return generateSequence(greater) { it + greater }
            .first { it % lesser == 0L }
    }

    fun part2(input: List<String>): Long {
        val instructions = parseInstructions(input.first())
        val nodes = parseNodes(input.drop(2))
        val startingNodes = nodes.keys.filter { it.endsWith('A') }

        val pathsWithsLengths = startingNodes.associateWith { node ->
            instructions.runningFold(node, nodes::nextNode)
                .mapIndexed() { index, n -> n to index }
                .first { it.first.endsWith('Z') }
        }

        // "end-to-end" lengths are the same

        val pathLengths = pathsWithsLengths.values.map { it.second }
        return pathLengths.fold(1L) { x, y -> leastCommonMultiple(x, y.toLong()) }
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day08_test")) == 6)
    check(part2(readInput("Day08_test2")) == 6L)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()

}
