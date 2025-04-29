package net.security.infosec.models;

public enum DepartmentRole {
    ALL("Все",0), IB("Информационная безопасность",-2), IT("Техническая поддержка",-1);
    private final String title;
    private final int type;

    DepartmentRole(String title, int type) {
        this.title = title;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }
}
