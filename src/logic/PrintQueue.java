package logic;

import models.PrintJob;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class PrintQueue<T extends PrintJob> {
    private static final int MAX_PAPER = 1_000_000;
    private final List<T> jobs = new ArrayList<>();
    private int paper;

    public PrintQueue(int paper) {
        if (paper < 0 || paper > MAX_PAPER) {
            throw new IllegalArgumentException("Запас бумаги должен быть от 0 до 1000000");
        }
        this.paper = paper;
    }

    public boolean add(T job) {
        if (job == null || job.isPrinted()) return false;
        if (jobs.stream().anyMatch(existing -> existing.getId() == job.getId())) return false;
        return jobs.add(job);
    }

    public int getPaper() {
        return paper;
    }

    public boolean restock(int amount) {
        if (amount <= 0 || (long) paper + amount > MAX_PAPER) return false;
        paper += amount;
        return true;
    }

    public Stream<T> getAnalyticsStream() {
        return jobs.stream();
    }

    public List<T> process(Predicate<T> condition, Comparator<T> order, Consumer<T> action) {
        List<T> completed = new ArrayList<>();
        if (condition == null || order == null || action == null) return completed;

        List<T> pending;
        try {
            pending = jobs.stream().filter(job -> !job.isPrinted()).sorted(order).toList();
        } catch (RuntimeException ex) {
            return completed;
        }

        for (T job : pending) {
            boolean accepted;
            try {
                accepted = condition.test(job);
            } catch (RuntimeException ex) {
                continue;
            }
            if (!accepted) continue;

            int required = job.getRequiredSheets();
            if (required > paper) continue;

            try {
                action.accept(job);
            } catch (RuntimeException ex) {
                if (job.isPrinted()) {
                    paper -= required;
                    completed.add(job);
                }
                continue;
            }
            if (!job.isPrinted()) continue;

            paper -= required;
            completed.add(job);
        }
        return completed;
    }
}
