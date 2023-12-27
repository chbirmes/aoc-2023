import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KProperty1

fun main() {

    data class P3(val x: Int, val y: Int, val z: Int) {
        fun moveZ(offset: Int) = copy(z = z + offset)
    }

    class Brick(val start: P3, val end: P3) {

        val minZ = min(start.z, end.z)

        private fun range(property: KProperty1<P3, Int>): IntRange {
            val startValue = property.get(start)
            val endValue = property.get(end)
            return min(startValue, endValue)..max(startValue, endValue)
        }

        val occupiedPositions: Set<P3> by lazy {
            (if (start.x != end.x) range(P3::x).map { P3(it, start.y, start.z) }
            else if (start.y != end.y) range(P3::y).map { P3(start.x, it, start.z) }
            else if (start.z != end.z) range(P3::z).map { P3(start.x, start.y, it) }
            else setOf(start))
                .toSet()
        }

        fun moveZ(offset: Int) = Brick(start.moveZ(offset), end.moveZ(offset))
    }

    fun parseBrick(line: String) =
        line.split(',', '~').map { it.toInt() }.let { ints ->
            Brick(P3(ints[0], ints[1], ints[2]), P3(ints[3], ints[4], ints[5]))
        }

    fun List<Brick>.maxValueOf(property: KProperty1<P3, Int>) =
        maxOf { max(property.get(it.start), property.get(it.end)) }

    fun Array<Array<Array<Brick?>>>.isFree(newBrick: Brick, oldBrick: Brick) =
        newBrick.occupiedPositions.all {
            val position = this[it.x][it.y][it.z - 1]
            position == null || position == oldBrick
        }

    fun Array<Array<Array<Brick?>>>.put(brick: Brick) =
        brick.occupiedPositions.forEach { this[it.x][it.y][it.z - 1] = brick }

    fun Array<Array<Array<Brick?>>>.remove(brick: Brick) =
        brick.occupiedPositions.forEach { this[it.x][it.y][it.z - 1] = null }

    fun Array<Array<Array<Brick?>>>.directlySupportedBricks(brick: Brick) =
        brick.moveZ(1).occupiedPositions.mapNotNull { this[it.x][it.y][it.z - 1] }.filterNot { it == brick }.toSet()

    fun Array<Array<Array<Brick?>>>.directlySupportingBricks(brick: Brick) =
        brick.moveZ(-1).occupiedPositions.mapNotNull { this[it.x][it.y][it.z - 1] }.filterNot { it == brick }.toSet()

    fun Array<Array<Array<Brick?>>>.isRemovable(brick: Brick) =
        directlySupportedBricks(brick).none { directlySupportingBricks(it) == setOf(brick) }

    fun Array<Array<Array<Brick?>>>.fallingBricks(startBrick: Brick): Set<Brick> {
        val queue = PriorityQueue<Brick>(compareBy { it.minZ })
        val falling = mutableSetOf(startBrick)
        queue.addAll(directlySupportedBricks(startBrick))
        while (queue.isNotEmpty()) {
            val nextBrick = queue.poll()!!
            if ((directlySupportingBricks(nextBrick) - falling).isEmpty()) {
                falling.add(nextBrick)
                directlySupportedBricks(nextBrick).forEach {
                    if (it !in queue) queue.add(it)
                }
            }
        }
        falling.remove(startBrick)
        return falling
    }

    fun Array<Array<Array<Brick?>>>.putAndSink(bricks: List<Brick>): List<Brick> {
        bricks.forEach { put(it) }
        return bricks.map { brick ->
            val newBrick = (0..<(brick.minZ - 1))
                .firstOrNull { !isFree(brick.moveZ(-(it + 1)), brick) }
                ?.let { brick.moveZ(-it) }
                ?: brick.moveZ(-(brick.minZ - 1))
            remove(brick)
            put(newBrick)
            newBrick
        }
    }

    fun createSpaceFor(bricks: List<Brick>): Array<Array<Array<Brick?>>> {
        val maxX = bricks.maxValueOf(P3::x)
        val maxY = bricks.maxValueOf(P3::y)
        val maxZ = bricks.maxValueOf(P3::z)
        return Array(maxX + 1) { Array(maxY + 1) { Array(maxZ) { null } } }
    }

    fun part1(input: List<String>): Int {
        val bricks = input.map { parseBrick(it) }.sortedBy { it.minZ }
        val space = createSpaceFor(bricks)
        val finalBricks = space.putAndSink(bricks)
        return finalBricks.count { space.isRemovable(it) }
    }

    fun part2(input: List<String>): Int {
        val bricks = input.map { parseBrick(it) }.sortedBy { it.minZ }
        val space = createSpaceFor(bricks)
        val finalBricks = space.putAndSink(bricks)
        return finalBricks.sumOf { space.fallingBricks(it).size }
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 5)
    check(part2(testInput) == 7)

    val input = readInput("Day22")
    part1(input).println()
    part2(input).println()
}
