# PA_23

Utilização da biblioteca:

Existem 3 tipos de objetos JSON: 

- Json Final, composto por primitivos como strings, ints, etc
- Json Array, composto por coleções
- Json Object, representante de objetos vazios

Para criar um modelo é necessário criar um main object que terá que ser sempre um JSON Object

Cada objeto tem que ser criado individualmente, especificando o seu nome, chave e pai, por exemplo:

val mainObject = JsonObject(null)
val w = JsonFinal("uc", mainObject, "PA")

o modelo é criado utilizando o objecto JSON Model, com o main object e a lista de objetos do modelo como argumentos, por exemplo:

val model = ModelJson(mutableListOf(mainObject,w))

A versão textual do JSON é obtida através do metodo .toText, o modelo inteiro é obtido através da chamada deste método no primeiro objeto, ou seja, do main object

mainObject.toText

Na instaciação de data classes como chave de um objeto é possivel especificar o nome através da anotação @Nome e forçar chaves como strings através de @ForceString, por exemplo:

data class Student(

    @Nome("Numero")
    @ForceString
    val n:Int,
    @Nome("Nome")
    val n1:String,

    val international:Boolean,

    val type:StudentType?

)
neste caso o nome do valor n seria "Numero" e seria String em vez de Int na versão textual do JSON

Exemplo da utilização de visitantes encontram-se presentes no ficheiro Tests.kt

--------------------------------------------------------------------------------------------------------------------------------------------------------------------

A versão gráfica do modelo pode ser observada a partir do ficheiro Controller.kt


