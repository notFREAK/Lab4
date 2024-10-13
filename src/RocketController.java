// Контроллер
public class RocketController implements RocketControl {
    private RocketModel model;

    public RocketController(RocketModel model) {
        this.model = model;
    }

    @Override
    public void startSimulation() {
        model.startSimulation();
    }

    @Override
    public void stopSimulation() {
        model.stopSimulation();
    }

    @Override
    public void setRocketParameters(double payloadMass, double[] stageMasses, double[] fuelMasses, double thrustPerKgFuel) {
        model.setRocketParameters(payloadMass, stageMasses, fuelMasses, thrustPerKgFuel);
    }
}