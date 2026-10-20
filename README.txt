Java 17+.

Папки и пакеты соответствуют структуре из задания.
StageAccess вставлен в конец Main.java после класса Main.
Бонусное задание с файлами и сериализацией не реализовано.

Пример текущей конфигурации:
N = 6, А1, Б1, В3.

Сборка из корневой папки проекта:
javac -encoding UTF-8 -d out src/interfaces/PaperDemand.java src/models/PrintJob.java src/models/SingleSidedJob.java src/models/DoubleSidedJob.java src/logic/PrintQueue.java src/main/Main.java

Запуск:
java -cp out main.Main

В Main.java активны А1, Б1 и В3. Остальные правила оставлены закомментированными.
При выборе другого правила проверь ACTIVE_A_VARIANT, ACTIVE_B_VARIANT и ACTIVE_V_VARIANT, а затем раскомментируй соответствующий блок. После изменения варианта перезапускай всю цепочку с исходными данными.
