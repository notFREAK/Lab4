import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RocketGUI extends JFrame implements RocketObserver {
    private RocketController controller;
    private JProgressBar[] fuelBars;

    private JLabel[] fuelLabels;
    private JLabel statusLabel;

    // Поля для ввода параметров
    private JTextField payloadMassField;
    private JTextField[] stageMassFields;
    private JTextField[] fuelMassFields;
    private JTextField thrustField;

    // Кнопка для управления симуляцией
    private JButton startStopButton;
    private boolean isSimulating = false;  // Флаг, указывающий на состояние симуляции

    public RocketGUI(RocketController controller) {
        this.controller = controller;

        initUI();
    }

    private void initUI() {
        setTitle("Rocket Simulation");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель с прогрессбарами
        JPanel fuelPanel = new JPanel();
        fuelPanel.setLayout(new BoxLayout(fuelPanel, BoxLayout.Y_AXIS));

        // Прогрессбары для топлива ступеней
        fuelBars = new JProgressBar[3];
        fuelLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            fuelBars[i] = new JProgressBar(0, 100);
            fuelBars[i].setValue(100);  // Изначально 100%
            fuelBars[i].setForeground(Color.GREEN);

            fuelLabels[i] = new JLabel("Stage " + (i + 1));
            fuelPanel.add(fuelLabels[i]);
            fuelPanel.add(fuelBars[i]);
        }

        // Панель для ввода параметров
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(8, 2));

        payloadMassField = new JTextField("10");  // По умолчанию 1000 кг
        inputPanel.add(new JLabel("Payload Mass (kg):"));
        inputPanel.add(payloadMassField);

        stageMassFields = new JTextField[3];
        fuelMassFields = new JTextField[3];

        for (int i = 0; i < 3; i++) {
            stageMassFields[i] = new JTextField("5");  // По умолчанию 500 кг
            fuelMassFields[i] = new JTextField("3");  // По умолчанию 300 кг

            inputPanel.add(new JLabel("Stage " + (i + 1) + " Mass (kg):"));
            inputPanel.add(stageMassFields[i]);

            inputPanel.add(new JLabel("Stage " + (i + 1) + " Fuel Mass (kg):"));
            inputPanel.add(fuelMassFields[i]);
        }

        thrustField = new JTextField("150");  // По умолчанию 150 Н/кг
        inputPanel.add(new JLabel("Thrust per kg of fuel:"));
        inputPanel.add(thrustField);

        add(inputPanel, BorderLayout.NORTH);

        // Панель для кнопки и статуса
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        // Кнопка для запуска/остановки симуляции
        startStopButton = new JButton("Start Simulation");
        statusLabel = new JLabel("Ready to launch");


        controlPanel.add(statusLabel, BorderLayout.NORTH);
        controlPanel.add(fuelPanel);

        // Лейбл для статуса

        add(controlPanel, BorderLayout.CENTER);
        add(startStopButton, BorderLayout.SOUTH);
        // Обработчик кнопки "Start/Stop"
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

    // Метод запуска симуляции
    private void startSimulation() {
        // Считывание параметров с GUI
        double payloadMass = Double.parseDouble(payloadMassField.getText());
        double[] stageMasses = new double[3];
        double[] fuelMasses = new double[3];
        for (int i = 0; i < 3; i++) {
            stageMasses[i] = Double.parseDouble(stageMassFields[i].getText());
            fuelMasses[i] = Double.parseDouble(fuelMassFields[i].getText());
        }
        double thrustPerKgFuel = Double.parseDouble(thrustField.getText());

        // Установка параметров ракеты в контроллер
        controller.setRocketParameters(payloadMass, stageMasses, fuelMasses, thrustPerKgFuel);
        controller.startSimulation();

        // Обновление статуса
        isSimulating = true;
        startStopButton.setText("Stop Simulation");
        statusLabel.setText("Simulation started");
        System.out.println("Simulation started");
    }

    // Метод остановки симуляции
    private void stopSimulation() {
        controller.stopSimulation();
        isSimulating = false;
        startStopButton.setText("Start Simulation");
        statusLabel.setText("Simulation stopped");
        System.out.println("Simulation stopped");
    }

    // Валидация вводимых данных
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

            // Проверка, чтобы тяга каждой ступени была больше, чем масса оставшихся ступеней
            for (int i = 0; i < 3; i++) {
                double remainingMass = payloadMass;
                for (int j = i; j < 3; j++) {
                    remainingMass += stageMasses[j] + fuelMasses[j];
                }
                double thrust = fuelMasses[i] * thrustPerKgFuel;

                if (thrust <= remainingMass) {
                    statusLabel.setText("Error: Thrust of stage " + (i + 1) + " is too low!");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Invalid input. Please enter numeric values.");
            return false;
        }

        return true;  // Все проверки пройдены
    }

    @Override
    public void onStageSeparation(int stageNumber) {
        System.out.println("Stage " + stageNumber + " separated!");
    }

    @Override
    public void onUpdateStatus(double currentMass, double speed, double altitude, int remainingStages, double[] fuelMasses, double[] initialFuelMasses) {
        statusLabel.setText(String.format("Mass: %.2f kg, Speed: %.2f m/s, Altitude: %.2f m", currentMass, speed, altitude));

        // Обновляем прогресс-бары в зависимости от количества оставшегося топлива
        for (int i = 0; i < 3; i++) {
            if (i < remainingStages) {
                fuelLabels[i].setText(String.format("Stage %d  mass: %.2f", i+1 , fuelMasses[i]));
                int fuelPercentage = (int) (fuelMasses[i] / initialFuelMasses[i] * 100);
                fuelBars[i].setValue(fuelPercentage);
                if (fuelPercentage == 0) {
                    fuelBars[i].setForeground(Color.RED);  // Если топлива нет, перекрасить в красный
                } else {
                    fuelBars[i].setForeground(Color.GREEN);  // Пока есть топливо, прогрессбар зелёный
                }
            } else {
                fuelBars[i].setValue(0);  // Если ступень отделена, сбросить прогрессбар
                fuelBars[i].setForeground(Color.RED);  // Отделённая ступень показывается как пустая
                fuelLabels[i].setText(String.format("Stage %d has separated!", i+1));
            }
        }
    }
}