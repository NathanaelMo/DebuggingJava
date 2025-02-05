package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class MethodCommand implements Command {
    private final LocatableEvent event;

    public MethodCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        StackFrame frame = null;
        try {
            frame = event.thread().frame(0);
            Location location = frame.location();
            Method method = location.method();

            System.out.println("Method : ");
            System.out.println("Name: " + method.name());
            System.out.println("Declaring class: " + method.declaringType().name());
            System.out.println("Return type: " + method.returnTypeName());
            System.out.println("Is static: " + method.isStatic());
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
    }
}