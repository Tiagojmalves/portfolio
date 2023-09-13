import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.JComboBox
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.FlexibleTypeDeserializer.ThrowException


class WidgetView (val model: ModelJson): JPanel() {

    private val observers: MutableList<WidgetViewObserver> = mutableListOf()

    init {



        layout = GridLayout(0,1)


         model.forEach {
            addElement(it)
        }

        model.addObserver(object:JsonObserver{

            override fun objectAdded(e: Element) {

                addElement(e)
                revalidate()
                repaint()
            }

        })

        model.addObserver(object :JsonObserver{

            override fun objectRemoved(e: Element) {

                revalidate()
                repaint()
                removeElement(e)
            }

        })

        this.addObserver(object :WidgetViewObserver{

            override fun Objectremoved(e: Element) {

                revalidate()
                repaint()
                model.remove(e)

            }

        })





    }

    fun addObserver(observer: WidgetViewObserver) {
        observers.add(observer)
    }

    inner class ElementComponent(var el: Element) : JComponent() {

        val first = JLabel("${el.name}")
        val second = JTextField("${el.key}")

        inner class MouseClick(val first: Boolean) : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent?) {


                    val menu = JPopupMenu("Message")
                    val add = JButton("add")
                    add.alignmentX = CENTER_ALIGNMENT
                    add.alignmentY = CENTER_ALIGNMENT
                    add.addActionListener {
                        menu.isVisible = false
                        val newPanel = JPanel()
                        val newPanel1 = JPanel()
                        val a = "Json Object"
                        val b = "Json Final"
                        val c = "Json Array"
                        val distros = arrayOf(
                            a, b, c
                        )
                        val box = JComboBox(distros)
                        val box1 = JComboBox<String>()

                        model.forEach {
                            if (it.name == null && it.parent == null) box1.addItem("Main Object")
                            else if (it.name == null && it.parent != null)box1.addItem("Empty Object, child of ${it.parent?.name}")
                            else
                                box1.addItem(it.name)
                        }
                        newPanel.setSize(300, 300)
                        newPanel.apply {

                            add(JLabel("Choose type"))
                            add(box)
                            JOptionPane.showConfirmDialog(null, newPanel, null, JOptionPane.OK_CANCEL_OPTION)
                            isVisible = true

                        }
                        newPanel1.setSize(300, 300)
                        newPanel1.apply {

                            add(JLabel("Choose parent"))
                            add(box1)
                            JOptionPane.showConfirmDialog(null, newPanel1, null, JOptionPane.OK_CANCEL_OPTION)
                            isVisible = true

                        }
                        val text = dualPrompt("Json Object", "Name", "Key")

                        val parent: Element? = if(box1.selectedItem == "Main Object")model.first()
                                               else if(box1.selectedItem.toString().startsWith("Empty Object"))model.firstOrNull { it.parent?.name == box1.selectedItem.toString().substringAfter("Empty Object, child of ") }
                                               else model.firstOrNull { it.name == box1.selectedItem }



                            if (box.selectedItem == a) model.add(JsonObject(parent))

                            else if (box.selectedItem == b) model.add(JsonFinal(text?.first, parent, text?.second))

                            else if (box.selectedItem == c) model.add(JsonArray(text?.first, parent, mutableListOf<Element>()))


                        println(model)
                        revalidate()
                        repaint()


                    }
                    val del = JButton("delete")
                    del.addActionListener {


                        if (el == model.first()) error("cant delete main object")

                        el?.let {

                            observers.forEach {

                                it.Objectremoved(el)
                            }

                        }
                        menu.isVisible = false
                        revalidate()
                        repaint()
                    }

                    menu.add(add)
                    menu.add(del)
                    menu.alignmentX = CENTER_ALIGNMENT
                    menu.alignmentY = CENTER_ALIGNMENT
                    menu.show(menu.invoker,200,200)




                }

        }




        init {


            layout = GridLayout(0, 3)

            first.isEnabled = false
            first.addMouseListener(MouseClick(true))
            add(first)

            second.isEnabled = false
            second.addMouseListener(MouseClick(false))
            add(second)

        }


    }

    private fun addElement(e: Element) {
        add(ElementComponent(e))
        revalidate()
        repaint()
    }

    private fun removeElement(e:Element){

        val find = components.find { it is ElementComponent && it.el == e }
        find?.let {
            remove(find)
        }
        revalidate()
        repaint()
    }



}


interface WidgetViewObserver{

    fun ObjectModified(old:Element,new:Element){}

    fun Objectremoved(e:Element){}


}

