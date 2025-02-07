package Interface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class DebuggerLauncher {
    private Process debuggerProcess;
    private Class debugClass;

    public DebuggerLauncher(Class debugClass) {
        this.debugClass = debugClass;
    }

    public void startDebuggerProcess() {
        try {
            // Construit la commande pour lancer le processus Java
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            String className = "Interface.DebuggerMain";

            // Création de la commande avec les arguments
            ProcessBuilder builder = new ProcessBuilder(
                    javaBin,
                    "-cp",
                    classpath,
                    className,
                    debugClass.getName()
            );

            // Redirection des erreurs vers la sortie standard
            builder.redirectErrorStream(true);

            // Démarrage du processus
            debuggerProcess = builder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopDebuggerProcess() {
        if (debuggerProcess != null && debuggerProcess.isAlive()) {
            debuggerProcess.destroy();
        }
    }

    public BufferedWriter getProcessWriter() {
        return this.debuggerProcess.outputWriter();
    }

    public BufferedReader getProcessReader() {
        return this.debuggerProcess.inputReader();
    }
}


