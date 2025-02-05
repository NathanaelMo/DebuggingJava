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


        StackFrame frame = null;
        try {
            frame = event.thread().frame(0);
            ObjectReference receiver = frame.thisObject();

            if (receiver != null) {
                System.out.println("Receiver (this):");
                System.out.println("Type: " + receiver.referenceType().name());
                System.out.println("Value: " + receiver.toString());
            } else {
                System.out.println("Pas de receiver");
            }
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }

    }
}