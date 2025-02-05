package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.Map;

public class TemporariesCommand implements Command {
    private final LocatableEvent event;

    public TemporariesCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {


        StackFrame frame = null;
        try {
            frame = event.thread().frame(0);
            System.out.println("Les variables :");
            Map<LocalVariable, Value> visibleVariables = frame.getValues(frame.visibleVariables());

            if (visibleVariables.isEmpty()) {
                System.out.println("pas de variables");
            } else {
                for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                    System.out.println(entry.getKey().name() + " → " + entry.getValue());
                }
            }
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            throw new RuntimeException(e);
        }

    }
}