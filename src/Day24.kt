import kotlin.math.sign

fun main() {

    data class P2(val x: Double, val y: Double)
    data class V2(val dx: Long, val dy: Long)

    class Line2D(val point: P2, val vector: V2) {
        val a = vector.dy.toDouble() / vector.dx
        val b = point.y - a * point.x

        fun intersection(other: Line2D): P2? {
            return if (a == other.a) null
            else {
                val x = (other.b - b) / (a - other.a)
                val y = a * x + b
                P2(x, y)
            }
        }

        fun isInFuture(intersection: P2) = (intersection.x - point.x).sign.toInt() == vector.dx.sign
    }

    val intRegex = "-?\\d+".toRegex()

    fun parseLine2D(s: String) =
        intRegex.findAll(s).map { it.value.toLong() }.toList()
            .let { Line2D(P2(it[0].toDouble(), it[1].toDouble()), V2(it[3], it[4])) }

    fun part1(input: List<String>, testRange: ClosedFloatingPointRange<Double>): Int {
        val lines = input.map { parseLine2D(it) }
        return lines.mapIndexed { index, line ->
            lines.drop(index + 1).count { otherLine ->
                val intersection = line.intersection(otherLine)
                intersection != null && line.isInFuture(intersection) && otherLine.isInFuture(intersection) &&
                        intersection.x in testRange && intersection.y in testRange
            }
        }
            .sum()
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day24_test")
    check(part1(testInput, 7.0..27.0) == 2)
//    check(part2(testInput) == 47)

    val input = readInput("Day24")
    part1(input, 200000000000000.0..400000000000000.0).println()
//    part2(input).println()
}
