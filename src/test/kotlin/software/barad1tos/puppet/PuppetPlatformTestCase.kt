package software.barad1tos.puppet

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import software.barad1tos.puppet.settings.OpenVoxSettings

/** Platform fixture that keeps highlighting hermetic: puppet and puppet-lint from the host PATH never run. */
abstract class PuppetPlatformTestCase : BasePlatformTestCase() {
    private var parserValidateWasEnabled = true
    private var puppetLintWasEnabled = true

    override fun setUp() {
        super.setUp()
        val settings = OpenVoxSettings.getInstance()
        parserValidateWasEnabled = settings.runParserValidate
        puppetLintWasEnabled = settings.runPuppetLint
        settings.runParserValidate = false
        settings.runPuppetLint = false
    }

    override fun tearDown() {
        try {
            val settings = OpenVoxSettings.getInstance()
            settings.runParserValidate = parserValidateWasEnabled
            settings.runPuppetLint = puppetLintWasEnabled
        } finally {
            super.tearDown()
        }
    }
}
