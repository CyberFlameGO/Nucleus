package io.github.nucleuspowered.gradle.task

import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

open class StdOutExec : AbstractExecTask<StdOutExec>(StdOutExec::class.java) {

    val variableWriter: ByteArrayOutputStream = ByteArrayOutputStream()

    init {
        super.setStandardOutput(variableWriter)
    }

    var result: String? = null

    @TaskAction
    override fun exec() {
        super.exec()
        result = variableWriter.toString(StandardCharsets.UTF_8.toString())
        variableWriter.reset()
    }

    override fun setStandardOutput(outputStream: OutputStream): StdOutExec {
        // noop
        return this
    }

}