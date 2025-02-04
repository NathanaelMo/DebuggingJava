package commandPattern;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;

import java.util.List;

public class PrintVarCommand implements Command {
    private final LocatableEvent event;
    private final String varName;

    public PrintVarCommand(LocatableEvent event, String varName) {
        this.event = event;
        this.varName = varName;
    }

    @Override
    public void execute() {
        try {
            // Récupérer la frame courante
            StackFrame currentFrame = event.thread().frame(0);

            // Rechercher d'abord dans les variables locales
            List<LocalVariable> localVars = currentFrame.visibleVariables();
            for (LocalVariable var : localVars) {
                if (var.name().equals(varName)) {
                    Value value = currentFrame.getValue(var);
                    System.out.println("Local variable " + varName + " → " + value);
                    return;
                }
            }

            // Si pas trouvé dans les variables locales, chercher dans les variables d'instance
            ObjectReference thisObject = currentFrame.thisObject();
            if (thisObject != null) {
                ReferenceType type = thisObject.referenceType();
                List<Field> fields = type.allFields();
                for (Field field : fields) {
                    if (field.name().equals(varName)) {
                        Value value = thisObject.getValue(field);
                        System.out.println("Instance variable " + varName + " → " + value);
                        return;
                    }
                }
            }

            System.out.println("Variable '" + varName + "' not found");

        } catch (IncompatibleThreadStateException e) {
            System.err.println("Error: Thread not suspended");
        } catch (AbsentInformationException e) {
            System.err.println("Error: Debug information not available");
        } catch (Exception e) {
            System.err.println("Error getting variable value: " + e.getMessage());
        }
    }
}