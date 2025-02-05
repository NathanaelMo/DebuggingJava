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
        ThreadReference thread = event.thread();
        List<StackFrame> frames;
        try {
            frames = thread.frames();
            System.out.println("Appel stack :");
            for (int i = 0; i < frames.size(); i++) {
                StackFrame frame = frames.get(i);
                Location location = frame.location();
                Method method = location.method();
                System.out.println(i + " " + method.declaringType().name() + "." + method.name() + " ligne " + location.lineNumber());
            }
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }

    }
}