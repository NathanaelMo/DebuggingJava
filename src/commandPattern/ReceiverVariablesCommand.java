package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;
import java.util.Map;

public class ReceiverVariablesCommand implements Command {
    private final LocatableEvent event;

    public ReceiverVariablesCommand(LocatableEvent event) {
        this.event = event;
    }

    @Override
    public void execute() {
        StackFrame currentFrame = null;
        try {
            currentFrame = event.thread().frame(0);
            ObjectReference receiver = currentFrame.thisObject();
            if (receiver != null) {
                ReferenceType type = receiver.referenceType();
                List<Field> fields = type.allFields();
                if (!fields.isEmpty()) {
                    System.out.println("Variables du receiver : ");
                    Map<Field, Value> values = receiver.getValues(fields);
                    for (Map.Entry<Field, Value> entry : values.entrySet()) {
                        Field field = entry.getKey();
                        Value value = entry.getValue();
                        System.out.println(field.name() + " → " + value);
                    }
                } else {
                    System.out.println("Pas de variables trouvées");
                }
            } else {
                System.out.println("Pas de receiver");
            }
        } catch (IncompatibleThreadStateException e) {
            throw new RuntimeException(e);
        }
    }
}