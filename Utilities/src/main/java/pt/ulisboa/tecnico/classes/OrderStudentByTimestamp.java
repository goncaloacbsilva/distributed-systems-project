package pt.ulisboa.tecnico.classes;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;

import java.util.Comparator;

public class OrderStudentByTimestamp implements Comparator<ClassesDefinitions.Student> {
    @Override
    public int compare(ClassesDefinitions.Student o1, ClassesDefinitions.Student o2) {
        return Long.compare(o1.getLastChange().getSeconds(), o2.getLastChange().getSeconds());
    }
}
