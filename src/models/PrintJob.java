package models;

import interfaces.PaperDemand;

public abstract class PrintJob implements PaperDemand {
    private final int id;
    private final int ownerId;
    private final int pages;
    private final int copies;
    private final int priority;
    private boolean printed;

    protected PrintJob(int id, int ownerId, int pages, int copies, int priority) {
        if (id <= 0) throw new IllegalArgumentException("ID должен быть положительным");
        if (ownerId <= 0) throw new IllegalArgumentException("ID владельца должен быть положительным");
        if (pages < 1 || pages > 100) throw new IllegalArgumentException("Число страниц должно быть от 1 до 100");
        if (copies < 1 || copies > 100) throw new IllegalArgumentException("Число копий должно быть от 1 до 100");
        if (priority < 1 || priority > 5) throw new IllegalArgumentException("Приоритет должен быть от 1 до 5");
        this.id = id;
        this.ownerId = ownerId;
        this.pages = pages;
        this.copies = copies;
        this.priority = priority;
        this.printed = false;
    }

    public final int getId() {
        return id;
    }

    public final int getOwnerId() {
        return ownerId;
    }

    public final int getPages() {
        return pages;
    }

    public final int getCopies() {
        return copies;
    }

    public final int getPriority() {
        return priority;
    }

    public final boolean isPrinted() {
        return printed;
    }

    public final void markPrinted() {
        printed = true;
    }

    public abstract String getDetails();

    protected final String baseDetails() {
        return "id=" + id + ", ownerId=" + ownerId + ", pages=" + pages
                + ", copies=" + copies + ", priority=" + priority
                + ", requiredSheets=" + getRequiredSheets()
                + ", status=" + (printed ? "напечатано" : "ожидает");
    }
}
