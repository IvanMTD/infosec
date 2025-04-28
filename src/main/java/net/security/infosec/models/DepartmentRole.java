package net.security.infosec.models;

public enum DepartmentRole {
    IB("Информационная безопасность"), IT("Техническая поддержка"), ALL("Мониторинг");
    private final String title;

    DepartmentRole(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
