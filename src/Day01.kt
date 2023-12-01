fun main() {
    fun part1(input: List<String>): Int {
        fun lineToInt(line: String) =
            line.first { it.isDigit() }.digitToInt() * 10 +
                    line.last { it.isDigit() }.digitToInt()

        return input.sumOf { lineToInt(it) }
    }

    fun part2(input: List<String>): Int {
        val digitWords = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9
        )
        val regex = ("\\d|" + digitWords.keys.joinToString(separator = "|")).toRegex()
        val regexBackwards = ("\\d|" + digitWords.keys.joinToString(separator = "|") { it.reversed() }).toRegex()
        fun stringToDigit(s: String) = digitWords.getOrElse(s) { s.first().digitToInt() }

        fun lineToInt(line: String): Int {
            val first = stringToDigit(regex.find(line)!!.value)
            val second = stringToDigit(regexBackwards.find(line.reversed())!!.value.reversed())
            return first * 10 + second
        }

        return input.sumOf { lineToInt(it) }
    }

    // test if implementation meets criteria from the description, like:
    check(part1(readInput("Day01_test")) == 142)
    check(part2(readInput("Day01_test2")) == 281)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
