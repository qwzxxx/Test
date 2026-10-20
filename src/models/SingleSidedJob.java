package models;

public final class SingleSidedJob extends PrintJob {
    public SingleSidedJob(int id, int ownerId, int pages, int copies, int priority) {
        super(id, ownerId, pages, copies, priority);
    }

    @Override
    public int getRequiredSheets() {
        return getPages() * getCopies();
    }

    @Override
    public String getDetails() {
        return "SingleSidedJob{" + baseDetails() + "}";
    }
}
