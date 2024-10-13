// Интерфейс команд для управления контроллером
public interface RocketControl {
    void startSimulation();
    void stopSimulation();
    void setRocketParameters(double payloadMass, double[] stageMasses, double[] fuelMasses, double thrustPerKgFuel);
}