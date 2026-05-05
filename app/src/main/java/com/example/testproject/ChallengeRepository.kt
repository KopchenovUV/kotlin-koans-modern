package com.example.testproject

import androidx.lifecycle.ViewModel

data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val initialCode: String,
    val expectedOutput: String,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.EASY
)

enum class ChallengeDifficulty {
    EASY, MEDIUM, HARD
}

class ChallengeRepository {

    private val allChallenges = listOf(
        Challenge(
            id = 0,
            title = "Hello, world!",
            description = "Напишите функцию hello(), которая возвращает строку \"OK\"",
            initialCode = "fun hello(): String = TODO()\n\nfun main() {\n    println(hello())\n}",
            expectedOutput = "OK"
        ),
        Challenge(
            id = 1,
            title = "Data classes",
            description = "Сделайте Person data class и выведите его",
            initialCode = "data class Person(val name: String, val age: Int)\n\nfun main() {\n    val person = Person(\"Alice\", 30)\n    println(person)\n}",
            expectedOutput = "Person(name=Alice, age=30)"
        ),
        Challenge(
            id = 2,
            title = "Smart casts",
            description = "Используйте умное приведение типов",
            initialCode = "fun eval(expr: Any): Int =\n    if (expr is Int) {\n        expr\n    } else {\n        throw IllegalArgumentException(\"Unknown expr\")\n    }\n\nfun main() {\n    println(eval(42))\n}",
            expectedOutput = "42"
        ),
        Challenge(
            id = 3,
            title = "Sealed classes",
            description = "Реализуйте sealed class Expr",
            initialCode = "sealed class Expr\nclass Num(val value: Int) : Expr()\nclass Sum(val left: Expr, val right: Expr) : Expr()\n\nfun eval(e: Expr): Int = when(e) {\n    is Num -> e.value\n    is Sum -> eval(e.left) + eval(e.right)\n}\n\nfun main() {\n    println(eval(Sum(Num(1), Num(2))))\n}",
            expectedOutput = "3"
        ),
        Challenge(
            id = 4,
            title = "Rename on import",
            description = "Переименуйте импорт и используйте его",
            initialCode = "import kotlin.math.abs as absoluteValue\n\nfun main() {\n    println(absoluteValue(-10))\n}",
            expectedOutput = "10"
        ),
        Challenge(
            id = 5,
            title = "Extension functions",
            description = "Создайте расширяющие функции Int.r() и Pair.r()",
            initialCode = "fun Int.r(): RationalNumber = RationalNumber(this, 1)\nfun Pair<Int, Int>.r(): RationalNumber = RationalNumber(first, second)\n\ndata class RationalNumber(val numerator: Int, val denominator: Int)\n\nfun main() {\n    val r1 = 5.r()\n    val r2 = Pair(1, 2).r()\n    println(\"\${r1}, \${r2}\")\n}",
            expectedOutput = "RationalNumber(numerator=5, denominator=1), RationalNumber(numerator=1, denominator=2)"
        ),
        Challenge(
            id = 6,
            title = "Object expressions",
            description = "Создайте анонимный объект Comparator",
            initialCode = "import java.util.*\n\nfun getComparator(): Comparator<Int> = object : Comparator<Int> {\n    override fun compare(o1: Int, o2: Int): Int = o1 - o2\n}\n\nfun main() {\n    val list = listOf(5, 3, 1, 4, 2)\n    println(list.sortedWith(getComparator()))\n}",
            expectedOutput = "[1, 2, 3, 4, 5]"
        ),
        Challenge(
            id = 7,
            title = "SAM conversions",
            description = "Передайте лямбду как SAM-интерфейс",
            initialCode = "fun interface Action {\n    fun run()\n}\n\nfun runAction(a: Action) = a.run()\n\nfun main() {\n    runAction { println(\"Hello, SAM!\") }\n}",
            expectedOutput = "Hello, SAM!"
        ),
        Challenge(
            id = 8,
            title = "Extension function literals",
            description = "Используйте литерал расширяющей функции",
            initialCode = "val sum: Int.(Int) -> Int = { other -> plus(other) }\n\nfun main() {\n    println(1.sum(2))\n}",
            expectedOutput = "3"
        ),
        Challenge(
            id = 9,
            title = "Lambdas",
            description = "Завершите лямбда-выражение для чётных чисел",
            initialCode = "fun main() {\n    val numbers = listOf(1, 2, 3, 4, 5, 6)\n    val evens = numbers.filter { it % 2 == 0 }\n    println(evens)\n}",
            expectedOutput = "[2, 4, 6]"
        ),
        Challenge(
            id = 10,
            title = "Strings",
            description = "Используйте строковые шаблоны и trimMargin",
            initialCode = "fun main() {\n    val name = \"Kotlin\"\n    val version = 1.9\n    val message = \"\"\"\n        |Welcome to \$name version \$version!\n    \"\"\".trimMargin()\n    println(message)\n}",
            expectedOutput = "Welcome to Kotlin version 1.9!"
        ),
        Challenge(
            id = 11,
            title = "Nullable types",
            description = "Работа с nullable типами и оператором ?.",
            initialCode = "fun getLength(s: String?): Int = s?.length ?: 0\n\nfun main() {\n    println(getLength(null))\n    println(getLength(\"test\"))\n}",
            expectedOutput = "0\n4"
        ),
        Challenge(
            id = 12,
            title = "Nothing type",
            description = "Функция, возвращающая Nothing",
            initialCode = "fun fail(message: String): Nothing = throw IllegalArgumentException(message)\n\nfun main() {\n    try {\n        fail(\"Something went wrong\")\n    } catch (e: Exception) {\n        println(e.message)\n    }\n}",
            expectedOutput = "Something went wrong"
        ),
        Challenge(
            id = 13,
            title = "Introduction",
            description = "Простое введение в Kotlin",
            initialCode = "fun main() {\n    println(\"Kotlin is fun!\")\n}",
            expectedOutput = "Kotlin is fun!"
        ),
        Challenge(
            id = 14,
            title = "Named arguments",
            description = "Используйте именованные аргументы",
            initialCode = "fun greet(firstName: String, lastName: String) = \"Hello, \$firstName \$lastName!\"\n\nfun main() {\n    println(greet(lastName = \"Doe\", firstName = \"John\"))\n}",
            expectedOutput = "Hello, John Doe!"
        ),
        Challenge(
            id = 15,
            title = "Default arguments",
            description = "Используйте аргументы по умолчанию",
            initialCode = "fun greet(name: String = \"World\") = \"Hello, \$name!\"\n\nfun main() {\n    println(greet())\n    println(greet(\"Kotlin\"))\n}",
            expectedOutput = "Hello, World!\nHello, Kotlin!"
        ),
        Challenge(
            id = 16,
            title = "Triple-quoted strings",
            description = "Работа с тройными кавычками",
            initialCode = "fun main() {\n    val text = \"\"\"\n        |Line 1\n        |Line 2\n        |Line 3\n    \"\"\".trimMargin()\n    println(text)\n}",
            expectedOutput = "Line 1\nLine 2\nLine 3"
        ),
        Challenge(
            id = 17,
            title = "For loop",
            description = "Цикл for по коллекции",
            initialCode = "fun main() {\n    val fruits = listOf(\"Apple\", \"Banana\", \"Cherry\")\n    for (fruit in fruits) {\n        println(fruit)\n    }\n}",
            expectedOutput = "Apple\nBanana\nCherry"
        ),
        Challenge(
            id = 18,
            title = "Filter and map",
            description = "Комбинируйте filter и map",
            initialCode = "fun main() {\n    val numbers = listOf(1, 2, 3, 4, 5)\n    val result = numbers.filter { it % 2 == 1 }.map { it * it }\n    println(result)\n}",
            expectedOutput = "[1, 9, 25]"
        ),
        Challenge(
            id = 19,
            title = "All, Any, Count, Find",
            description = "Предикаты для коллекций",
            initialCode = "fun main() {\n    val numbers = listOf(-1, 0, 1, 2)\n    println(numbers.all { it >= 0 })\n    println(numbers.any { it < 0 })\n    println(numbers.count { it == 0 })\n    println(numbers.find { it > 1 })\n}",
            expectedOutput = "false\ntrue\n1\n2"
        ),
        Challenge(
            id = 20,
            title = "FlatMap",
            description = "Используйте flatMap",
            initialCode = "fun main() {\n    val list = listOf(1, 2, 3)\n    val result = list.flatMap { listOf(it, it * 10) }\n    println(result)\n}",
            expectedOutput = "[1, 10, 2, 20, 3, 30]"
        ),
        Challenge(
            id = 21,
            title = "Max and min",
            description = "Поиск максимума и минимума",
            initialCode = "fun main() {\n    val numbers = listOf(5, 2, 10, 8, 1)\n    println(\"Max: \${numbers.maxOrNull()}\")\n    println(\"Min: \${numbers.minOrNull()}\")\n}",
            expectedOutput = "Max: 10\nMin: 1"
        ),
        Challenge(
            id = 22,
            title = "Sort",
            description = "Сортировка коллекций",
            initialCode = "fun main() {\n    val numbers = listOf(5, 3, 1, 4, 2)\n    println(numbers.sorted())\n    println(numbers.sortedDescending())\n}",
            expectedOutput = "[1, 2, 3, 4, 5]\n[5, 4, 3, 2, 1]"
        ),
        Challenge(
            id = 23,
            title = "Sum",
            description = "Суммирование элементов",
            initialCode = "fun main() {\n    val numbers = listOf(1, 2, 3, 4, 5)\n    println(numbers.sum())\n}",
            expectedOutput = "15"
        ),
        Challenge(
            id = 24,
            title = "GroupBy",
            description = "Группировка элементов по ключу",
            initialCode = "fun main() {\n    val words = listOf(\"apple\", \"banana\", \"cherry\", \"avocado\", \"blueberry\")\n    val byFirstLetter = words.groupBy { it.first() }\n    println(byFirstLetter)\n}",
            expectedOutput = "{a=[apple, avocado], b=[banana, blueberry], c=[cherry]}"
        ),
        Challenge(
            id = 25,
            title = "Partition",
            description = "Разделение коллекции по предикату",
            initialCode = "fun main() {\n    val numbers = listOf(1, 2, 3, 4, 5, 6)\n    val (evens, odds) = numbers.partition { it % 2 == 0 }\n    println(\"Evens: \$evens\")\n    println(\"Odds: \$odds\")\n}",
            expectedOutput = "Evens: [2, 4, 6]\nOdds: [1, 3, 5]"
        ),
        Challenge(
            id = 26,
            title = "Fold",
            description = "Свёртка коллекции",
            initialCode = "fun main() {\n    val numbers = listOf(1, 2, 3, 4, 5)\n    val sum = numbers.fold(0) { acc, i -> acc + i }\n    println(sum)\n}",
            expectedOutput = "15"
        ),
        Challenge(
            id = 27,
            title = "Compound tasks",
            description = "Составная задача на коллекции",
            initialCode = "fun main() {\n    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)\n    val result = numbers.filter { it % 2 == 0 }.map { it * 10 }.take(3)\n    println(result)\n}",
            expectedOutput = "[20, 40, 60]"
        ),
        Challenge(
            id = 28,
            title = "Get used to new style",
            description = "Современный стиль Kotlin",
            initialCode = "fun main() {\n    val map = mapOf(1 to \"One\", 2 to \"Two\", 3 to \"Three\")\n    for ((key, value) in map) {\n        println(\"\$key -> \$value\")\n    }\n}",
            expectedOutput = "1 -> One\n2 -> Two\n3 -> Three"
        ),
        Challenge(
            id = 29,
            title = "Inheritance",
            description = "Наследование классов",
            initialCode = "open class Animal(val name: String)\nclass Dog(name: String, val breed: String) : Animal(name)\n\nfun main() {\n    val dog = Dog(\"Buddy\", \"Golden Retriever\")\n    println(\"\${dog.name} is a \${dog.breed}\")\n}",
            expectedOutput = "Buddy is a Golden Retriever"
        ),
        Challenge(
            id = 30,
            title = "Properties",
            description = "Свойства и поля класса",
            initialCode = "class Person {\n    var name: String = \"Unknown\"\n    var age: Int = 0\n}\n\nfun main() {\n    val person = Person()\n    person.name = \"Alice\"\n    person.age = 25\n    println(\"\${person.name} is \${person.age} years old\")\n}",
            expectedOutput = "Alice is 25 years old"
        ),
        Challenge(
            id = 31,
            title = "Lazy property",
            description = "Ленивая инициализация свойства",
            initialCode = "class Greeter {\n    val greeting: String by lazy {\n        println(\"Computing greeting...\")\n        \"Hello, Lazy!\"\n    }\n}\n\nfun main() {\n    val greeter = Greeter()\n    println(greeter.greeting)\n    println(greeter.greeting)\n}",
            expectedOutput = "Computing greeting...\nHello, Lazy!\nHello, Lazy!"
        ),
        Challenge(
            id = 32,
            title = "Delegates",
            description = "Делегаты свойств",
            initialCode = "import kotlin.properties.Delegates\n\nclass User {\n    var name: String by Delegates.observable(\"<no name>\") { prop, old, new ->\n        println(\"\$old -> \$new\")\n    }\n}\n\nfun main() {\n    val user = User()\n    user.name = \"Alice\"\n    user.name = \"Bob\"\n}",
            expectedOutput = "<no name> -> Alice\nAlice -> Bob"
        ),
        Challenge(
            id = 33,
            title = "Lateinit",
            description = "Использование lateinit var",
            initialCode = "class MyClass {\n    lateinit var text: String\n\n    fun init() {\n        text = \"Initialized!\"\n    }\n}\n\nfun main() {\n    val obj = MyClass()\n    obj.init()\n    println(obj.text)\n}",
            expectedOutput = "Initialized!"
        ),
        Challenge(
            id = 34,
            title = "Equality",
            description = "Сравнение объектов",
            initialCode = "data class Point(val x: Int, val y: Int)\n\nfun main() {\n    val p1 = Point(1, 2)\n    val p2 = Point(1, 2)\n    println(p1 == p2)\n    println(p1 === p2)\n}",
            expectedOutput = "true\nfalse"
        ),
        Challenge(
            id = 35,
            title = "Operators overloading",
            description = "Перегрузка операторов",
            initialCode = "data class Point(val x: Int, val y: Int) {\n    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)\n}\n\nfun main() {\n    val p1 = Point(1, 2)\n    val p2 = Point(3, 4)\n    println(p1 + p2)\n}",
            expectedOutput = "Point(x=4, y=6)"
        ),
        Challenge(
            id = 36,
            title = "Conventions",
            description = "Соглашения в Kotlin",
            initialCode = "data class Point(val x: Int, val y: Int)\n\noperator fun Point.times(scale: Int): Point = Point(x * scale, y * scale)\n\nfun main() {\n    val p = Point(2, 3)\n    println(p * 5)\n}",
            expectedOutput = "Point(x=10, y=15)"
        ),
        Challenge(
            id = 37,
            title = "Comparison",
            description = "Сравнение и сортировка",
            initialCode = "class Person(val firstName: String, val lastName: String) : Comparable<Person> {\n    override fun compareTo(other: Person): Int = compareValuesBy(this, other, { it.lastName }, { it.firstName })\n}\n\nfun main() {\n    val people = listOf(Person(\"John\", \"Doe\"), Person(\"Jane\", \"Doe\"), Person(\"John\", \"Smith\"))\n    println(people.sorted())\n}",
            expectedOutput = "[Person(firstName=Jane, lastName=Doe), Person(firstName=John, lastName=Doe), Person(firstName=John, lastName=Smith)]"
        ),
        Challenge(
            id = 38,
            title = "Destructuring declarations",
            description = "Деструктурирующие объявления",
            initialCode = "data class Person(val name: String, val age: Int)\n\nfun main() {\n    val (name, age) = Person(\"Alice\", 30)\n    println(\"\$name is \$age years old\")\n}",
            expectedOutput = "Alice is 30 years old"
        ),
        Challenge(
            id = 39,
            title = "Invoke",
            description = "Перегрузка оператора invoke",
            initialCode = "class Greeter(val greeting: String) {\n    operator fun invoke(name: String) {\n        println(\"\$greeting, \$name!\")\n    }\n}\n\nfun main() {\n    val hello = Greeter(\"Hello\")\n    hello(\"Kotlin\")\n}",
            expectedOutput = "Hello, Kotlin!"
        ),
        Challenge(
            id = 40,
            title = "Delegates example",
            description = "Пример использования делегатов свойств",
            initialCode = "import kotlin.properties.ReadWriteProperty\nimport kotlin.reflect.KProperty\n\nclass Example {\n    var p: String by Delegate()\n}\n\nclass Delegate : ReadWriteProperty<Example, String> {\n    var value: String = \"\"\n    override fun getValue(thisRef: Example, property: KProperty<*>): String {\n        println(\"Getting value\")\n        return value\n    }\n    override fun setValue(thisRef: Example, property: KProperty<*>, value: String) {\n        println(\"Setting value to \$value\")\n        this.value = value\n    }\n}\n\nfun main() {\n    val e = Example()\n    println(e.p)\n    e.p = \"NEW\"\n    println(e.p)\n}",
            expectedOutput = "Getting value\n\nSetting value to NEW\nGetting value\nNEW"
        ),
        Challenge(
            id = 41,
            title = "Visible on backstack",
            description = "Видимость переменных",
            initialCode = "fun main() {\n    val greeting = \"Hello, Stack!\"\n    println(greeting)\n}",
            expectedOutput = "Hello, Stack!"
        ),
    )

    fun getAllChallenges(): List<Challenge> = allChallenges
    fun getChallengeById(id: Int): Challenge? = allChallenges.find { it.id == id }
}

class MainViewModel : ViewModel() {
    private val repository = ChallengeRepository()
    fun getChallenges(): List<Challenge> = repository.getAllChallenges()
    fun getChallengeById(id: Int): Challenge? = repository.getChallengeById(id)
}