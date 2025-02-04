package commandPattern;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.event.LocatableEvent;

public class FrameCommand implements Command {
    private final LocatableEvent event;

    public FrameCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            StackFrame currentFrame = event.thread().frame(0);


            System.out.println("frame information:");
            System.out.println("Method: " + currentFrame.location().method());
            System.out.println("Location: " + currentFrame.location());
            System.out.println("Code Index: " + currentFrame.location().codeIndex());
            System.out.println("Line Number: " + currentFrame.location().lineNumber());

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (Exception e) {
            System.err.println("Error getting frame information: " + e.getMessage());
        }
    }
}