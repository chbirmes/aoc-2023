import kotlin.math.absoluteValue

fun main() {

    val north = -1 to 0
    val east = 0 to 1
    val south = 1 to 0
    val west = 0 to -1

    val charToDirection = mapOf(
        'U' to north,
        'R' to east,
        'D' to south,
        'L' to west,
        '0' to east,
        '1' to south,
        '2' to west,
        '3' to north
    )

    operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = (first + other.first) to (second + other.second)
    operator fun Pair<Int, Int>.times(scalar: Int) = (first * scalar) to (second * scalar)

    data class Instruction(val direction: Pair<Int, Int>, val steps: Int)

    fun parseInstruction(line: String) =
        Instruction(charToDirection[line[0]]!!, line.substring(2).substringBefore(' ').toInt())

    fun parseInstructionPart2(line: String) =
        line.substringAfter('#').substringBefore(')').let { hex ->
            Instruction(charToDirection[hex.last()]!!, hex.dropLast(1).toInt(16))
        }

    fun vertices(instructions: List<Instruction>): List<Pair<Int, Int>> =
        instructions.dropLast(1).runningFold(0 to 0) { position, instruction ->
            position + instruction.direction * instruction.steps
        }

    fun shoelace(vertices: List<Pair<Int, Int>>): Long {
        val a = vertices.windowed(2)
            .sumOf {
                it[0].first.toLong() * it[1].second.toLong() -
                        it[0].second.toLong() * it[1].first.toLong()
            } +
                vertices.last().first.toLong() * vertices.first().second.toLong() -
                vertices.last().second.toLong() * vertices.first().first.toLong()
        return a.absoluteValue / 2
    }

    fun circumference(instructions: List<Instruction>) = instructions.sumOf { it.steps - 1 }

    fun part1(input: List<String>): Long {
        val instructions = input.map { parseInstruction(it) }
        val vertices = vertices(instructions)
        return shoelace(vertices) + (circumference(instructions) / 2) + (instructions.size / 2) + 1
    }

    fun part2(input: List<String>): Long {
        val instructions = input.map { parseInstructionPart2(it) }
        val vertices = vertices(instructions)
        return shoelace(vertices) + (circumference(instructions) / 2) + (instructions.size / 2) + 1
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day18_test")
    check(part1(testInput) == 62L)
    check(part2(testInput) == 952408144115) // 952404941483 952411346731

    val input = readInput("Day18")
    part1(input).println()
    part2(input).println()
}
