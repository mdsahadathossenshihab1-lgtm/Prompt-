package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.PromptGeneratorService
import com.example.model.PromptConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PromptFlow AI", appName)
    }

    @Test
    fun `test master prompt local synthesis format and mandates`() {
        val service = PromptGeneratorService()
        val bengaliDialogue = "আমাদের আধুনিক প্রযুক্তির ছোঁয়ায় গ্রাম থেকে শহরে এক নতুন বিপ্লব তৈরি হয়েছে।"
        val config = PromptConfig(
            script = bengaliDialogue,
            aspectRatio = "9:16",
            resolution = "1080x1920",
            duration = "10 seconds",
            videoStyle = "Cinematic",
            location = "Rural",
            presenter = "Male",
            camera = "Medium shot",
            bRoll = "Auto"
        )

        val masterPrompt = service.generateHighFidelityLocalPrompt(config)

        // 1. Verify exact dialogue preservation
        assertTrue("Must contain exact dialogue", masterPrompt.contains(bengaliDialogue))

        // 2. Verify mandatory prompt sections
        assertTrue("Must include VIDEO FORMAT", masterPrompt.contains("VIDEO FORMAT: 9:16"))
        assertTrue("Must include RESOLUTION", masterPrompt.contains("RESOLUTION: 1080x1920"))
        assertTrue("Must include DURATION", masterPrompt.contains("DURATION: 10 seconds"))
        assertTrue("Must include LIP-SYNC", masterPrompt.contains("LIP-SYNC"))
        assertTrue("Must include CONTINUITY / One voice take", masterPrompt.contains("One continuous take, single voice actor"))
        assertTrue("Must include B-ROLL synchronization", masterPrompt.contains("B-roll cutaways"))
        assertTrue("Must include NEGATIVE INSTRUCTIONS", masterPrompt.contains("NEGATIVE INSTRUCTIONS"))
    }
}
