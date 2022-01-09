import kotlin.math.abs

const val EXIT = "exit"
const val INPUT_REGEX = "[a-h][1-8][a-h][1-8]"

const val WHITE = 'W'
const val BLACK = 'B'
const val SPACE = ' '

const val DIGIT_START = 0
const val LITER_START = 1
const val DIGIT_END = 2
const val LITER_END = 3


var firstPlayer =""
var secondPlayer =""
val board = mutableListOf(
    MutableList<Char>(8) { SPACE },
    MutableList<Char>(8) { BLACK },
    MutableList<Char>(8) { SPACE },
    MutableList<Char>(8) { SPACE },
    MutableList<Char>(8) { BLACK },
    MutableList<Char>(8) { SPACE },
    MutableList<Char>(8) { WHITE },
    MutableList<Char>(8) { SPACE }
)

var start = ' '                                               // Выносим переменные для использования везде
var end = ' '
var step = arrayOf(0, 0, 0, 0)
var enter = ""
var myColor = 'W'
var oppColor = 'B'
var color = ""
var vector = 0
var startingPosition = 0

var passedPawn = arrayOf(8, 8)                                  // проходнаяПешка
var aPassedPawnIsBeingCaptured = false                          // идётВзятиеПроходнойПешки

fun main() {
    greetings()                                                 // Приветствие
    game()                                                      // Игра
}

fun greetings() {                                               // Приветствие
    println(" Pawns-Only Chess")

    println("First Player's name:")                             // Представление первого игрока
    firstPlayer = readLine()!!

    println("Second Player's name:")                            // Представление второго игрока
    secondPlayer = readLine()!!
}

fun game() {            // Игра()
    val regex = Regex(INPUT_REGEX)
    var player = firstPlayer                                     // первым ходит первый игрок

    field()                                                      // Построить поле

    do {
        if (isPat(player)) {                                     // Пат?() - проверяем перед ходом, чтобы сменился игрок
            println("Stalemate!")                                // и можно было пользоваться всеми нужными методами, а не
            break                                                // писать новые
        }

        println("$player's turn:")                               // представление игрока
        enter = readLine()!!                                     // считывание его хода

        if (enter.matches(regex)) {                              // если ячейки названы верно
            while (move(player)) {                               // делаем ход

                if (player == firstPlayer) {                     // меняем игрока
                    player = secondPlayer
                    break                                        // обрываем цикл while (move(player))
                } else {
                    player = firstPlayer
                    break                                        // обрываем цикл while (move(player))
                }
            }
        } else if (!enter.matches(regex) && enter != EXIT) {     // если ход введен с ошибкой
            println("Invalid Input")
        }
    } while (enter != EXIT)                                      // пока не введено "Выход"

    println("Bye!")
}

fun move(player: String): Boolean {                              // Ход()
    step = parseStep()                                           // Разделяем введенный ход в массив
    start = board[step[DIGIT_START]][step[LITER_START]]          // Начальная клетка
    end = board[step[DIGIT_END]][step[LITER_END]]                // Конечная клетка

    startingPosition = if (player == firstPlayer) 6 else 1       // Сторона доски для делающего ход игрока
    myColor = if (player == firstPlayer) WHITE else BLACK        // Цвет фигур для делающего ход игрока
    oppColor = if (player == firstPlayer) BLACK else WHITE       // Цвет фигур противника
    vector = if (player == firstPlayer) 1 else -1                // Направление хода для делающего ход игрока
    color = if (player == firstPlayer) "white" else "black"      // Строковый вывод цвета фигур делающего ход игрока

    when {                                                                   // Когда
        // 1. Неверный ход
        start != myColor -> println("No $color pawn at ${enter.substring(0, 2)}") // Нет своей пешки для хода
        // 2. Неверный ход
        wrongMoveToTheField() -> invalid()                                   // неверныйХодВПоле()
        // 3. Неверный ход
        wrongMoveFromTheFirstLine() -> invalid()                             // неверныйХодСПервойЛинии()
        // 4. Неверный ход
        moveInPlaceOrBackward() -> invalid()                                 // ходНаМестеИлиНазад()
        // 5. Обычный ход
        normalMove() -> {                                                    // обычныйХод()
            movingPawn()                                                     // вызываем передвижениеПешки()
            isWins()                                                         // победа?()
            return true                                                      // возврат тру, чтобы остаться в цикле вайл
        }                                                                    // и сменить игрока
        // 6. Взятие пешки противника
        capturingTheOpponentPawn() -> {                                      // взятиеПешкиПротивника()
            movingPawn()                                                     // вызываем передвижениеПешки()
            isWins()                                                         // победа?()
            return true                                                      // возврат тру, чтобы остаться в цикле вайл
        }                                                                    // и сменить игрока
        // 7. Взятие проходной пешки противника
        capturingAnOpponentPassedPawn() -> {                                 // взятиеПроходнойПешкиПротивника()
            aPassedPawnIsBeingCaptured = true                                // Ставим флаг идётВзятиеПроходной Пешки в true
            movingPawn()                                                     // вызываем передвижениеПешки()
            aPassedPawnIsBeingCaptured = false                               // возвращаем флаг в false
            isWins()                                                         // победа?()
            return true                                                      // возврат тру, чтобы остаться в цикле вайл
        }                                                                    // и сменить игрока
        // 8. Неверный ход
        (step[LITER_START] != step[LITER_END]) -> invalid()                  // Буква начала хода не равна букве окончания хода
                                                                             // т.е. ход без взятия должен быть по прямой

        else -> invalid()
    }
    return false                                                             // возврат false, чтобы остаться в цикле,
}                                                                            // не меняя игрока, т.к. ход был неверный

fun normalMove() =                                                           // обычныйХод()
    (start == myColor && end != oppColor &&                                  // Есть своя пешка на начальной клетке и
                                                                             // нет пешки противника на конечной клетке и
     board[step[DIGIT_START ] - vector][step[LITER_START]] != oppColor &&     // нет пешки противника на клетке перед, если ход
                                                                             // через клетку, чтобы не перескочить
     step[LITER_END] == step[LITER_START])                                   // и ход идет по прямой

fun capturingTheOpponentPawn() =                                             // взятиеПешкиПротивника()
    start == myColor && end == oppColor                                      // На стартовой клетке есть своя и на конечной
                                                                             // есть противник
            && abs(step[LITER_START] - step[LITER_END]) == 1              // и противник на соседней рядом букве
            && (step[DIGIT_START] - step[DIGIT_END]) * vector == 1           // и ход равен одной клетке

fun capturingAnOpponentPassedPawn() =                                        // взятиеПроходнойПешкиПротивника()
    (start == myColor && end != oppColor && oppPawnNearby()  &&              // На стартовой клетке есть своя и на конечной
                                                                             // есть противник и пешка соперника рядом и
    step[DIGIT_END] == passedPawn[DIGIT_START] - vector &&                   // номер конечной клетки на 1 меньше проходнойПешки и
    step[LITER_END] == passedPawn[LITER_START])                              // буква конечной клетки совпадает с проходнойПешкой

fun movingPawn () {                                                          // перемещениеПешки()
    board[step[DIGIT_START]][step[LITER_START]] = SPACE                      // начальную клетку обнуляем
    board[step[DIGIT_END]][step[LITER_END]] = myColor                        // в конечную клетку прописываем цвет игрока

    if (aPassedPawnIsBeingCaptured) {                                        // Если идётВзятиеПроходнойПешки, то
        board[passedPawn[DIGIT_START]][passedPawn[LITER_START]] = SPACE      // Обозначение пешки стираем с доски
    }

    field()                                                                  // обновляем игровое поле

    if (abs(step[DIGIT_START] - step[DIGIT_END]) == 2) {                  // Условие определения проходной пешки
        passedPawn[DIGIT_START] = step[DIGIT_END]                            // и назначения ей данных
        passedPawn[LITER_START] = step[LITER_END]
    } else {                                                                 // иначе назначаем данные вне поля, т.е. её нет
        passedPawn[DIGIT_START] = 8
        passedPawn[LITER_START] = 8
    }
}

fun isWins() {                                                        // победа?()
    when {
        isAllOppositePawnsAreCaptured() -> wins()                     // всеПешкиПротивникаВзяты?()
        hasPawnOnTheLastOppositeLine() -> wins()                      // естьПешкаНаПоследнейПротивоположнойЛинии?()
    }
}
fun wins() {                                                          // победа()
    println("${color.capitalize()} Wins!")
    enter = EXIT
}


fun isAllOppositePawnsAreCaptured(): Boolean {                         // всеПешкиПротивникаВзяты?()
    var count = 0
    for (i in 0..7) {
        for (j in 0..7) {
            if (board[i][j] == oppColor) count ++                      // Если на доске пешка противника есть ++
        }
    }
    return count == 0                                                  // Если count == 0, значит пешек нет: true
}

fun hasPawnOnTheLastOppositeLine() =                                   // естьПешкаНаПоследнейПротивоположнойЛинии?()
    step[DIGIT_END] == 0 || step[DIGIT_END] == 7

fun isPat(player: String): Boolean {                                   // пат?()
    myColor = if (player == firstPlayer) WHITE else BLACK              // определяем цвета, т.к. они еще не поменялись
    oppColor = if (player == firstPlayer) BLACK else WHITE             // от прежнего игрока
    var pawn = 0                                                       // счётчики всех пешек
    var patPawn = 0                                                    // и патовых пешек

    for (i in 0..7) {                                                  // перебираем все поле
        for (j in 0..7) {
            if (board[i][j] == myColor) {                              // находим свой цвет
                if (isNormalMove(i, j, player) || isCapturingTheOpponentPawn(i, j, player)) { // проверяем
                    pawn++                                             // если успешно, патовые пешки не ++
                } else {
                    pawn++
                    patPawn++
                }
            }
        }
    }
    return pawn == patPawn
}

fun isNormalMove(i: Int, j: Int, player: String): Boolean {            // нормальныйХод?()          
    vector = if (player == firstPlayer) 1 else -1                      // проверяем простой ход вперед
    step = arrayOf(i, j, i - vector, j)                                // Определяем переменные. т.к. они тоже от старого игрока еще
    start = board[step[DIGIT_START]][step[LITER_START]]                // Изменяем конечную клетку на 1 вручную (vector)        
    end = board[step[DIGIT_END]][step[LITER_END]]                      // для каждого игрока будет свой знак направления

    return normalMove()
}

fun isCapturingTheOpponentPawn(i: Int, j: Int, player: String): Boolean { // возможноВзятиеПешкиПротивника?()
    var pawn = 0                                                        // Проверяем возможные ходы не по прямой
    var patPawn = 0
    if (j != 7) {                                                       // Проверяем отдельно в двух условиях крайние 
        vector = if (player == firstPlayer) 1 else -1                   // клетки, чтобы не выйти за пределы массива
        step = arrayOf(i, j, i - vector, j + 1)
        start = board[step[DIGIT_START]][step[LITER_START]]          
        end = board[step[DIGIT_END]][step[LITER_END]]
        if (capturingTheOpponentPawn() || capturingAnOpponentPassedPawn()) {
            pawn++
        } else {
            pawn++
            patPawn++
        }
    }
    if(j != 0) {
        vector = if (player == firstPlayer) 1 else -1
        step = arrayOf(i, j, i - vector, j - 1)
        start = board[step[DIGIT_START]][step[LITER_START]]          // Начальная клетка
        end = board[step[DIGIT_END]][step[LITER_END]]
        if (capturingTheOpponentPawn() || capturingAnOpponentPassedPawn()) {
            pawn++
        } else {
            pawn++
            patPawn++
        }
    }
    return pawn != patPawn
}

fun oppPawnNearby ():Boolean {                                         // пешкаСоперникаПоблизости()
    return abs(step[LITER_START] - passedPawn[LITER_START]) == 1    // определение соседней буквы для проходной пешки
            && step[DIGIT_START] == passedPawn[DIGIT_START]            // и номера клетки такого же, как и у делающей ход пешки
}                                                                      // т.е. проходная пешка противника стоит рядом, можно бить

fun wrongMoveFromTheFirstLine() =                                      // неверныйХодСПервойЛинии()
    step[DIGIT_START] == startingPosition &&                           // Пешка в первом ряду (начальное положение)
    (step[DIGIT_START] - step[DIGIT_END]) * vector > 2                 // и ход более 2 клеток

fun wrongMoveToTheField() =                                            // неверныйХодВПоле()
    step[DIGIT_START] != startingPosition &&                           // Не первый ряд (в игре)
    (step[DIGIT_START] - step[DIGIT_END]) * vector > 1                 // и ход более 1 клетки

fun moveInPlaceOrBackward() =                                          // ходНаМестеИлиНазад()
    (step[DIGIT_START] - step[DIGIT_END]) * vector < 1

fun invalid() {                                                        // неверныйХод()
    println("Invalid Input")
}

fun parseStep(): Array<Int> {                                           // разделениеХода()
    val startCell = enter.substring(0, 2)                               // выделяем из ввода стартовую ячейку
    val endCell = enter.substring(2)                           // выделяем из ввода конечную ячейку
    return cellTransformation(startCell) + cellTransformation(endCell)  // возвращаем преобразованный ввод игрока с индексами
    // массива игрового поля вместо обозначений доски
}

fun field() {                                                    // Поле()
    val line: String = "+---".repeat(8) + "+"

    var count = 8
    for (i in 0..7) {
        println("  $line")                                       // разделительная полоса

        print("${count--} ")
        for (j in 0..7) {
            print("| ${board[i][j]} ")                           // игровые ячейки
        }
        println("|")                                             // правый край поля
    }

    println("  $line")                                           // нижняя полоса
    println("    a   b   c   d   e   f   g   h")
}

fun cellTransformation(cell: String): Array<Int> {              // преобразованиеКлеток()
                                                                // Преобразовываем ввод игрока в индексы массива игрового поля
    val last = when (cell.first()) {                            // Преобразование буквы в индекс массива игрового поля
        'a' -> 0
        'b' -> 1
        'c' -> 2
        'd' -> 3
        'e' -> 4
        'f' -> 5
        'g' -> 6
        else -> 7
    }

    val first = when (cell.last().toString().toInt()) {         // Преобразование номера в индекс массива игрового поля
        8 -> 0
        7 -> 1
        6 -> 2
        5 -> 3
        4 -> 4
        3 -> 5
        2 -> 6
        else -> 7
    }
    return arrayOf(first, last)                                 // возвращаем преобразованную ячейку в элемент массива игрового поля
}


