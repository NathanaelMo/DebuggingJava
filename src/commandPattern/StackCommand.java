package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class StackCommand implements Command {
    private final LocatableEvent event;

    public StackCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            ThreadReference thread = event.thread();
            List<StackFrame> frames = thread.frames();

            System.out.println("Appel stack :");
            for (int i = 0; i < frames.size(); i++) {
                StackFrame frame = frames.get(i);
                Location location = frame.location();
                Method method = location.method();

                // Afficher les informations de chaque frame dans la pile
                System.out.println(String.format("[%d] %s.%s (ligne %d)",
                        i,
                        method.declaringType().name(),
                        method.name(),
                        location.lineNumber()));
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (Exception e) {
            System.err.println("Error getting stack information: " + e.getMessage());
        }
    }
}