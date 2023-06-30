import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HEntity;
import gearth.extensions.parsers.HEntityUpdate;
import gearth.extensions.parsers.HPoint;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

import javax.swing.*;

import java.util.Map;
import java.util.TreeMap;

@ExtensionInfo(
        Title = "GHandItemChooser",
        Description = "Developed by doraemon in Python", // (Initially)
        Version = "1.1.5",
        Author = "Julianty"
)


public class GHandItemChooser extends ExtensionForm{

    public Button buttonIntercept;
    public TextField txtDelay;
    public CheckBox checkFurniId, checkShowItem;
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
        blackHoleIdToNameItem.put(39, "Orange"); // It's repeated, why?
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
        blackHoleIdToNameItem.put(1051, "Brush");
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

    public ListView <String> listViewShopping;

    public ListView<String> listViewToBuy;


    public Button buttonDeleteItem;
    public CheckBox checkStartPoint, checkEndPoint, checkDoorId;

    int furniIdSelected = -1;
    public HPoint startPoint = new HPoint(-1, -1), endPoint = new HPoint(-1, -1);
    public int doorId = -1;
    int currentIndexSelected = -1;


    @Override
    protected void onShow() { // When you open the extension!
        sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0)); // When its sent, get AvatarExpression packet
        sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER)); // When its sent, get UserObject packet

        timerUseFurniture = new Timer(Integer.parseInt(txtDelay.getText()), e -> {
            sendToServer(new HPacket(String.format("{out:UseFurniture}{i:%s}{i:0}", furniIdSelected)));
        });
    }

    @Override
    protected void onHide() {   // When you close the extension!
        yourIndex = -1;
    }

    @Override
    protected void initExtension() { // When you install the extension or connected G-Earth to habbo

        listViewShopping.setOnDragDetected(event -> {
            Dragboard db = listViewShopping.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(listViewShopping.getSelectionModel().getSelectedItem());
            db.setContent(content);
            event.consume();
        });

        listViewToBuy.setOnDragOver(event -> {
            if (event.getGestureSource() != listViewToBuy && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        listViewToBuy.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if(!listViewToBuy.getItems().contains(db.getString())){
                listViewToBuy.getItems().add(db.getString());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        buttonIntercept.setOnAction(event -> {
            if(buttonIntercept.getText().equals("OFF!")){
                if(furniIdSelected != -1){
                    buttonIntercept.setText("ON!"); buttonIntercept.setTextFill(Color.GREEN);
                }
            }
            else {
                turnOffButton();
            }
        });

        listViewToBuy.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentIndexSelected = listViewToBuy.getSelectionModel().getSelectedIndex();
        });

        buttonDeleteItem.setOnAction(event -> {
            if(currentIndexSelected != -1){
                listViewToBuy.getItems().remove(currentIndexSelected); // The getSelectionModel() event will be fired
                listViewToBuy.getSelectionModel().select(-1);
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

        intercept(HMessage.Direction.TOCLIENT, "UserUpdate", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            for (HEntityUpdate hEntityUpdate: HEntityUpdate.parse(hPacket)){
                int currentIndex = hEntityUpdate.getIndex();
                HPoint currentPosition = hEntityUpdate.getMovingTo();
                if(yourIndex == currentIndex && currentPosition != null){
                    if(currentPosition.getX() == endPoint.getX() && currentPosition.getY() == endPoint.getY()){
                        timerUseFurniture.setInitialDelay(0); // Inicia inmediatamente el timer
                        timerUseFurniture.start();
                    }
                }
            }
        });

        intercept(HMessage.Direction.TOSERVER, "ClickFurni", hMessage -> {
            int furnitureId = hMessage.getPacket().readInteger();
            if(checkFurniId.isSelected()){
                furniIdSelected = furnitureId;
                Platform.runLater(() -> checkFurniId.setText(String.format("Get Furni Id (%s)", furniIdSelected)));
                checkFurniId.setSelected(false);
            }
            else if(checkDoorId.isSelected()){
                doorId = furnitureId;
                Platform.runLater(() -> checkDoorId.setText(String.format("Get Door Id (%s)", doorId)));
                checkDoorId.setSelected(false);
            }
        });

        intercept(HMessage.Direction.TOSERVER, "MoveAvatar", hMessage -> {
            HPoint hPoint = new HPoint(hMessage.getPacket().readInteger(), hMessage.getPacket().readInteger());
            if(checkStartPoint.isSelected()){
                startPoint = hPoint;
                Platform.runLater(() -> checkStartPoint.setText(String.format("Start Point: (%s, %s)", startPoint.getX(), startPoint.getY())));
                checkStartPoint.setSelected(false);
                hMessage.setBlocked(true);
            }
            else if(checkEndPoint.isSelected()){
                endPoint = hPoint;
                Platform.runLater(() -> checkEndPoint.setText(String.format("End Point: (%s, %s)", endPoint.getX(), endPoint.getY())));
                checkEndPoint.setSelected(false);
                hMessage.setBlocked(true);
            }
        });

        // Cuando se cambia el estado de un furni mediante wired
        intercept(HMessage.Direction.TOCLIENT, "ObjectsDataUpdate", hMessage -> {
            // {in:ObjectsDataUpdate}
            // {i:8}
            // {i:2147418981}{i:0}{s:"5"}
            // {i:2147418286}{i:0}{s:"6"}
            // {i:2147418215}{i:0}{s:"3"} ...

            int count = hMessage.getPacket().readInteger();
            for(int i = 0; i < count; i++){
                int furnitureId = hMessage.getPacket().readInteger();
                int idk = hMessage.getPacket().readInteger();
                String state = hMessage.getPacket().readString(); // 0 = cerrado, 1 = abierto para puertas

                if(furnitureId == doorId && state.equals("1")){
                    sendToServer(new HPacket(String.format("{out:MoveAvatar}{i:%s}{i:%s}", endPoint.getX(), endPoint.getY())));
                }
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "CarryObject", hMessage -> {
            int currentUserIndex = hMessage.getPacket().readInteger();  // Important to detect who took the item
            int itemId = hMessage.getPacket().readInteger();

            try{
                if(currentUserIndex == yourIndex){
                    String nameItem = null;
                    if(radioFlowers.isSelected()){
                        nameItem = flowersIdToNameItem.get(itemId);
                    }
                    else if(radioBlackHole.isSelected()){
                        nameItem = blackHoleIdToNameItem.get(itemId);
                    }
                    else if(radioNormalFridge.isSelected()){
                        nameItem = normalFridgeIdToNameItem.get(itemId);
                    }
                    else if(radioFreezeFridge.isSelected()){
                        nameItem = freezeFridgeIdToNameItem.get(itemId);
                    }

                    if(checkShowItem.isSelected()){
                        sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"nameItem: " + nameItem + "\"}{i:0}{i:0}{i:0}{i:0}"));

                        // For testing purposes
                        // if(nameItem == null){ Platform.runLater(this::turnOffButton); }
                    }

                    if(listViewToBuy.getItems().contains(nameItem)){
                        sendToClient(new HPacket("{in:Chat}{i:-1}{s:\"You got!: " + nameItem + "\"}{i:0}{i:0}{i:0}{i:0}"));

                        Platform.runLater(this::turnOffButton);
                        sendToServer(new HPacket(String.format("{out:MoveAvatar}{i:%d}{i:%d}", startPoint.getX(), startPoint.getY())));
                    }
                }
            }catch (NullPointerException exception){ System.out.println("Exception here!");}
        });
    }

    public void turnOffButton(){
        buttonIntercept.setText("OFF!");    buttonIntercept.setTextFill(Color.RED); timerUseFurniture.stop();
    }

    public void handleRadioButtonChanged() {
        listViewShopping.getItems().clear();
        if(radioBlackHole.isSelected()){
            for (Map.Entry<Integer, String> entry : blackHoleIdToNameItem.entrySet()) {
                listViewShopping.getItems().add(entry.getValue());
            }
        }
        else if(radioNormalFridge.isSelected()){
            for (Map.Entry<Integer, String> entry : normalFridgeIdToNameItem.entrySet()) {
                listViewShopping.getItems().add(entry.getValue());
            }
        }
        else if(radioFreezeFridge.isSelected()){
            for (Map.Entry<Integer, String> entry : freezeFridgeIdToNameItem.entrySet()) {
                listViewShopping.getItems().add(entry.getValue());
            }
        }
        else if(radioFlowers.isSelected()){
            for (Map.Entry<Integer, String> entry : flowersIdToNameItem.entrySet()) {
                listViewShopping.getItems().add(entry.getValue());
            }
        }
    }
}
