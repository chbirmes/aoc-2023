import kotlin.math.max
import kotlin.math.min

fun main() {

    fun parseInstructions(input: List<String>): Sequence<Char> {
        val instructionsLength = input.first().length
        return generateSequence(0) { it + 1 }
            .map { input.first()[it % instructionsLength] }
    }

    fun parseNodes(input: List<String>) =
        input.drop(2).associate { line ->
            line.dropLast(1).split(" = (", ", ")
                .let { it[0] to (it[1] to it[2]) }
        }

    fun part1(input: List<String>): Int {
        val instructions = parseInstructions(input)
        val nodes = parseNodes(input)
        val path = instructions.runningFold("AAA") { currentNode, instruction ->
            nodes[currentNode]!!.let { if (instruction == 'L') it.first else it.second }
        }
        return path.indexOfFirst { it == "ZZZ" }
    }

    fun leastCommonMultiple(x: Long, y: Long): Long {
        val greater = max(x, y)
        val lesser = min(x, y)
        return generateSequence(greater) { it + greater }
            .first { it % lesser == 0L }
    }

    fun part2(input: List<String>): Long {
        val instructions = parseInstructions(input)
        val nodes = parseNodes(input)
        val startingNodes = nodes.keys.filter { it.endsWith('A') }

        val pathsWithsLengths = startingNodes.associateWith { node ->
            instructions.runningFold(node) { currentNode, instruction ->
                nodes[currentNode]!!.let { if (instruction == 'L') it.first else it.second }
            }
                .mapIndexed() { index, n -> n to index }
                .first { it.first.endsWith('Z') }
        }

        // "end-to-end" lengths are the same

        val pathLengths = pathsWithsLengths.values.map { it.second }
        return pathLengths.drop(1).fold(pathLengths[0].toLong()) { x, y -> leastCommonMultiple(x, y.toLong()) }
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day08_test")) == 6)
    check(part2(readInput("Day08_test2")) == 6L)

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()

}
