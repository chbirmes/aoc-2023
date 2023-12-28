fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1
    val allDirections = setOf(north, east, south, west)

    operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)
    operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = (first - other.first) to (second - other.second)
    operator fun Pair<Int, Int>.unaryMinus() = -first to -second

    val slopes = mapOf(
        '^' to north,
        '>' to east,
        'v' to south,
        '<' to west
    )

    data class Vertex(val position: Pair<Int, Int>, val exits: Set<Pair<Int, Int>>)

    data class Edge(val start: Pair<Int, Int>, val end: Pair<Int, Int>, val cost: Int)

    fun List<String>.charAt(position: Pair<Int, Int>) = this[position.first][position.second]

    fun List<String>.validNeighbors(position: Pair<Int, Int>) =
        allDirections.asSequence()
            .map { position + it }
            .filter { it.first in indices && it.second in first().indices }
            .filterNot { charAt(it) == '#' }

    fun parseVertices(input: List<String>): List<Vertex> {
        val start = (0 to input.first().indexOf('.')).let { Vertex(it, setOf(it + south)) }
        val end = (input.indices.last to input.last().indexOf('.')).let { Vertex(it, setOf(it+north)) }
        val columnIndices = 1..<input.first().indices.last
        val innerVertices = input.subList(1, input.indices.last).flatMapIndexed { lineIndex, line ->
            line.substring(columnIndices).withIndex()
                .filterNot { it.value == '#' }
                .map {
                    val position = lineIndex + 1 to it.index + 1
                    val exits = input.validNeighbors(position).toSet()
                    Vertex(position, exits)
                }
                .filter { it.exits.size > 2 }
        }
        return listOf(start, end) + innerVertices
    }

    fun followPath(input: List<String>, start: Pair<Int, Int>, next: Pair<Int, Int>): Edge? {
        val startDirection = next - start
        val (index, positionToDirection) = generateSequence(next to startDirection) { (current, direction) ->
            val char = input.charAt(current)
            val nextDirection =
                slopes[char]?.let { setOf(it) } ?: input.validNeighbors(current).map { it - current }.toSet()
            nextDirection.singleOrNull { it != -direction }?.let { current + it to it }
        }
            .withIndex().last()
        val position = positionToDirection.first
        val oppositeSlope = slopes[input.charAt(position)]?.takeIf { it == -positionToDirection.second }
        val invalidNeighbors = if (oppositeSlope != null) 1 else 0
        val neighbors = input.validNeighbors(position).count() - invalidNeighbors
        return if (neighbors < 2 && position.first != 0 && position.first != input.indices.last) null
        else Edge(start, position, index+1)
    }

    fun parseEdges(input: List<String>, vertices: List<Vertex>): List<Edge> {
        return vertices.flatMap { vertex ->
            vertex.exits.mapNotNull { followPath(input, vertex.position, it) }
        }
    }

    class Graph(edges: List<Edge>) {
        val vertices = edges.flatMap { listOf(it.start, it.end) }.toSet()
        val outgoingEdges = edges.groupBy { it.start }
        val start = vertices.single { it.first == 0 }
        val finish = vertices.maxBy { it.first }

        fun longestPathToFinish(current:Edge = outgoingEdges[start]!!.first(), visited:Set<Pair<Int,Int>> = setOf(current.start, current.end)): Int {
            return if (current.end == finish) current.cost
            else {
                val validNext = outgoingEdges[current.end]!!.filterNot { it.end in visited }
                if (validNext.isEmpty()) Int.MIN_VALUE
                else (current.cost + validNext.maxOf { longestPathToFinish(it, visited + it.end) })
            }
        }

    }

    fun part1(input: List<String>): Int {
        val vertices = parseVertices(input)
        val edges = parseEdges(input, vertices)
        return Graph(edges).longestPathToFinish()
    }

    fun part2(input: List<String>): Int {
        val inputWithoutSlopes = input.map { line ->
            line.map { if (it in slopes.keys) '.' else it }
                .joinToString("")
        }
        val vertices = parseVertices(inputWithoutSlopes)
        val edges = parseEdges(inputWithoutSlopes, vertices)
        return Graph(edges).longestPathToFinish()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day23_test")
    check(part1(testInput) == 94)
    check(part2(testInput) == 154)

    val input = readInput("Day23")
    part1(input).println()
    part2(input).println()
}
