public class Main {
    public static void main(String[] args) {
        RocketModel model = new RocketModel();
        RocketController controller = new RocketController(model);
        RocketGUI gui = new RocketGUI(controller);
        model.addObserver(gui);
    }
}