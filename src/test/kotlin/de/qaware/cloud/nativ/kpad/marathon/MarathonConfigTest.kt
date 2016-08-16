package de.qaware.cloud.nativ.kpad.marathon

import com.moandjiezana.toml.Toml
import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull

class MarathonConfigTest {

    @Test
    fun testConfig() {
        val homeDirectory = System.getProperty("user.home")
        val dcosConfigFile = File(homeDirectory + "/.dcos/dcos.toml")

        val dcosConfig = Toml().read(dcosConfigFile)
        println(dcosConfig.toMap().toString())
        assertNotNull(dcosConfig.getString("core.dcos_url"), "dcos_url is null!")
        assertNotNull(dcosConfig.getString("core.dcos_acs_token"), "dcos_acs_token is null!")
    }
}