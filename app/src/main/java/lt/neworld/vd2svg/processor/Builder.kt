package lt.neworld.vd2svg.processor

import lt.neworld.vd2svg.converter.Builder
import lt.neworld.vd2svg.resources.ResourceCollector
import java.io.File
import java.nio.file.PathMatcher

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class Builder(
    private val resourceFiles: List<File>,
    private val input: List<PathMatcher>,
    private val output: File?
) {
    fun build(): Processor {
        val converterBuilder = Builder(resourceFiles)
        val converter = converterBuilder.build()

        return Processor(converter, input, output)
    }
}
