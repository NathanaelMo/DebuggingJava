package commandPattern;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.EventRequestManager;

public class FrameCommand implements Command {
    private final LocatableEvent event;

    public FrameCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        StackFrame frame = null;
        try {
            frame = event.thread().frame(0);
            System.out.println("Frame : " + frame.toString());
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
    }



}