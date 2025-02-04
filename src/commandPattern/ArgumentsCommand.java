package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class ArgumentsCommand implements Command {
    private final LocatableEvent event;

    public ArgumentsCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            // Récupérer la frame courante
            StackFrame currentFrame = event.thread().frame(0);
            Method method = currentFrame.location().method();

            // Récupérer la liste des arguments
            List<LocalVariable> arguments = method.arguments();

            if (arguments != null && !arguments.isEmpty()) {
                System.out.println("Method arguments:");

                // Pour chaque argument, récupérer sa valeur
                for (LocalVariable arg : arguments) {
                    Value value = currentFrame.getValue(arg);
                    System.out.println(arg.name() + " → " + value);
                }
            } else {
                System.out.println("No arguments for this method");
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (AbsentInformationException e) {
            System.err.println("Error: Debug information not available");
        } catch (Exception e) {
            System.err.println("Error getting arguments: " + e.getMessage());
        }
    }
}