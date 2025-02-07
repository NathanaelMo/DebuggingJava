package Interface;

import dbg.ScriptableDebugger;
import dbg.Test;

public class DebuggerMain {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DebuggerMain <class-to-debug>");
            System.exit(1);
        }

        try {

            // Créer et démarrer le débogueur
            ScriptableDebugger debugger = new ScriptableDebugger();
            debugger.attachTo(Test.class);

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du débogueur: " + e.getMessage());
            System.exit(1);
        }
    }
}
