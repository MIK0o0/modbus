import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CRC_GUI extends JFrame {

    private final JTextField inputField;
    private final JTextField iterationsField;
    private final JTextArea resultArea;
    private final JComboBox<String> methodSelector;

    public CRC_GUI() {
        super("Modbus CRC Kalkulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Sekwencja bajtów \\(hex\\):"));
        inputField = new JTextField();
        inputPanel.add(inputField);

        inputPanel.add(new JLabel("Liczba powtórzeń:"));
        iterationsField = new JTextField();
        inputPanel.add(iterationsField);


        methodSelector = new JComboBox<>(new String[] { "CRCx8", "CRCx16" });
        inputPanel.add(new JLabel("Typ CRC:"));
        inputPanel.add(methodSelector);

        JButton calculateButton = new JButton("Oblicz CRC");
        calculateButton.addActionListener(this::calculateCRC);
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(calculateButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void calculateCRC(ActionEvent e) {
        try {
            String input = inputField.getText().replaceAll("\\s+", "");
            int n = Integer.parseInt(iterationsField.getText());

            if (input.length() % 2 != 0) {
                JOptionPane.showMessageDialog(this, "Nieparzysta liczba znaków w sekwencji!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (input.length() / 2 > 256) {
                JOptionPane.showMessageDialog(this, "Sekwencja nie może być dłuższa niż 256 bajtów!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] bytes = new byte[input.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                String hex = input.substring(i * 2, i * 2 + 2);
                bytes[i] = (byte) Integer.parseInt(hex, 16);
            }

            String selectedMethod = (String) methodSelector.getSelectedItem();
            int crc = "CRCx8".equals(selectedMethod)
                    ? ModbusCRCTwoTables.computeModbusCRC(bytes)
                    : ModbusCRCSingleTable.computeModbusCRC(bytes);

            String crcHex = String.format("%04X", crc);
            String swappedCrc = crcHex.substring(2) + crcHex.substring(0, 2);

            //Pomiar czasu
            long startTime = System.nanoTime();
            for (int i = 0; i < n; i++) {
                if ("CRCx8".equals(selectedMethod)) {
                    ModbusCRCTwoTables.computeModbusCRC(bytes);
                } else {
                    ModbusCRCSingleTable.computeModbusCRC(bytes);
                }
            }
            long totalTime = System.nanoTime() - startTime;

            resultArea.setText(String.format(
                    "Wynik CRC: %s\nCzas wykonania %,d iteracji: %.3f ms",
                    swappedCrc, n, totalTime / 1_000_000.0
            ));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nieprawidłowy format liczby powtórzeń!", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CRC_GUI().setVisible(true));
    }
}
