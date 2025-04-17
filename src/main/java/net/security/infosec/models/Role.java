package net.security.infosec.models;

public enum Role {
    WORKER("Сотрудник"),MANAGER("Управляющий"),DIRECTOR("Контролирующий"),ADMIN("Администратор"),GUIDE_ADMIN("Администратор справочника");

    private final String title;

    Role(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }
}
