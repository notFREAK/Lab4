import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

// Модель ракеты
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

    private List<RocketObserver> observers = new ArrayList<>();
    private boolean running = false;

    // Метод запуска симуляции (в отдельном потоке)
    public void startSimulation() {
        new Thread(() -> {
            running = true;
            while (running && (altitude >= 0 || speed > 0)) {
                updateRocketState();
                notifyObservers();

                try {
                    Thread.sleep(100);  // обновление каждую 1/10 секунды
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopSimulation() {
        running = false;
    }

    // Метод обновления состояния ракеты
    private void updateRocketState() {
        // Если больше нет ступеней, продолжаем расчет для полезной нагрузки
        if (remainingStages == 0) {
            simulatePayloadFlight();
            return;
        }

        // Текущая активная ступень
        int currentStage = remainingStages - 1;

        // Мгновенное значение силы тяги
        double thrust = fuelMasses[currentStage] * thrustPerKgFuel;  // Тяга пропорциональна оставшемуся топливу

        // Расход топлива: уменьшение топлива в зависимости от тяги
        double fuelConsumption = 0.01;  // Например, топливо расходуется со скоростью 0.01 кг/с
        fuelMasses[currentStage] -= fuelConsumption;

        // Проверка на исчерпание топлива в текущей ступени
        if (fuelMasses[currentStage] <= 0) {
            fuelMasses[currentStage] = 0;  // Установить топливо на 0
            separateStage();  // Отделить текущую ступень
            return;  // После отделения обновление состояния будет вызвано заново
        }

        // Текущая масса ракеты = масса полезной нагрузки + оставшиеся ступени + оставшееся топливо
        currentMass = payloadMass;
        for (int i = 0; i < remainingStages; i++) {
            currentMass += stageMasses[i] + fuelMasses[i];
        }

        // Расчет ускорения свободного падения на текущей высоте
        double gravity = calculateGravity(altitude);

        // Ускорение (по второму закону Ньютона: a = F / m - g)
        double acceleration = thrust / currentMass - gravity;

        double deltaTime = 0.1;  // Время между обновлениями, например, 0.1 сек (100 мс)

        // Обновляем скорость и высоту (интегрируем ускорение и скорость по времени)
        speed += acceleration * deltaTime;
        altitude += speed * deltaTime;

        // Уведомление наблюдателей о новом состоянии ракеты
        notifyObservers();
    }

    // Метод для симуляции полета полезной нагрузки после отделения последней ступени
    private void simulatePayloadFlight() {
        // Масса полезной нагрузки остается неизменной
        currentMass = payloadMass;

        // Расчет ускорения свободного падения на текущей высоте
        double gravity = calculateGravity(altitude);

        double deltaTime = 0.1;  // Время между обновлениями, например, 0.1 сек (100 мс)

        // Ускорение = только гравитационное замедление
        double acceleration = -gravity;

        // Обновляем скорость и высоту (интегрируем ускорение и скорость по времени)
        speed += acceleration * deltaTime;
        altitude += speed * deltaTime;

        // Ограничиваем высоту до 0, если ракета падает ниже поверхности Земли
        if (altitude < 0) {
            altitude = 0;
            speed = 0;
            stopSimulation();  // Останавливаем симуляцию, когда ракета достигла земли
        }

        // Уведомление наблюдателей о новом состоянии ракеты
        notifyObservers();
    }

    // Добавление наблюдателя
    public void addObserver(RocketObserver observer) {
        observers.add(observer);
    }

    // Удаление наблюдателя
    public void removeObserver(RocketObserver observer) {
        observers.remove(observer);
    }

    // Уведомление всех наблюдателей
    private void notifyObservers() {
        for (RocketObserver observer : observers) {
            observer.onUpdateStatus(currentMass, speed, altitude, remainingStages, fuelMasses, initialFuelMasses);
        }
    }

    // Отделение ступени
    private void separateStage() {
        // Логика отделения ступени
        for (RocketObserver observer : observers) {
            observer.onStageSeparation(remainingStages--);
        }
    }

    // Расчет силы тяжести на данной высоте
    private double calculateGravity(double altitude) {
        double distanceFromEarthCenter = EARTH_RADIUS + altitude;
        return GRAVITATIONAL_CONSTANT * EARTH_MASS / Math.pow(distanceFromEarthCenter, 2);
    }

    // Установка параметров ракеты
    public void setRocketParameters(double payloadMass, double[] stageMasses, double[] fuelMasses, double thrustPerKgFuel) {
        this.payloadMass = payloadMass;
        this.stageMasses = stageMasses;
        this.fuelMasses = fuelMasses;
        this.initialFuelMasses = fuelMasses.clone();
        this.thrustPerKgFuel = thrustPerKgFuel;
        this.remainingStages = stageMasses.length;
        this.currentMass = payloadMass + Arrays.stream(stageMasses).sum() + Arrays.stream(fuelMasses).sum();
        this.altitude = 0;  // Начальная высота
        this.speed = 0;     // Начальная скорость
    }
}