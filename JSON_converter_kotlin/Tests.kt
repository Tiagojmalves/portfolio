import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.annotation.RetentionPolicy
import javax.swing.JPanel
import kotlin.reflect.*
import kotlin.reflect.full.*

enum class StudentType {
    Bachelor, Master, Doctoral
}


@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Nome(val name:String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ForceString




data class Student(

    @Nome("Numero")
    @ForceString
    val n:Int,
    @Nome("Nome")
    val n1:String,

    val international:Boolean,

    val type:StudentType?

)


class TestsProject() {

    @Test
    fun test() {

        val x = JsonObject(null)
        val w = JsonFinal("uc", x, "PA")
        val y = JsonArray("inscritos", x, mutableListOf<Element>())
        val z = JsonArray("teste",x, mutableListOf<String>("abc","def","ghi"))
        val v = JsonObject(y)
        val t = JsonObject(y)
        val pedro = Student(1111,"Pedro",false,StudentType.Doctoral)
        val pedrol = JsonFinal(null,v,pedro)
        val tiago = JsonFinal(null,t,Student(96144,"Tiago",false,StudentType.Bachelor))



        assertEquals("yes", "yes")

        // Visitor test
        val va = object : Visitor {

            var valueList: MutableList<Any?> = mutableListOf()
            var count:Int = 0
            override fun visit(e: Element) {

                if(e.key!!::class.isData && "Numero" in e.key!!::class.dataClassFields.joinToString()
                    { it.findAnnotations<Nome>().firstOrNull()?.name?:it.name } )

                    e.key!!::class.dataClassFields.forEach { when(it.findAnnotations<Nome>().firstOrNull()?.name?:it.name){
                        "Numero" -> valueList.add(it.call(e.key))
                        else -> count ++

                    } }

            }

        }


        x.accept(va)
        assertEquals(listOf(1111,96144), va.valueList)

        // Teste Json texto
        assertEquals("yes", x.toText)






    }
}

