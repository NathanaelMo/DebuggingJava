package dbg;

public class JDISimpleDebugger {
    public static void main(String[] args) throws Exception {
        DebuggerGUI gui = new DebuggerGUI();
        ScriptableDebugger debuggerInstance = new ScriptableDebugger();
        debuggerInstance.setGUI(gui);

        gui.showGUI();
        debuggerInstance.attachTo(Test.class);
    }
}

