package jacob.simpson.tinyscript;

public class Main {

    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("No script file specified.");
            System.exit(1);
        }
        System.out.println("Executing script: " + args[0]);
    }
}
