fun main() {

    fun hash(s: String) = s.fold(0) { currentValue, char -> ((currentValue + char.code) * 17) % 256 }

    fun part1(input: List<String>): Int = input.first().split(',').sumOf { hash(it) }

    data class Instruction(val label: String, val isRemove: Boolean, val focalLength: Int?) {
        val boxIndex = hash(label)
        fun execute(boxes: List<LinkedHashMap<String, Int>>) {
            if (isRemove) {
                boxes[boxIndex].remove(label)
            } else {
                boxes[boxIndex][label] = focalLength!!
            }
        }
    }

    fun parseInstruction(s: String): Instruction {
        val minusIndex = s.indexOf('-')
        return if (minusIndex != -1) Instruction(s.take(minusIndex), true, null)
        else s.split('=').let { Instruction(it[0], false, it[1].toInt()) }
    }

    fun part2(input: List<String>): Int {
        val boxes = List(256) { LinkedHashMap<String, Int>() }

        input.first().split(',')
            .map { parseInstruction(it) }
            .forEach { it.execute(boxes) }

        return boxes.mapIndexed { boxIndex, map ->
            (boxIndex + 1) * map.entries.foldIndexed(0) { index, sum, entry ->
                sum + (index + 1) * entry.value
            }
        }.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 1320)
    check(part2(testInput) == 145)

    val input = readInput("Day15")
    part1(input).println()
    part2(input).println()
}
