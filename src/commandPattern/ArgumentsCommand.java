package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class ArgumentsCommand implements Command {
    private final LocatableEvent event;

    public ArgumentsCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        StackFrame currentFrame = null;
        try {
            currentFrame = event.thread().frame(0);
            Method method = currentFrame.location().method();

            List<LocalVariable> arguments = method.arguments();

            if (arguments != null && !arguments.isEmpty()) {
                System.out.println("Method arguments:");
                for (LocalVariable arg : arguments) {
                    Value value = currentFrame.getValue(arg);
                    System.out.println(arg.name() + " → " + value);
                }
            } else {
                System.out.println("Pas d'arguments");
            }
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        } catch (AbsentInformationException e) {
            throw new RuntimeException(e);
        }
    }
}