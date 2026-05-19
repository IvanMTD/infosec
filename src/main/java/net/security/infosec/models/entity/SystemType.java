package net.security.infosec.models.entity;

public enum SystemType {
    INTERNAL("Внутренняя"),
    EXTERNAL("Внешняя");

    private final String title;

    SystemType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
