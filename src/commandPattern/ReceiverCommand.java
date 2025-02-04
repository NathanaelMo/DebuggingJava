package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

public class ReceiverCommand implements Command {
    private final LocatableEvent event;

    public ReceiverCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            // Récupérer la frame courante
            StackFrame currentFrame = event.thread().frame(0);

            // Récupérer le receveur (this)
            ObjectReference receiver = currentFrame.thisObject();

            if (receiver != null) {
                System.out.println("Current receiver (this):");
                System.out.println("Type: " + receiver.referenceType().name());
                System.out.println("Value: " + receiver.toString());
            } else {
                System.out.println("No receiver available (probably in a static method)");
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (Exception e) {
            System.err.println("Error getting receiver: " + e.getMessage());
        }
    }
}