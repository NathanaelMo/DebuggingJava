package dbg;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public interface Command {
    void execute();
}
