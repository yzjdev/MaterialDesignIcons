package lt.neworld.vd2svg.processor

import lt.neworld.vd2svg.converter.Converter
import lt.neworld.vd2svg.logProgress
import lt.neworld.vd2svg.logWarning
import java.io.File
import java.nio.file.PathMatcher

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class Processor(val converter: Converter, val input: List<PathMatcher>, val output: File?) {
    fun process() {
        prepareOutput(output)
        File(".").walkTopDown()
            .filter { file -> input.any { it.matches(file.relativeTo(File(".")).toPath()) } }
            .filter { file -> file.readText().contains("<vector") }
            .forEach { file ->
                logProgress("Processing: $file")

                val outputFile = createOutputFile(file)
                val outputStream = outputFile.outputStream()
                try {
                    converter.convert(file.inputStream(), outputStream)
                } catch (e: Exception) {
                    outputFile.delete()
                    logWarning("Processing failed: $file\n $e")
                }
                outputStream.close()
            }
    }

    private fun createOutputFile(input: File): File {
        val filename = input.nameWithoutExtension
        val outputDir = output ?: input.parentFile

        val fileOutput = File(outputDir, "$filename.svg")

        logProgress("Save to: $fileOutput")

        return fileOutput
    }

    private fun prepareOutput(output: File?) {
        output ?: return

        if (output.isFile) {
            throw RuntimeException("output is not directory")
        }
        if (!output.exists()) {
            logProgress("Creating output dir: ${output.path}")
            output.mkdirs() || throw RuntimeException("Failed create output dir")
        }
    }
}