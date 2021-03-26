package entity;

import annotation.CSVEntity;
import annotation.CSVField;

@CSVEntity
public class Person {

    @CSVField
    private String name;
    @CSVField
    private String surname;

    public Person(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
