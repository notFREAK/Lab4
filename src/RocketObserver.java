public interface RocketObserver {
    void onStageSeparation(int stageNumber);
    void onUpdateStatus(double currentMass, double speed, double altitude, int remainingStages, double[] fuelMasses, double[] initialFuelMasses);
}