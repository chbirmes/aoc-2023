import Day20.Module
import Day20.Module.*
import Day20.Pulse
import Day20.PulseQueue
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

object Day20 {

    data class Pulse(val source: String, val high: Boolean, val destination: String)
    sealed class Module(val name: String, val destinations: List<String>) {

        abstract fun process(pulse: Pulse): List<Pulse>

        class Broadcaster(destinations: List<String>) : Module("broadcaster", destinations) {
            override fun process(pulse: Pulse) = destinations.map { pulse.copy(source = name, destination = it) }
        }

        class FlipFlop(name: String, destinations: List<String>) :
            Module(name, destinations) {

            private var on = false
            override fun process(pulse: Pulse): List<Pulse> {
                return if (pulse.high) emptyList()
                else {
                    on = !on
                    destinations.map { Pulse(name, on, it) }
                }
            }

        }

        class Conjunction(name: String, destinations: List<String>, sources: Set<String>) :
            Module(name, destinations) {

            private val rememberedInputs = mutableMapOf(*(sources.map { it to false }.toTypedArray()))

            override fun process(pulse: Pulse): List<Pulse> {
                rememberedInputs[pulse.source] = pulse.high
                val allHigh = rememberedInputs.values.all { it }
                return destinations.map { Pulse(name, !allHigh, it) }
            }

        }

    }

    class PulseQueue {

        private val queue = LinkedList<Pulse>()

        var lowCount = 0
        var highCount = 0

        fun addAll(pulses: List<Pulse>) {
            pulses.forEach {
                if (it.high) highCount++ else lowCount++
            }
            queue.addAll(pulses)
        }

        fun poll() = queue.poll()!!

        fun isEmpty() = queue.isEmpty()

    }

}

fun main() {

    fun parseModules(input: List<String>): Map<String, Module> {
        val sourceToTargets = input.associate { line ->
            val split = line.split(" -> ")
            val name = split[0].filterNot { it in "%&" }
            val targets = split[1].split(", ")
            name to targets
        }
        return input.map { line ->
            val typeAndName = line.substringBefore(' ')
            when (typeAndName[0]) {
                '%' -> typeAndName.drop(1).let { name ->
                    FlipFlop(
                        name,
                        sourceToTargets[name]!!
                    )
                }

                '&' -> typeAndName.drop(1).let { name ->
                    Conjunction(
                        name,
                        sourceToTargets[name]!!,
                        sourceToTargets.filterValues { it.contains(name) }.keys
                    )
                }

                else -> Broadcaster(sourceToTargets["broadcaster"]!!)
            }
        }
            .associateBy { it.name }
    }

    fun part1(input: List<String>): Int {
        val modules = parseModules(input)
        val queue = PulseQueue()
        repeat(1000) {
            queue.addAll(listOf(Pulse("button", false, "broadcaster")))
            while (!queue.isEmpty()) {
                val pulse = queue.poll().takeIf { it.destination != "output" }
                if (pulse != null) {
                    modules[pulse.destination]?.process(pulse)
                        ?.let {
                            queue.addAll(it)
                        }
                }
            }
        }
        return queue.lowCount * queue.highCount
    }

    fun leastCommonMultiple(x: Long, y: Long): Long {
        val greater = max(x, y)
        val lesser = min(x, y)
        return generateSequence(greater) { it + greater }
            .first { it % lesser == 0L }
    }

    fun part2(input: List<String>): Long {
        val modules = parseModules(input)
        val queue = PulseQueue()
        val sourceToRx = modules.values.single { "rx" in it.destinations }
        if (sourceToRx !is Conjunction) throw IllegalStateException()
        val sourcesToSourceToRx = modules.values.filter { sourceToRx.name in it.destinations }
        if (sourcesToSourceToRx.any { it !is Conjunction }) throw IllegalStateException()
        val neededPeriods = sourcesToSourceToRx.map { it.name }.toSet()
        val periods = mutableMapOf<String, Long>()
        var c = 0L
        while (periods.keys != neededPeriods) {
            c++
            queue.addAll(listOf(Pulse("button", false, "broadcaster")))
            while (!queue.isEmpty()) {
                val pulse = queue.poll().takeIf { it.destination != "output" }
                if (pulse != null) {
                    if (pulse.high && pulse.source in neededPeriods && pulse.source !in periods.keys) {
                        periods[pulse.source] = c
                    }
                    modules[pulse.destination]?.process(pulse)
                        ?.let {
                            queue.addAll(it)
                        }
                }
            }
        }
        return periods.values.fold(1L, ::leastCommonMultiple)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day20_test")
    check(part1(testInput) == 11687500)

    val input = readInput("Day20")
    part1(input).println()
    part2(input).println()
}
