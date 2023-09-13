
import kotlin.reflect.*
import kotlin.reflect.full.*


sealed interface Element {

    val name:String?
    val children: MutableList<Element>?
    val parent: Element?
    val key:Any?
    fun accept(v:Visitor){
        v.visit(this)
    }
    val observers :MutableList<JsonObserver>



    fun add(p: Element) {

        // fires event to registered observers
            observers.forEach {
                it.objectAdded(p)
            }
    }
    val depth: Int?
        get() = if (parent != null) parent?.depth?.plus(1) else 0

    val toText:String

}

interface Visitor{

    fun visit(e:Element){}

}



interface  JsonObserver{

    fun objectAdded(e:Element){}

    fun objectRemoved(e:Element){}

    fun objectReplaced(old:Element,new:Element){}

}

val KClass<*>.dataClassFields: List<KProperty<*>>
    get() {
        require(isData) { "instance must be data class" }
        return primaryConstructor!!.parameters.map { p ->
            declaredMemberProperties.find { it.name == p.name }!!
        }
    }

class ModelJson(model: MutableList<Element>):Iterable<Element>{

    private val data = model.toMutableSet()

    private val observers:MutableList<JsonObserver> = mutableListOf()

    fun addObserver(observer: JsonObserver) = observers.add(observer)

    fun add(p: Element) {

        if(data.add(p))
        // fires event to registered observers
            observers.forEach {
                it.objectAdded(p)
        }
    }

    override fun iterator(): Iterator<Element> = data.iterator()

    override fun toString(): String {
        return data.joinToString(separator = "    ") { it.toString() }
    }

    fun remove(e:Element) {
        if(data.remove(e))

            e.parent?.children?.remove(e)
            observers.forEach {

                it.objectRemoved(e)

            }
    }
}


data class JsonFinal(

    override val name: String?,
    override val parent: Element? = null,
    override val key: Any?):Element{

    override val children: MutableList<Element>? = null

    override val observers: MutableList<JsonObserver> = mutableListOf()


    override val toText: String
        get() = if(name != null)"${"\t".repeat(depth!!)}" + "'${name}':" + typeSwitch(this.key)
                else  typeSwitch(this.key)

    override fun accept(v: Visitor) {
        v.visit(this)
    }

    private fun typeSwitch(k:Any?):String {

        if (k is String) return "'${k}',\n"
        else if (k == null)return  "\n"
        else if (k::class.isData) return k::class.dataClassFields.joinToString("")
        { "${"\t".repeat(depth!!)}" + "'${ it.findAnnotations<Nome>().firstOrNull()?.name?: it.name}': ${if(it.hasAnnotation<ForceString>()) typeSwitch(it.call(k).toString())else typeSwitch(it.call(k))}" }
        else return "${k},\n"
    }

    init {
        parent?.children?.add(this)



    }

}

data class JsonObject(

    override val parent: Element?,


):Element{

    override val key: Any? = null

    override val name: String? = null

    override var children:MutableList<Element> = mutableListOf()

    override val observers: MutableList<JsonObserver> = mutableListOf()

    override val toText: String
        get() = "${"\t".repeat(depth!!)}{" + typeSwitch(children.joinToString("") { it.toText }) + "${"\t".repeat(depth!!)}}\n"

    private fun typeSwitch(k:Any?):String =

        "\n${k.toString()}\n"

    override fun accept(v: Visitor) {
        children.forEach {
            it.accept(v)
        }
    }
    init {
        parent?.children?.add(this)
    }

}

data class JsonArray(

    override val name: String?,
    override val parent: Element?=null,
    override val key: Any?
    ):Element{

    override var children:MutableList<Element> = mutableListOf()

    override val observers: MutableList<JsonObserver> = mutableListOf()

    override val toText: String
        get() = if (name != null)"${"\t".repeat(depth!!)}" + "'${name}':" + typeSwitch(children.joinToString("${"\t".repeat(depth!!)},\n") { it.toText })
                else "${"\t".repeat(depth!!)}"  + typeSwitch(children.joinToString("") { it.toText })

    private fun typeSwitch(k:Any?):String =

        if (key is ArrayList<*> && key.isEmpty()|| key is HashMap<*,*> && key.isEmpty()|| key is HashSet<*> && key.isEmpty())
            "[\n${k.toString()}\n ${"\t".repeat(depth!!)}]\n"
        else "\n${"\t".repeat(depth!!)}[\n${(key as ArrayList<*>).joinToString(",\n${"\t".repeat(depth!!)}","${"\t".repeat(depth!!)}") { it.toString() }}\n ${"\t".repeat(depth!!)}]\n"

    override fun accept(v: Visitor) {
        children.forEach {
            it.accept(v)
        }
    }

    init {
        parent?.children?.add(this)
    }

}