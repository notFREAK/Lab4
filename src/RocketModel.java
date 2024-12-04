import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RocketModel {
    private static final double EARTH_RADIUS = 6_371_000;  // Радиус Земли в метрах
    private static final double GRAVITATIONAL_CONSTANT = 6.67430e-11;  // Гравитационная постоянная
    private static final double EARTH_MASS = 5.972e24;  // Масса Земли в килограммах

    private double payloadMass;
    private double[] stageMasses;
    private double[] fuelMasses;
    private double[] initialFuelMasses;
    private double thrustPerKgFuel;


    private double currentMass;
    private double speed;
    private double altitude;
    private int remainingStages;

    private int millsDelay = 100;
    private List<RocketObserver> observers = new ArrayList<>();
    private boolean running = false;
    private double fuelConsumption = 0.01;
    private double deltaTime = millsDelay * 0.001;

    public void startSimulation() {
        new Thread(() -> {
            running = true;
            while (running && (altitude >= 0 || speed > 0)) {
                updateRocketState();
                notifyObservers();

                try {
                    Thread.sleep(millsDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopSimulation() {
        running = false;
    }

    private void updateRocketState() {
        if (remainingStages == 0) {
            simulatePayloadFlight();
            return;
        }

        int currentStage = remainingStages - 1;

        double thrust = fuelConsumption/deltaTime  * thrustPerKgFuel;

        fuelMasses[currentStage] -= fuelConsumption;

        if (fuelMasses[currentStage] <= 0) {
            fuelMasses[currentStage] = 0;
            separateStage();
            return;
        }

        currentMass = payloadMass;
        for (int i = 0; i < remainingStages; i++) {
            currentMass += stageMasses[i] + fuelMasses[i];
        }

        double gravity = calculateGravity(altitude);

        // Ускорение ракеты по уравнению Мещерского
        // a = F / m - g
        double acceleration = thrust / currentMass - gravity;


        // Обновляем скорость и высоту (интегрируем ускорение и скорость по времени)
        speed += acceleration * deltaTime;
        altitude += speed * deltaTime;
        if (altitude < 0) {
            speed = 0;
            altitude = 0;
        }
        notifyObservers();
    }


    private void simulatePayloadFlight() {

        currentMass = payloadMass;

        double gravity = calculateGravity(altitude);

        double acceleration = -gravity;

        speed += acceleration * deltaTime;
        altitude += speed * deltaTime;

        if (altitude < 0) {
            altitude = 0;
            speed = 0;
            stopSimulation();
        }

        notifyObservers();
    }

    public void addObserver(RocketObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(RocketObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (RocketObserver observer : observers) {
            observer.onUpdateStatus(currentMass, speed, altitude, remainingStages, fuelMasses, initialFuelMasses);
        }
    }

    private void separateStage() {
        for (RocketObserver observer : observers) {
            observer.onStageSeparation(remainingStages--);
        }
    }

    private double calculateGravity(double altitude) {
        double distanceFromEarthCenter = EARTH_RADIUS + altitude;
        return GRAVITATIONAL_CONSTANT * EARTH_MASS / Math.pow(distanceFromEarthCenter, 2);
    }

    public void setRocketParameters(double payloadMass, double[] stageMasses, double[] fuelMasses, double thrustPerKgFuel) {
        this.payloadMass = payloadMass;
        this.stageMasses = stageMasses;
        this.fuelMasses = fuelMasses;
        this.initialFuelMasses = fuelMasses.clone();
        this.thrustPerKgFuel = thrustPerKgFuel;
        this.remainingStages = stageMasses.length;
        this.currentMass = payloadMass + Arrays.stream(stageMasses).sum() + Arrays.stream(fuelMasses).sum();
        this.altitude = 0;
        this.speed = 0;
    }
}