package dbg;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private String name = "test";
    private int counter = 0;
    private double value = 3.14;

    public void doSomething(List<Integer> tableau, String toto, int titi) {
        counter++;
        System.out.println(name);
        System.out.println(counter);
        System.out.println(value);
        for (Integer integer : tableau) {
            System.out.println(integer);
        }
        value = value * 2;
        for(int j = 0;j < 5;j++){
            System.out.println(j);
        }
        doSomethingElse(42);
    }

    public void doSomethingElse(int i) {
        System.out.println("doSomethingElse");
        System.out.println(i);
    }

    public static void main(String[] args) {
        Test demo = new Test();
        List<Integer>tableau = new ArrayList<>();
        tableau.add(1);
        tableau.add(2);
        tableau.add(3);
        demo.doSomething(tableau, "toto", 42);
    }
}