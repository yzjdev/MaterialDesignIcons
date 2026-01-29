package io.github.yzjdev.mdicon

import lt.neworld.vd2svg.converter.Converter
import lt.neworld.vd2svg.resources.AndroidResourceParser
import lt.neworld.vd2svg.resources.ResourceCollector
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.FileSystems
import lt.neworld.vd2svg.processor.Builder as ProcessorBuilder

class Vd2SvgConverter {
    fun convertVectorsToSvg(resourceDir: String, inputPattern: String, outputDir: String? = null) {
        val resourceDirectory = File(resourceDir)
        val inputPathMatcher = FileSystems.getDefault().getPathMatcher("glob:$inputPattern")
        val outputDirectory = outputDir?.let { File(it) }

        // 查找资源文件
        val resourceFiles = findResourceFiles(resourceDirectory)

        // 构建处理器
        val processorBuilder = ProcessorBuilder(
            resourceFiles = resourceFiles,
            input = listOf(inputPathMatcher),
            output = outputDirectory
        )

        val processor = processorBuilder.build()

        // 执行转换
        processor.process()
    }

    // 单文件转换功能：只指定输入和输出文件路径
    fun convertSingleFile(inputFile: String, outputFile: String, resourceFiles: List<String> = emptyList()) {
        // 构建资源收集器
        val collector = ResourceCollector()

        // 如果提供了资源文件，则加载它们；否则为空
        resourceFiles.forEach { resourceFilePath ->
            val resourceFile = File(resourceFilePath)
            if (resourceFile.exists()) {
                val parser = AndroidResourceParser(FileInputStream(resourceFile))
                collector.addResources(parser.values("color"))
            }
        }

        // 创建转换器
        val converter = Converter(collector)

        // 执行单文件转换
        val input = File(inputFile).inputStream()
        val output = File(outputFile).outputStream()

        try {
            converter.convert(input, output)
        } finally {
            input.close()
            output.close()
        }
    }

    // 新增：将输入文件转换为SVG字符串
    fun convert(inputFile: File): String {
        // 创建空的资源收集器
        val collector = ResourceCollector()

        // 创建转换器
        val converter = Converter(collector)

        // 执行转换并将结果保存到内存中
        val input = inputFile.inputStream()
        val output = ByteArrayOutputStream()

        try {
            converter.convert(input, output)
            return output.toString("UTF-8")
        } finally {
            input.close()
            output.close()
        }
    }

    private fun findResourceFiles(resourceDir: File): List<File> {
        val result = mutableListOf<File>()

        if (resourceDir.exists() && resourceDir.isDirectory) {
            resourceDir.walkTopDown()
                .filter { it.isFile && it.extension.equals("xml", ignoreCase = true) }
                .filter { file ->
                    // 检查是否包含Android资源定义
                    val content = file.readText()
                    content.contains("<resources") ||
                            content.contains("<color") ||
                            content.contains("<string")
                }
                .forEach { result.add(it) }
        }

        return result
    }
}

// 使用示例
fun main() {
    val converter = Vd2SvgConverter()

    // 批量转换示例
    converter.convertVectorsToSvg(
        resourceDir = "./app/src/main/res/values",  // 包含colors.xml等资源文件的目录
        inputPattern = "**/*.xml",                 // 要转换的Vector Drawable文件模式
        outputDir = "./svg_output"                 // 输出SVG文件的目录
    )

    // 单文件转换示例（只需指定输入和输出文件路径）
    converter.convertSingleFile(
        inputFile = "E:/Projects/MyApp/app/src/main/res/drawable/ic_example.xml",  // 输入文件路径
        outputFile = "E:/Output/SVG/ic_example.svg",  // 输出文件路径
        resourceFiles = listOf(  // 可选：提供资源文件路径
            "E:/Projects/MyApp/app/src/main/res/values/colors.xml"
        )
    )

    // 直接获取SVG字符串
    val svgContent = converter.convert(File("E:/Projects/MyApp/app/src/main/res/drawable/ic_example.xml"))
    println("Converted SVG content:")
    println(svgContent)
}
