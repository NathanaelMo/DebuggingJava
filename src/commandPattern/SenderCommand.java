package commandPattern;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class SenderCommand implements Command {
    private final LocatableEvent event;

    public SenderCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            // Récupérer le thread et ses frames
            ThreadReference thread = event.thread();
            List<StackFrame> frames = thread.frames();

            if (frames.size() > 1) {
                // La frame 1 contient l'appelant (sender)
                StackFrame senderFrame = frames.get(1);
                ObjectReference sender = senderFrame.thisObject();

                if (sender != null) {
                    System.out.println("Sender information:");
                    System.out.println("Type: " + sender.referenceType().name());
                    System.out.println("Method: " + senderFrame.location().method().name());
                    System.out.println("Value: " + sender);
                } else {
                    System.out.println("Sender is a static method");
                }
            } else {
                System.out.println("No sender available (top-level method)");
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (Exception e) {
            System.err.println("Error getting sender: " + e.getMessage());
        }
    }
}