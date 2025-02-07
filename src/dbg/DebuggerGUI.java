package dbg;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DebuggerGUI extends JFrame {
    private JTextArea outputArea;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private final BlockingQueue<String> commandQueue;


    public DebuggerGUI() {
        this.commandQueue = new LinkedBlockingQueue<>();
        setupGUI();
        redirectSystemOut();
    }

    private void setupGUI() {
        setTitle("Java Debugger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);

        // Zone des boutons simples
        JPanel buttonPanel = createSimpleCommandsPanel();
        add(buttonPanel, BorderLayout.NORTH);

        // Zone des commandes avec paramètres
        JPanel paramCommandsPanel = createParamCommandsPanel();
        add(paramCommandsPanel, BorderLayout.WEST);

        // Zone de sortie
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSimpleCommandsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        String[] commands = {"step", "step-over", "continue", "frame", "temporaries",
                "stack", "receiver", "sender", "receiver-variables",
                "method", "arguments", "breakpoints"};

        for (String command : commands) {
            JButton button = new JButton(command);
            button.addActionListener(e -> sendCommand(command));
            panel.add(button);
        }
        return panel;
    }

    private JPanel createParamCommandsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Break command
        JButton breakButton = new JButton("break");
        breakButton.addActionListener(e -> showBreakDialog());
        panel.add(breakButton);

        // Break once command
        JButton breakOnceButton = new JButton("break-once");
        breakOnceButton.addActionListener(e -> showBreakOnceDialog());
        panel.add(breakOnceButton);

        // Break on count command
        JButton breakOnCountButton = new JButton("break-on-count");
        breakOnCountButton.addActionListener(e -> showBreakOnCountDialog());
        panel.add(breakOnCountButton);

        // Break before method call command
        JButton breakBeforeMethodButton = new JButton("break-before-method-call");
        breakBeforeMethodButton.addActionListener(e -> showBreakBeforeMethodDialog());
        panel.add(breakBeforeMethodButton);

        // Print var command
        JButton printVarButton = new JButton("print-var");
        printVarButton.addActionListener(e -> showPrintVarDialog());
        panel.add(printVarButton);

        // Step back command
        JButton stepBackButton = new JButton("step-back");
        stepBackButton.addActionListener(e -> showStepBackDialog());
        panel.add(stepBackButton);

        return panel;
    }

    private void showBreakDialog() {
        JTextField fileNameField = new JTextField();
        JTextField lineNumberField = new JTextField();

        Object[] message = {
                "File name:", fileNameField,
                "Line number:", lineNumberField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Break", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String command = String.format("break(%s %s)",
                    fileNameField.getText(), lineNumberField.getText());
            sendCommand(command);
        }
    }

    private void showBreakOnceDialog() {
        JTextField fileNameField = new JTextField();
        JTextField lineNumberField = new JTextField();

        Object[] message = {
                "File name:", fileNameField,
                "Line number:", lineNumberField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Break Once", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String command = String.format("break-once(%s %s)",
                    fileNameField.getText(), lineNumberField.getText());
            sendCommand(command);
        }
    }

    private void showBreakOnCountDialog() {
        JTextField fileNameField = new JTextField();
        JTextField lineNumberField = new JTextField();
        JTextField countField = new JTextField();

        Object[] message = {
                "File name:", fileNameField,
                "Line number:", lineNumberField,
                "Count:", countField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Break on Count", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String command = String.format("break-on-count(%s %s %s)",
                    fileNameField.getText(), lineNumberField.getText(), countField.getText());
            sendCommand(command);
        }
    }

    private void showBreakBeforeMethodDialog() {
        String methodName = JOptionPane.showInputDialog(this,
                "Enter method name:", "Break Before Method Call", JOptionPane.QUESTION_MESSAGE);
        if (methodName != null && !methodName.trim().isEmpty()) {
            sendCommand("break-before-method-call(" + methodName + ")");
        }
    }

    private void showPrintVarDialog() {
        String varName = JOptionPane.showInputDialog(this,
                "Enter variable name:", "Print Variable", JOptionPane.QUESTION_MESSAGE);
        if (varName != null && !varName.trim().isEmpty()) {
            sendCommand("print-var(" + varName + ")");
        }
    }

    private void showStepBackDialog() {
        String[] options = {"Simple step-back", "Step-back(n)"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose step-back type", "Step Back",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            sendCommand("step-back");
        } else if (choice == 1) {
            String steps = JOptionPane.showInputDialog(this,
                    "Enter number of steps:", "Step Back (n)", JOptionPane.QUESTION_MESSAGE);
            if (steps != null && !steps.trim().isEmpty()) {
                sendCommand("step-back(" + steps + ")");
            }
        }
    }

    private void sendCommand(String command) {
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getNextCommand() {
        try {
            return commandQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void redirectSystemOut() {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream) {
            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
                SwingUtilities.invokeLater(() -> {
                    outputArea.append(outputStream.toString());
                    outputStream.reset();
                });
            }
        };
        System.setOut(printStream);
    }

    public void showGUI() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}