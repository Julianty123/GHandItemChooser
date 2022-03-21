import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HEntity;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.scene.control.*;
import javax.swing.*;
import java.util.Map;
import java.util.TreeMap;

@ExtensionInfo(
        Title = "GHandItemChooser",
        Description = "Developed by doraemon in Python", // (Initially)
        Version = "1.0.0",
        Author = "Julianty"
)


public class GHandItemChooser extends ExtensionForm {
    public Button buttonIntercept;
    public ChoiceBox<String> choiceBoxItems;
    public TextField txtFurniId, txtDelay;
    public CheckBox checkFurniId, checkShowItemId;
    public RadioButton radioBlackHole, radioNormalFridge, radioFreezeFridge, radioFlowers;

    public Timer timerUseFurniture;
    public int yourIndex = -1;
    public String yourName;

    private static final TreeMap<Integer, String> blackHoleIdToNameItem = new TreeMap<>();
    static {
        blackHoleIdToNameItem.put(1, "Tea");
        blackHoleIdToNameItem.put(3, "Carrot"); // Zanahoria
        blackHoleIdToNameItem.put(28, "Sake");
        blackHoleIdToNameItem.put(29, "Tomato juice");
        blackHoleIdToNameItem.put(34, "Fresh-Cool"); // Fresco
        blackHoleIdToNameItem.put(36, "Pear"); // Pera
        blackHoleIdToNameItem.put(37, "Peach"); // Durazno
        blackHoleIdToNameItem.put(38, "Orange");
        blackHoleIdToNameItem.put(58, "Blood cup");
        blackHoleIdToNameItem.put(70, "Chicken thigh");
        blackHoleIdToNameItem.put(71, "Toast"); // Pan tajado
        blackHoleIdToNameItem.put(1013, "Pills"); // Pildoras
        blackHoleIdToNameItem.put(1014, "Syringe"); // Jeringuilla
        blackHoleIdToNameItem.put(1015, "Hospital bag"); // Bolsa Hospitalar
        blackHoleIdToNameItem.put(1019, "Bolly flower");
        blackHoleIdToNameItem.put(1029, "Ballon-Handitem.."); // Globo
        blackHoleIdToNameItem.put(1031, "Torch Habbo"); // Linterna
        blackHoleIdToNameItem.put(1032, "Astronaut"); // Like Major tom in Habbo
        blackHoleIdToNameItem.put(1033, "OVNI");
        blackHoleIdToNameItem.put(1034, "Parada ET");
        blackHoleIdToNameItem.put(1035, "Spanner"); // Llave de boca
        blackHoleIdToNameItem.put(1036, "Rubber duck"); // Pato de goma
        blackHoleIdToNameItem.put(1037, "Cobra (Snake)");
        blackHoleIdToNameItem.put(1038, "Pau");
    }

    private static final TreeMap<Integer, String> flowersIdToNameItem = new TreeMap<>();
    static {
        flowersIdToNameItem.put(1000, "Rose");
        flowersIdToNameItem.put(1001, "Black rose");
        flowersIdToNameItem.put(1002, "Sunflower"); // Girasol
        flowersIdToNameItem.put(1006, "Present flower");
        flowersIdToNameItem.put(1007, "Spell weed (Cannabis)"); // i think lmaooo
        flowersIdToNameItem.put(1008, "Yellow delight"); // Flor delicia amarilla (perfumada, creo)
        flowersIdToNameItem.put(1009, "Pink pandemic");
        flowersIdToNameItem.put(1019, "Bolly flower");
        flowersIdToNameItem.put(1021, "Hyacinth"); // Flor Jacinto
        flowersIdToNameItem.put(1022, "Hyacinth 2");
    }

    private static final TreeMap<Integer, String> normalFridgeIdToNameItem = new TreeMap<>();
    static {
        normalFridgeIdToNameItem.put(3, "Carrot");
        normalFridgeIdToNameItem.put(4, "Helado de vainilla"); // Shows water, i think wtf
        normalFridgeIdToNameItem.put(5, "Milk"); // Also shows water (be careful)
        normalFridgeIdToNameItem.put(6, "Currant"); // Pasa de corinto
    }

    private static final TreeMap<Integer, String> freezeFridgeIdToNameItem = new TreeMap<>();
    static {
        freezeFridgeIdToNameItem.put(3, "Cenoura");
        freezeFridgeIdToNameItem.put(36, "Pear");
        freezeFridgeIdToNameItem.put(37, "Peach");
        freezeFridgeIdToNameItem.put(38, "Orange");
        freezeFridgeIdToNameItem.put(39, "Pineapple"); // Piña
    }

    /*Timer timerUseFurniture = new Timer(Integer.parseInt(txtDelay.getText()), e -> {
        sendToServer(new HPacket("UseFurniture", HMessage.Direction.TOSERVER, Integer.parseInt(txtFurniId.getText()), 0));
    });*/

    @Override
    protected void onShow() { // When you open the extension!
        sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0)); // When its sent, get AvatarExpression packet
        sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER)); // When its sent, get UserObject packet
        timerUseFurniture = new Timer(Integer.parseInt(txtDelay.getText()), e -> sendToServer(new HPacket("{out:UseFurniture}{i:"+txtFurniId.getText()+"}{i:0}")));
    }

    @Override
    protected void onHide() {   // When you close the extension!
        yourIndex = -1;
    }

    @Override
    protected void initExtension() { // When you install the extension or connected G-Earth to habbo
        buttonIntercept.setOnAction(event -> {
            if(buttonIntercept.getText().equals("OFF!")){
                if(!txtFurniId.getText().equals("")){
                    buttonIntercept.setText("ON!");
                    timerUseFurniture.start();
                }
            }
            else {
                buttonIntercept.setText("OFF!");
                timerUseFurniture.stop();
            }
        });

        txtDelay.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                timerUseFurniture.setDelay(Integer.parseInt(txtDelay.getText()));
            } catch (NumberFormatException e) {
                txtDelay.setText(oldValue);
            }
        });

        // Response of packet InfoRetrieve
        intercept(HMessage.Direction.TOCLIENT, "UserObject", hMessage -> {
            // Gets Name and ID in order.
            int YourID = hMessage.getPacket().readInteger();
            yourName = hMessage.getPacket().readString();
        });

        // Response of packet AvatarExpression (get UserIndex without restart room)
        intercept(HMessage.Direction.TOCLIENT, "Expression", hMessage -> {
            // First integer is index in room, second is animation id, i think
            if(primaryStage.isShowing() && yourIndex == -1){ // this could avoid any bug, i think
                yourIndex = hMessage.getPacket().readInteger();
            }
        });

        // Intercept this packet when you enter or restart a room
        intercept(HMessage.Direction.TOCLIENT, "Users", hMessage -> {
            try {
                HEntity[] roomUsersList = HEntity.parse(hMessage.getPacket());
                for (HEntity hEntity: roomUsersList){
                    if(hEntity.getName().equals(yourName)){    // In another room, the userIndex changes
                        yourIndex = hEntity.getIndex();      // The userindex has been restarted
                    }
                    //System.out.println("stuff: " + Arrays.toString(hEntity.getStuff()));
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        intercept(HMessage.Direction.TOSERVER, "UseFurniture", hMessage -> {
            if(checkFurniId.isSelected()){
                int FurniId = hMessage.getPacket().readInteger();
                txtFurniId.setText(String.valueOf(FurniId));
                hMessage.setBlocked(true);
                checkFurniId.setSelected(false);
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "CarryObject", hMessage -> {
            int currentUserIndex = hMessage.getPacket().readInteger();  // Important to detect who took the item
            int itemId = hMessage.getPacket().readInteger();
            try{ // This is for avoid any exception
                /* Look this... after!
                RadioButton[] radioButtons = new RadioButton[]{radioBlackHole, radioNormalFridge, radioFreezeFridge, radioFlowers};
                for(RadioButton radioButton: radioButtons){
                    radioButton.
                }*/
                if(currentUserIndex == yourIndex){
                    if(checkShowItemId.isSelected()){
                        sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"itemId: " + itemId+"\"}{i:0}{i:0}{i:0}{i:0}"));
                    }
                    if(radioBlackHole.isSelected()){
                        if(blackHoleIdToNameItem.get(itemId).equals(choiceBoxItems.getValue())){
                            sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"You got!: " + choiceBoxItems.getValue()+"\"}{i:0}{i:0}{i:0}{i:0}"));
                            Platform.runLater(()-> buttonIntercept.setText("OFF!"));
                            timerUseFurniture.stop();
                        }
                    }
                    else if(radioNormalFridge.isSelected()){
                        if(normalFridgeIdToNameItem.get(itemId).equals(choiceBoxItems.getValue())){
                            sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"You got!: " + choiceBoxItems.getValue()+"\"}{i:0}{i:0}{i:0}{i:0}"));
                            Platform.runLater(()-> buttonIntercept.setText("OFF!"));
                            timerUseFurniture.stop();
                        }
                    }
                    else if(radioFreezeFridge.isSelected()){
                        if(freezeFridgeIdToNameItem.get(itemId).equals(choiceBoxItems.getValue())){
                            sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"You got!: " + choiceBoxItems.getValue()+"\"}{i:0}{i:0}{i:0}{i:0}"));
                            Platform.runLater(()-> buttonIntercept.setText("OFF!"));
                            timerUseFurniture.stop();
                        }
                    }
                    else if(radioFlowers.isSelected()){
                        if(flowersIdToNameItem.get(itemId).equals(choiceBoxItems.getValue())){
                            sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"You got!: " + choiceBoxItems.getValue()+"\"}{i:0}{i:0}{i:0}{i:0}"));
                            Platform.runLater(()-> buttonIntercept.setText("OFF!"));
                            timerUseFurniture.stop();
                        }
                    }
                }
            }catch (NullPointerException exception){ System.out.println("Exception here!");}
        });
    }

    public void handleRadioButtonChanged() {
        /*choiceBoxItems.getItems().forEach(item->{
            System.out.println("item");
        });*/
        choiceBoxItems.getItems().clear();
        if(radioBlackHole.isSelected()){
            for (Map.Entry<Integer, String> entry : blackHoleIdToNameItem.entrySet()) { // Itera claves y valores de un Map!
                choiceBoxItems.getItems().add(entry.getValue());
            }
        }
        else if(radioNormalFridge.isSelected()){
            for (Map.Entry<Integer, String> entry : normalFridgeIdToNameItem.entrySet()) {
                choiceBoxItems.getItems().add(entry.getValue());    // entry.getKey();
            }        }
        else if(radioFreezeFridge.isSelected()){
            for (Map.Entry<Integer, String> entry : freezeFridgeIdToNameItem.entrySet()) {
                choiceBoxItems.getItems().add(entry.getValue());
            }
        }
        else if(radioFlowers.isSelected()){
            for (Map.Entry<Integer, String> entry : flowersIdToNameItem.entrySet()) {
                choiceBoxItems.getItems().add(entry.getValue());
            }
        }
    }
}
