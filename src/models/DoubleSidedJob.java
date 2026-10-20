package models;

public final class DoubleSidedJob extends PrintJob {
    public DoubleSidedJob(int id, int ownerId, int pages, int copies, int priority) {
        super(id, ownerId, pages, copies, priority);
    }

    @Override
    public int getRequiredSheets() {
        return ((getPages() + 1) / 2) * getCopies();
    }

    @Override
    public String getDetails() {
        return "DoubleSidedJob{" + baseDetails() + "}";
    }
}
