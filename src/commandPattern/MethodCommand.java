package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class MethodCommand implements Command {
    private final LocatableEvent event;

    public MethodCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            // Récupérer la frame courante
            StackFrame currentFrame = event.thread().frame(0);
            Location location = currentFrame.location();
            Method method = location.method();

            // Afficher les informations de la méthode
            System.out.println("Current method information:");
            System.out.println("Name: " + method.name());
            System.out.println("Declaring class: " + method.declaringType().name());
            System.out.println("Return type: " + method.returnTypeName());
            System.out.println("Is static: " + method.isStatic());

            // Afficher les arguments de la méthode
            List<LocalVariable> arguments = method.arguments();
            if (arguments != null && !arguments.isEmpty()) {
                System.out.println("Arguments:");
                for (LocalVariable arg : arguments) {
                    System.out.println("  - " + arg.typeName() + " " + arg.name());
                }
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (AbsentInformationException e) {
            System.err.println("Error: Debug information not available");
        } catch (Exception e) {
            System.err.println("Error getting method information: " + e.getMessage());
        }
    }
}