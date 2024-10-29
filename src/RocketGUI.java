import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RocketGUI extends JFrame implements RocketObserver {
    private RocketController controller;
    private JProgressBar[] fuelBars;

    private JLabel[] fuelLabels;
    private JLabel statusLabel;

    private JTextField payloadMassField;
    private JTextField[] stageMassFields;
    private JTextField[] fuelMassFields;
    private JTextField thrustField;

    private JButton startStopButton;
    private boolean isSimulating = false;

    public RocketGUI(RocketController controller) {
        this.controller = controller;

        initUI();
    }

    private void initUI() {
        setTitle("Rocket Simulation");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel fuelPanel = new JPanel();
        fuelPanel.setLayout(new BoxLayout(fuelPanel, BoxLayout.Y_AXIS));

        fuelBars = new JProgressBar[3];
        fuelLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            fuelBars[i] = new JProgressBar(0, 100);
            fuelBars[i].setValue(100);
            fuelBars[i].setForeground(Color.GREEN);

            fuelLabels[i] = new JLabel("Ступень " + (i + 1));
            fuelPanel.add(fuelLabels[i]);
            fuelPanel.add(fuelBars[i]);
        }

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(8, 2));

        payloadMassField = new JTextField("10");
        inputPanel.add(new JLabel("Полезная масса (kg):"));
        inputPanel.add(payloadMassField);

        stageMassFields = new JTextField[3];
        fuelMassFields = new JTextField[3];

        for (int i = 0; i < 3; i++) {
            stageMassFields[i] = new JTextField("5");
            fuelMassFields[i] = new JTextField("3");

            inputPanel.add(new JLabel("Масса ступени " + (i + 1) + " (kg):"));
            inputPanel.add(stageMassFields[i]);

            inputPanel.add(new JLabel("Масса топлева ступени " + (i + 1) + " (kg):"));
            inputPanel.add(fuelMassFields[i]);
        }

        thrustField = new JTextField("3500");
        inputPanel.add(new JLabel("Тяга на кг топлива:"));
        inputPanel.add(thrustField);

        add(inputPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        startStopButton = new JButton("Начать Симуляцию");
        statusLabel = new JLabel("Готово к запуску");


        controlPanel.add(statusLabel, BorderLayout.NORTH);
        controlPanel.add(fuelPanel);


        add(controlPanel, BorderLayout.CENTER);
        add(startStopButton, BorderLayout.SOUTH);
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSimulating) {
                    if (validateInputs()) {
                        startSimulation();
                    }
                } else {
                    stopSimulation();
                }
            }
        });

        setVisible(true);
    }


    private void startSimulation() {
        double payloadMass = Double.parseDouble(payloadMassField.getText());
        double[] stageMasses = new double[3];
        double[] fuelMasses = new double[3];
        for (int i = 0; i < 3; i++) {
            stageMasses[i] = Double.parseDouble(stageMassFields[i].getText());
            fuelMasses[i] = Double.parseDouble(fuelMassFields[i].getText());
        }
        double thrustPerKgFuel = Double.parseDouble(thrustField.getText());

        controller.setRocketParameters(payloadMass, stageMasses, fuelMasses, thrustPerKgFuel);
        controller.startSimulation();

        isSimulating = true;
        startStopButton.setText("Остановить Симуляцию");
        statusLabel.setText("Симуляция запущена");
        System.out.println("Симуляция запущена");
    }

    private void stopSimulation() {
        controller.stopSimulation();
        isSimulating = false;
        startStopButton.setText("Начать Симуляцию");
        System.out.println("Симуляция остановлена");
    }

    private boolean validateInputs() {
        try {
            double payloadMass = Double.parseDouble(payloadMassField.getText());
            double[] stageMasses = new double[3];
            double[] fuelMasses = new double[3];
            for (int i = 0; i < 3; i++) {
                stageMasses[i] = Double.parseDouble(stageMassFields[i].getText());
                fuelMasses[i] = Double.parseDouble(fuelMassFields[i].getText());
            }
            double thrustPerKgFuel = Double.parseDouble(thrustField.getText());

            for (int i = 0; i < 3; i++) {
                double remainingMass = payloadMass;
                for (int j = i; j < 3; j++) {
                    remainingMass += stageMasses[j] + fuelMasses[j];
                }
                double thrust = 0.1 * thrustPerKgFuel ;

                if (thrust / remainingMass <= 10) {
                    statusLabel.setText("Error: Тяга ступени " + (i + 1) + " слишком низкая! (Добавьте топлива или тягу)");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Неверный ввод, используйте целочисленные значения.");
            return false;
        }

        return true;
    }

    @Override
    public void onStageSeparation(int stageNumber) {
        System.out.println("Ступень  " + stageNumber + " отделилась!");
    }

    @Override
    public void onUpdateStatus(double currentMass, double speed, double altitude, int remainingStages, double[] fuelMasses, double[] initialFuelMasses) {
        statusLabel.setText(String.format("Масса: %.2f kg, Скорость: %.2f m/s, Высота: %.2f m", currentMass, speed, altitude));

        for (int i = 0; i < 3; i++) {
            if (i < remainingStages) {
                fuelLabels[i].setText(String.format("Ступень %d  масса: %.2f", i+1 , fuelMasses[i]));
                int fuelPercentage = (int) (fuelMasses[i] / initialFuelMasses[i] * 100);
                fuelBars[i].setValue(fuelPercentage);
                if (fuelPercentage == 0) {
                    fuelBars[i].setForeground(Color.RED);
                } else {
                    fuelBars[i].setForeground(Color.GREEN);
                }
            } else {
                fuelBars[i].setValue(0);
                fuelBars[i].setForeground(Color.RED);
                fuelLabels[i].setText(String.format("Ступень %d отделена!", i+1));
            }
        }
    }
}