package main;

import interfaces.PaperDemand;
import logic.PrintQueue;
import models.DoubleSidedJob;
import models.PrintJob;
import models.SingleSidedJob;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Main {
    private static final int ACTIVE_A_VARIANT = 1;
    private static final int ACTIVE_B_VARIANT = 1;
    private static final int ACTIVE_V_VARIANT = 3;

    private final List<PrintJob> sourceJobs = List.of(
            new SingleSidedJob(101, 11, 5, 1, 5),
            new SingleSidedJob(103, 12, 4, 2, 5),
            new SingleSidedJob(105, 13, 3, 2, 2),
            new SingleSidedJob(108, 14, 6, 1, 3),
            new SingleSidedJob(110, 15, 8, 1, 2),
            new DoubleSidedJob(102, 11, 5, 2, 4),
            new DoubleSidedJob(104, 12, 7, 1, 3),
            new DoubleSidedJob(106, 13, 9, 1, 4),
            new DoubleSidedJob(107, 14, 2, 3, 1),
            new DoubleSidedJob(109, 12, 1, 2, 4)
    );

    private PrintQueue<PrintJob> queue = new PrintQueue<>(14);
    private int n;
    private int secondVariant;
    private int thirdVariant;
    private boolean stage1Done;
    private boolean stage2Done;
    private boolean stage3Done;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            Integer inputN = readInt(scanner, "Введите N (1..36): ", 1, 36);
            if (inputN == null) return;
            n = inputN;

            while (true) {
                System.out.println("\n1. Выполнить задание 1");
                System.out.println("2. Выполнить задание 2");
                System.out.println("3. Выполнить задание 3 и отчёт");
                System.out.println("4. Показать очередь и остаток бумаги");
                System.out.println("5. Выход");
                Integer choice = readInt(scanner, "Выберите пункт (1..5): ", 1, 5);
                if (choice == null || choice == 5) return;
                try {
                    switch (choice) {
                        case 1 -> executeStageOne();
                        case 2 -> executeStageTwo();
                        case 3 -> executeStageThree();
                        case 4 -> showQueue();
                        default -> System.out.println("Неизвестная команда.");
                    }
                } catch (RuntimeException ex) {
                    String message = ex.getMessage();
                    System.out.println("Операция не выполнена" + (message == null || message.isBlank() ? "." : ": " + message));
                }
            }
        }
    }

    private static Integer readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) return null;
            String value = scanner.nextLine().trim();
            int parsed;
            try {
                parsed = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Ошибка: введите целое число.");
                continue;
            }
            if (parsed < min || parsed > max) {
                System.out.println("Ошибка: допустимое значение от " + min + " до " + max + ".");
                continue;
            }
            return parsed;
        }
    }

    private void executeStageOne() {
        if (stage1Done) {
            System.out.println("Этап 1 уже выполнен. Данные не изменены.");
            return;
        }

        int assignedA = StageAccess.initial(n);
        if (assignedA != ACTIVE_A_VARIANT) {
            System.out.println("Для N=" + n + " назначен А" + assignedA + ", а в этом примере активен А" + ACTIVE_A_VARIANT + ".");
            System.out.println("Включи соответствующий блок А, затем запусти цепочку заново.");
            return;
        }

        Predicate<PrintJob> condition = job -> job.getPriority() >= 3 && job.getRequiredSheets() <= 8;

        /* Predicate<PrintJob> condition = job -> job.getCopies() >= 2 && job.getRequiredSheets() <= 12; */
        /* Predicate<PrintJob> condition = job -> job.getPages() % 2 != 0 && job.getPriority() >= 2; */
        /* Predicate<PrintJob> condition = job ->
                (job instanceof DoubleSidedJob && job.getPages() >= 5)
                        || (job instanceof SingleSidedJob && job.getPriority() >= 4); */
        /* Predicate<PrintJob> condition = job -> job.getRequiredSheets() >= 4
                && job.getPriority() >= 3 && job.getCopies() <= 2; */
        /* Predicate<PrintJob> condition = job ->
                (job.getCopies() == 1 && job.getPriority() >= 3)
                        || (job.getCopies() >= 2 && job.getRequiredSheets() <= 4); */

        List<PrintJob> selected = sourceJobs.stream()
                .filter(condition)
                .sorted(Comparator.comparingInt(PrintJob::getId))
                .toList();
        List<Integer> ids = selected.stream().map(PrintJob::getId).toList();
        int sheets = selected.stream().mapToInt(PaperDemand::getRequiredSheets).sum();
        int assignedB = StageAccess.second(n, ids, sheets);

        PrintQueue<PrintJob> candidate = new PrintQueue<>(14);
        boolean allAdded = selected.stream().allMatch(candidate::add);
        if (!allAdded) {
            System.out.println("Не удалось сформировать очередь. Исходные данные не изменены.");
            return;
        }

        queue = candidate;
        secondVariant = assignedB;
        stage1Done = true;
        System.out.println("Добавлены ID: " + ids);
        System.out.println("Суммарная потребность: " + sheets + " листов");
        System.out.println("Вариант задания 2: Б" + secondVariant);
    }

    private void executeStageTwo() {
        if (!stage1Done) {
            System.out.println("Сначала выполни задание 1.");
            return;
        }
        if (stage2Done) {
            System.out.println("Этап 2 уже выполнен. Данные не изменены.");
            return;
        }
        if (secondVariant != ACTIVE_B_VARIANT) {
            System.out.println("StageAccess назначил Б" + secondVariant + ", а в этом примере активен Б" + ACTIVE_B_VARIANT + ".");
            System.out.println("Включи соответствующий блок Б и запусти всю цепочку заново.");
            return;
        }

        List<PrintJob> printedThisCall = processByActiveB();
        stage2Done = true;
        System.out.println("Выполнены ID: " + printedThisCall.stream().map(PrintJob::getId).toList());
        System.out.println("Остаток бумаги: " + queue.getPaper());

        thirdVariant = StageAccess.third(n, printedThisCall.stream().map(PrintJob::getId).toList(), queue.getPaper());
        System.out.println("Вариант задания 3: В" + thirdVariant);
    }

    private List<PrintJob> processByActiveB() {
        if (ACTIVE_B_VARIANT == 1) {
            Predicate<PrintJob> condition = job -> true;
            Comparator<PrintJob> order = Comparator.comparingInt(PrintJob::getPriority).reversed()
                    .thenComparingInt(PrintJob::getRequiredSheets)
                    .thenComparingInt(PrintJob::getId);
            Consumer<PrintJob> action = PrintJob::markPrinted;
            return queue.process(condition, order, action);
        }

        /*
        if (ACTIVE_B_VARIANT == 2) {
            Predicate<PrintJob> condition = job -> true;
            Comparator<PrintJob> order = Comparator.comparingInt(PrintJob::getPriority).reversed()
                    .thenComparing(Comparator.comparingInt(PrintJob::getRequiredSheets).reversed())
                    .thenComparingInt(PrintJob::getId);
            Consumer<PrintJob> action = PrintJob::markPrinted;
            return queue.process(condition, order, action);
        }

        if (ACTIVE_B_VARIANT == 3) {
            Map<Integer, Integer> ownerCount = new HashMap<>();
            Predicate<PrintJob> condition = job -> ownerCount.getOrDefault(job.getOwnerId(), 0) < 1;
            Comparator<PrintJob> order = Comparator.comparingInt(PrintJob::getPriority).reversed()
                    .thenComparingInt(PrintJob::getId);
            Consumer<PrintJob> action = job -> {
                job.markPrinted();
                ownerCount.merge(job.getOwnerId(), 1, Integer::sum);
            };
            return queue.process(condition, order, action);
        }

        if (ACTIVE_B_VARIANT == 4) {
            int reserve = (queue.getPaper() + 3) / 4;
            Predicate<PrintJob> condition = job -> queue.getPaper() - job.getRequiredSheets() >= reserve;
            Comparator<PrintJob> order = Comparator.comparingInt(PrintJob::getPriority).reversed()
                    .thenComparingInt(PrintJob::getRequiredSheets)
                    .thenComparingInt(PrintJob::getId);
            Consumer<PrintJob> action = PrintJob::markPrinted;
            return queue.process(condition, order, action);
        }

        if (ACTIVE_B_VARIANT == 5) {
            Map<Integer, Integer> ownerCount = new HashMap<>();
            Predicate<PrintJob> condition = job -> ownerCount.getOrDefault(job.getOwnerId(), 0) < 2;
            Comparator<PrintJob> order = Comparator.comparingInt(PrintJob::getRequiredSheets)
                    .thenComparing(Comparator.comparingInt(PrintJob::getPriority).reversed())
                    .thenComparingInt(PrintJob::getId);
            Consumer<PrintJob> action = job -> {
                job.markPrinted();
                ownerCount.merge(job.getOwnerId(), 1, Integer::sum);
            };
            return queue.process(condition, order, action);
        }

        if (ACTIVE_B_VARIANT == 6) {
            Map<Integer, Long> previousPrints = queue.getAnalyticsStream()
                    .filter(PrintJob::isPrinted)
                    .collect(Collectors.groupingBy(PrintJob::getOwnerId, Collectors.counting()));
            Predicate<PrintJob> condition = job -> true;
            Comparator<PrintJob> order = Comparator
                    .comparingLong((PrintJob job) -> previousPrints.getOrDefault(job.getOwnerId(), 0L))
                    .thenComparing(Comparator.comparingInt(PrintJob::getPriority).reversed())
                    .thenComparingInt(PrintJob::getId);
            Consumer<PrintJob> action = PrintJob::markPrinted;
            return queue.process(condition, order, action);
        }
        */

        return List.of();
    }

    private void executeStageThree() {
        if (!stage2Done) {
            System.out.println("Сначала успешно выполни задание 2.");
            return;
        }
        if (stage3Done) {
            System.out.println("Этап 3 уже выполнен. Данные не изменены.");
            return;
        }
        if (thirdVariant != ACTIVE_V_VARIANT) {
            System.out.println("StageAccess назначил В" + thirdVariant + ", а в этом примере активен В" + ACTIVE_V_VARIANT + ".");
            System.out.println("Включи соответствующий блок В и запусти всю цепочку заново.");
            return;
        }

        int addedSheets = 0;
        if (ACTIVE_V_VARIANT == 3) addedSheets = 6;

        /*
        if (ACTIVE_V_VARIANT == 1) addedSheets = 4;
        if (ACTIVE_V_VARIANT == 2) addedSheets = 5;
        if (ACTIVE_V_VARIANT == 4) addedSheets = 7;
        if (ACTIVE_V_VARIANT == 5) addedSheets = 8;
        if (ACTIVE_V_VARIANT == 6) addedSheets = 9;
        */

        if (!queue.restock(addedSheets)) {
            System.out.println("Не удалось пополнить бумагу. Данные не изменены.");
            return;
        }

        List<PrintJob> printedThisCall = processByActiveB();
        System.out.println("Добавлено листов: " + addedSheets);
        System.out.println("Новые выполненные ID: " + printedThisCall.stream().map(PrintJob::getId).toList());
        System.out.println("Остаток бумаги: " + queue.getPaper());
        printThirdReport();
        stage3Done = true;
    }

    private void printThirdReport() {
        if (ACTIVE_V_VARIANT == 3) {
            Optional<PrintJob> report = queue.getAnalyticsStream()
                    .filter(job -> !job.isPrinted() && job.getRequiredSheets() <= queue.getPaper())
                    .max(Comparator.comparingInt(PrintJob::getRequiredSheets)
                            .thenComparingInt(PrintJob::getPriority)
                            .thenComparing(Comparator.comparingInt(PrintJob::getId).reversed()));
            System.out.println("Отчёт В3: " + report.map(PrintJob::getDetails).orElse("подходящих ожидающих заданий нет"));
            return;
        }

        /*
        if (ACTIVE_V_VARIANT == 1) {
            Map<Integer, Long> report = queue.getAnalyticsStream()
                    .filter(PrintJob::isPrinted)
                    .collect(Collectors.groupingBy(PrintJob::getOwnerId,
                            Collectors.summingLong(PrintJob::getRequiredSheets)));
            System.out.println("Отчёт В1: " + report);
            return;
        }

        if (ACTIVE_V_VARIANT == 2) {
            Map<Integer, Map<Boolean, List<PrintJob>>> byOwner = queue.getAnalyticsStream()
                    .collect(Collectors.groupingBy(PrintJob::getOwnerId,
                            Collectors.partitioningBy(PrintJob::isPrinted)));
            List<Integer> report = byOwner.entrySet().stream()
                    .filter(entry -> !entry.getValue().get(true).isEmpty() && !entry.getValue().get(false).isEmpty())
                    .map(Map.Entry::getKey).sorted().toList();
            System.out.println("Отчёт В2: " + report);
            return;
        }

        if (ACTIVE_V_VARIANT == 4) {
            Map<String, Long> report = queue.getAnalyticsStream()
                    .filter(PrintJob::isPrinted)
                    .collect(Collectors.groupingBy(job -> job.getClass().getSimpleName(),
                            Collectors.summingLong(job -> (long) job.getPages() * job.getCopies())));
            report.putIfAbsent("SingleSidedJob", 0L);
            report.putIfAbsent("DoubleSidedJob", 0L);
            System.out.println("Отчёт В4: " + report);
            return;
        }

        if (ACTIVE_V_VARIANT == 5) {
            Optional<Integer> report = queue.getAnalyticsStream()
                    .filter(job -> !job.isPrinted())
                    .collect(Collectors.groupingBy(PrintJob::getOwnerId,
                            Collectors.summingLong(PrintJob::getRequiredSheets)))
                    .entrySet().stream()
                    .max(Comparator.comparingLong((Map.Entry<Integer, Long> entry) -> entry.getValue())
                            .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                    .map(Map.Entry::getKey);
            System.out.println("Отчёт В5: " + report);
            return;
        }

        if (ACTIVE_V_VARIANT == 6) {
            Map<Integer, Double> report = queue.getAnalyticsStream()
                    .collect(Collectors.groupingBy(PrintJob::getOwnerId,
                            Collectors.collectingAndThen(Collectors.toList(), ownerJobs ->
                                    (double) ownerJobs.stream().filter(PrintJob::isPrinted).count() / ownerJobs.size())));
            System.out.println("Отчёт В6: " + report);
        }
        */
    }

    private void showQueue() {
        System.out.println("Остаток бумаги: " + queue.getPaper());
        List<PrintJob> current = queue.getAnalyticsStream()
                .sorted(Comparator.comparingInt(PrintJob::getId))
                .toList();
        if (current.isEmpty()) {
            System.out.println("Очередь пуста.");
            return;
        }
        current.forEach(job -> System.out.println(job.getDetails()));
    }
}

final class StageAccess {
    private static final int[] S = {5, 3, 2, 4, 2, 1};
    private static final int[] A = {
        0xcb89387d, 0x4626b7fe, 0xacac2431, 0x4997263f, 0x6b20cc2b,
        0x209a21ae
    };
    private static final int[] B = {
        0x6128024d, 0x38a00d09, 0x38a2c749, 0x2ad3a538, 0x38a00d09,
        0x38a00d09, 0x612876ab, 0x612ba548, 0xe7a1669a, 0x612876ab,
        0xc4f9d0bb, 0x612876ab, 0xf3601d34, 0x013dc91f, 0xf3601d34,
        0x4471d939, 0x38a2c749, 0x2ad3a538, 0x6128024d, 0x38a00d09,
        0x38a16a29, 0x04b3adf2, 0x38a00d09, 0x38a00d09, 0x38a00d09,
        0x38a00d09, 0x6128024d, 0x38a00d09, 0x38a16a29, 0x04b3adf2,
        0x013dc91f, 0x4471d939, 0x4471d939, 0x013e3d80, 0x0d1d0f0f,
        0x4471d939
    };
    static int initial(int n) {
        if (n < 1 || n > 36)
            throw new IllegalArgumentException("N: 1..36");
        return (n - 1) / 6 + 1;
    }
    static int second(int n, java.util.List<Integer> ids, int sheets) {
        int a = initial(n);
        return check(n, ids, sheets, A[a - 1], 0);
    }
    static int third(int n, java.util.List<Integer> ids, int paper) {
        int a = initial(n);
        int b = 1 + Math.floorMod((long) A[a - 1] + (n - 1) % 6, 6);
        return check(n, ids, paper, B[n - 1], b);
    }
    private static int check(int n, java.util.List<Integer> ids,
                             int value, int expected, int previous) {
        if (ids == null) throw new IllegalArgumentException("ids: null");
        int actual = (ids + "|" + value).hashCode();
        if (actual != expected)
            throw new IllegalStateException("Check stage result");
        long key = previous == 0 ? actual
            : (actual >>> 6) + 0L * previous
            + (1 - 1L) * ((n - 1) % 6) + S[initial(n) - 1];
        return 1 + Math.floorMod(key + (n - 1) % 6, 6);
    }
}
