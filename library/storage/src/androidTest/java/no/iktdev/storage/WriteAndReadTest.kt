package no.iktdev.storage

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import no.iktdev.storage.demostructure.CoreData
import no.iktdev.storage.demostructure.CoreImpl
import no.iktdev.storage.demostructure.SubData
import no.iktdev.storage.isolated.Reader
import no.iktdev.storage.isolated.Writer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File



@RunWith(AndroidJUnit4::class)

class WriteAndReadTest {


    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("no.iktdev.storage.test", appContext.packageName)
    }


    @Test
    fun testWrite() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val paths = arrayOf("files", "test.json")


        val testItems = listOf<CoreImpl<SubData>>(
            CoreImpl("Pain", data = SubData("file pain")),
            CoreImpl("NoFun", data = SubData("No fun allowed")),
            CoreImpl("Fun", data = SubData("Not allowed"))
        )
        val writer = TestWriter(context = appContext, paths)
        testItems.forEach {
            writer.writeData(it)
        }

        val reader = TestReader(appContext, paths)
        val read = reader.readData()
        assert(read.isNotEmpty())
        //assertThat(read).isNotEmpty
    }



    class TestReader(context: Context, val paths: Array<String>): Reader(context) {

        fun readData(): List<CoreData> {
            return readWith<List<CoreImpl<SubData>>>(paths) ?: emptyList()
        }
    }

    class TestWriter(context: Context, val paths: Array<String>): Writer(context) {
        fun writeData(data: CoreData) {
            val existing = TestReader(context, paths).readData().toMutableList() ?: mutableListOf()
            existing.add(data)
            writeWith(existing, paths)
        }
    }
}
