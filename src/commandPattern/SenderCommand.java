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

        ThreadReference thread = event.thread();
        try {
            if (thread.frameCount() > 1) {
                StackFrame frames = thread.frame(1);
                ObjectReference sender = frames.thisObject();

                if (sender != null) {
                    System.out.println("Sender");
                    System.out.println("Type: " + sender.referenceType().name());
                    System.out.println("Method: " + frames.location().method().name());
                    System.out.println("Value: " + sender);
                } else {
                    System.out.println("Sender null");
                }
            } else {
                System.out.println("Il n'y a pas de sender");
            }
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
    }
}