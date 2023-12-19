import java.util.PriorityQueue

fun <T> dijkstra(
    start: T,
    neighborsFunction: (T) -> List<T>,
    costFunction: (T) -> Int,
    stopPredicate: (T) -> Boolean,
    priorityComparator: Comparator<in Pair<T, Int>> = compareBy { it.second }
): Int {
    val priorityQueue = PriorityQueue(priorityComparator)
    return recursiveSearch(start, neighborsFunction, costFunction, stopPredicate, priorityQueue)
}

private tailrec fun <T> recursiveSearch(
    node: T,
    neighborsFunction: (T) -> List<T>,
    costFunction: (T) -> Int,
    stopPredicate: (T) -> Boolean,
    priorityQueue: PriorityQueue<Pair<T, Int>>,
    lowestCosts: MutableMap<T, Int> = mutableMapOf(),
): Int {
    if (stopPredicate(node)) {
        return lowestCosts[node]!!
    }
    visitNode(node, neighborsFunction, costFunction, priorityQueue, lowestCosts)
    val nextNode = priorityQueue.poll().first
    return recursiveSearch(nextNode, neighborsFunction, costFunction, stopPredicate, priorityQueue, lowestCosts)
}

private fun <T> visitNode(
    node: T,
    neighborsFunction: (T) -> List<T>,
    costFunction: (T) -> Int,
    priorityQueue: PriorityQueue<Pair<T, Int>>,
    lowestCosts: MutableMap<T, Int>
) {
    val costToCurrent = lowestCosts.getOrDefault(node, 0)
    neighborsFunction(node).forEach { neighbor ->
        val newCost = costFunction(neighbor) + costToCurrent
        val nextCost = lowestCosts.getOrDefault(neighbor, Integer.MAX_VALUE)
        if (newCost < nextCost) {
            lowestCosts[neighbor] = newCost
            priorityQueue.add(neighbor to newCost)
        }
    }
}
