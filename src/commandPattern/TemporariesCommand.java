package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.Map;

public class TemporariesCommand implements Command {
    private final LocatableEvent event;

    public TemporariesCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            // Récupérer la frame courante
            StackFrame currentFrame = event.thread().frame(0);

            // Récupérer et afficher les variables locales
            System.out.println("Local variables :");
            Map<LocalVariable, Value> visibleVariables = currentFrame.getValues(currentFrame.visibleVariables());

            if (visibleVariables.isEmpty()) {
                System.out.println("pas de local variables");
            } else {
                for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                    System.out.println(entry.getKey().name() + " → " + entry.getValue());
                }
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (AbsentInformationException e) {
            System.err.println("Error: Debug information not available");
        } catch (Exception e) {
            System.err.println("Error getting local variables: " + e.getMessage());
        }
    }
}