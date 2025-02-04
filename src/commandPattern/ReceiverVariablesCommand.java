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
        try {
            // Récupérer la frame courante
            StackFrame currentFrame = event.thread().frame(0);
            ObjectReference receiver = currentFrame.thisObject();

            if (receiver != null) {
                // Récupérer le type du receveur
                ReferenceType type = receiver.referenceType();

                // Récupérer tous les champs visibles
                List<Field> fields = type.allFields();

                if (!fields.isEmpty()) {
                    System.out.println("Instance variables of current receiver:");
                    // Récupérer les valeurs de tous les champs
                    Map<Field, Value> values = receiver.getValues(fields);

                    // Afficher chaque variable et sa valeur
                    for (Map.Entry<Field, Value> entry : values.entrySet()) {
                        Field field = entry.getKey();
                        Value value = entry.getValue();
                        System.out.println(field.name() + " → " + value);
                    }
                } else {
                    System.out.println("No instance variables found");
                }
            } else {
                System.out.println("No receiver available (probably in a static method)");
            }

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (Exception e) {
            System.err.println("Error getting receiver variables: " + e.getMessage());
        }
    }
}