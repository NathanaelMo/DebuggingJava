package Interface;

import dbg.ScriptableDebugger;
import dbg.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DebuggerInterface extends JFrame {
    private JTextArea textArea;
    private JPanel buttonPanel;
    private JButton[] buttons;
    private JLabel logoLabel;
    private DebuggerLauncher debuggerLauncher;
    private BufferedWriter processWriter;
    private BufferedReader processReader;

    // File de commandes pour la communication asynchrone entre l'interface et le débogueur
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public DebuggerInterface(Class debugClass) {
        debuggerLauncher = new DebuggerLauncher(debugClass);
        setupInterface();
        launchDebugger();
    }

    /**
     * Démarre le processus du débogueur et lance deux threads :
     * - Un thread qui attend les commandes saisies dans l'interface et les envoie au débogueur.
     * - Un thread qui lit la sortie du débogueur et l'affiche dans le JTextArea.
     */
    private void launchDebugger() {
        debuggerLauncher.startDebuggerProcess();
        // Supposons que DebuggerLauncher fournit des méthodes pour obtenir les flux d'écriture et de lecture
        processWriter = debuggerLauncher.getProcessWriter();
        processReader = debuggerLauncher.getProcessReader();

        // Thread dédié à l'envoi des commandes vers le débogueur
        new Thread(() -> {
            try {
                while (true) {
                    String command = commandQueue.take(); // Attend une commande dans la file
                    processWriter.write(command);
                    processWriter.flush();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Thread dédié à la lecture de la sortie du débogueur
        new Thread(() -> {
            try {
                String line;
                while ((line = processReader.readLine()) != null) {
                    final String output = line;
                    SwingUtilities.invokeLater(() -> textArea.append(output + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Configure l'interface graphique : titre, dimension, position, etc.
     */
    private void setupInterface() {
        setTitle("Debugger Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                debuggerLauncher.stopDebuggerProcess();
                dispose();
            }
        });

        // Panel pour le logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoLabel = new JLabel();
        logoPanel.add(logoLabel);
        add(logoPanel, BorderLayout.NORTH);

        // Zone de texte avec scroll
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        redirectSystemStreams();
        setupButtonPanel();
        setupComponents();
    }

    /**
     * Méthode complémentaire de configuration (redondante ici pour la démo)
     */
    private void setupComponents() {
        // Panel pour le logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoLabel = new JLabel();
        logoPanel.add(logoLabel);
        add(logoPanel, BorderLayout.NORTH);

        // Zone de texte
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        setupButtonPanel();
        redirectSystemStreams();
    }

    /**
     * Méthode modifiée :
     * Au lieu d'exécuter directement la commande, on l'ajoute dans la file (BlockingQueue)
     * pour qu'elle soit traitée par le thread dédié.
     */
    private void handleDebuggerCommand(String command) {
        textArea.append("> " + command + "\n");
        commandQueue.offer(command);
        System.out.println(command);
    }

    /**
     * Configure le panneau contenant les boutons de commande.
     */
    private void setupButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setPreferredSize(new Dimension(200, getHeight()));

        String[][] buttonConfig = {
                {"Step", "step"},
                {"Step Over", "step-over"},
                {"Continue", "continue"},
                {"Show Frame", "frame"},
                {"Show Variables", "temporaries"},
                {"Show Stack", "stack"},
                {"Show Receiver", "receiver"},
                {"Show Sender", "sender"},
                {"Show Receiver Vars", "receiver-variables"},
                {"Show Method", "method"},
                {"Show Arguments", "arguments"},
                {"Show Breakpoints", "breakpoints"},
        };

        buttons = new JButton[buttonConfig.length];

        for (int i = 0; i < buttonConfig.length; i++) {
            buttons[i] = new JButton(buttonConfig[i][0]);
            buttons[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            final String command = buttonConfig[i][1];

            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleDebuggerCommand(command);
                }
            });

            if (i > 0) {
                buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            buttonPanel.add(buttons[i]);
        }

        add(buttonPanel, BorderLayout.EAST);
    }

    /**
     * Redirige les flux System.out et System.err vers le JTextArea de l'interface.
     */
    private void redirectSystemStreams() {
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
    }

    /**
     * Classe interne pour rediriger les sorties vers le JTextArea.
     */
    private static class CustomOutputStream extends OutputStream {
        private final JTextArea textArea;
        private StringBuilder sb = new StringBuilder();

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            if (b == '\n') {
                final String text = sb.toString() + "\n";
                SwingUtilities.invokeLater(() -> textArea.append(text));
                sb.setLength(0);
            } else {
                sb.append((char) b);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DebuggerInterface debuggerInterface = new DebuggerInterface(Test.class);
            debuggerInterface.setVisible(true);
        });
    }
}
