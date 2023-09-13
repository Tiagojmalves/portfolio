import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.JComboBox
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.accessibility.AccessibleEditableText
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField


class TextView (model:ModelJson, var main:Element): JTextArea(){


    init {


        text = main.toText
        tabSize = 2
        isEditable = false



        model.addObserver(object:JsonObserver{

            override fun objectAdded(e: Element) {

                text = main.toText


            }

        })

        model.addObserver(object : JsonObserver{

            override fun objectRemoved(e: Element) {


                revalidate()
                repaint()
                text = main.toText

            }

        })

    }




}


