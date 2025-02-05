package dbg;

public class JDISimpleDebuggee {
    int power = 2;

    public static void main(String[] args) {
        String description = "Simple power printer";
        System.out.println(description + " -- starting");
        int x = 40;
        JDISimpleDebuggee test = new JDISimpleDebuggee();
        printPower(x, test.getPower());
        System.out.println(test.addition());
    }

    public static double power(int x, int power) {
        double powerX = Math.pow(x, power);
        return powerX;
    }

    public static void printPower(int x, int power) {
        double powerX = power(x, power);
        System.out.println(powerX);
    }

    private int addition(){
        int i = this.getPower();
        return ++i;
    }
    public int getPower(){
        return this.power;
    }
}