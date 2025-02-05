package commandPattern;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;

public interface Command {
    void execute();

    default boolean resume() {
        return false;
    }
}
