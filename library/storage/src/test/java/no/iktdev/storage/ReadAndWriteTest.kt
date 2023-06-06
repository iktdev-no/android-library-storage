package no.iktdev.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.iktdev.storage.demostructure.CoreImpl
import no.iktdev.storage.demostructure.SubData
import no.iktdev.storage.isolated.Reader
import no.iktdev.storage.isolated.Storage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReadAndWriteTest {

    val data = """
            {
                "header": "Pain",
                "data": {
                    "substring": "this is pain"
                }
            }
        """.trimIndent()

    @Test
    fun testReading() {


        val typeToken = object : TypeToken<CoreImpl<SubData>>() {}.type
        val result = Storage.fromJson<CoreImpl<SubData>>(data, typeToken)

        assertThat(result).isInstanceOf(CoreImpl::class.java)
        assertThat(result?.data).isInstanceOf(SubData::class.java)
    }

    @Test
    fun testWriteAndRead() {
        val demo = CoreImpl("Pain", data = SubData("this is pain"))
        val json = Gson().toJson(demo)

        val typeToken = object : TypeToken<CoreImpl<SubData>>() {}.type
        val result = Storage.fromJson<CoreImpl<SubData>>(json, typeToken)

        assertThat(result).isEqualTo(demo)

    }


}