import java.awt.Choice
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*
import javax.swing.JComboBox
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField



fun main() {

    val mainObject = JsonObject(null)
    val w = JsonFinal("uc", mainObject, "PA")
    val y = JsonArray("inscritos", mainObject, mutableListOf<Element>())


    val model = ModelJson(mutableListOf(mainObject,w,y))

    val view = JFrame().apply {

        val frame = JFrame("Josue - JSON Object Editor").apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            layout = GridLayout(0, 2)
            size = Dimension(600, 600)

            val left = JPanel()
            left.layout = GridLayout()
            val scrollPane = JScrollPane(WidgetView(model)).apply {
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            }

            val right = JPanel()
            right.layout = GridLayout()
            val scrollPane1 = JScrollPane(TextView(model,mainObject)).apply {
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            }


            right.add(scrollPane1)
            left.add(scrollPane)
            add(left)
            add(right)




        }
        frame.isVisible = true



    }




}










