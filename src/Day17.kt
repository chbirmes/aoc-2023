fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1
    val allDirections = setOf(north, east, south, west)

    operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)
    fun Pair<Int, Int>.isOppositeOf(other: Pair<Int, Int>) = this + other == 0 to 0

    data class Node(val position: Pair<Int, Int>, val direction: Pair<Int, Int>, val stepsTaken: Int)

    class CityBocks(val lines: List<String>) {

        val finish = lines.indices.last to lines.first().indices.last

        fun Pair<Int, Int>.isInBounds() = first in lines.indices && second in lines.first().indices

        fun neighbors(node: Node, minSteps: Int, maxSteps: Int): List<Node> {
            val neighborSequence =
                if (node.stepsTaken != 0 && node.stepsTaken < minSteps) sequenceOf(
                    node.copy(
                        position = node.position + node.direction,
                        stepsTaken = node.stepsTaken + 1
                    )
                )
                else allDirections.asSequence()
                    .filterNot { it.isOppositeOf(node.direction) }
                    .filterNot { it == node.direction && node.stepsTaken == maxSteps }
                    .map {
                        val newPosition = node.position + it
                        val newStepsTaken =
                            if (it == node.direction) node.stepsTaken + 1 else 1
                        Node(newPosition, it, newStepsTaken)
                    }
            return neighborSequence
                .filter { it.position.isInBounds() }
                .toList()
        }

        fun cost(node: Node): Int = lines[node.position.first][node.position.second].digitToInt()

        fun stop(node: Node, minStepsTaken: Int) = node.position == finish && node.stepsTaken >= minStepsTaken

        val priorityComparator: Comparator<Pair<Node, Int>> =
            compareBy<Pair<Node, Int>> { (node, cost) -> cost - node.position.first - node.position.second }
                .thenComparing { (node, _) -> node.stepsTaken }

        fun lowestHeatLoss(minSteps: Int, maxSteps: Int): Int {
            return dijkstra(
                start = Node(0 to 0, east, 0),
                neighborsFunction = { neighbors(it, minSteps, maxSteps) },
                costFunction = ::cost,
                stopPredicate = { stop(it, minSteps) },
                priorityComparator
            )
        }

    }

    fun part1(input: List<String>): Int = CityBocks(input).lowestHeatLoss(1, 3)

    fun part2(input: List<String>): Int = CityBocks(input).lowestHeatLoss(4, 10)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 102)
    check(part2(testInput) == 94)

    val input = readInput("Day17")
    part1(input).println()
    part2(input).println()
}

