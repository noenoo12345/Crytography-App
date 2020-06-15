package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class decryptController {

    private String finalMessage = "";

    @FXML
    private Label typeModeLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private ToggleGroup typeGroup, modeGroup, hashGroup;

    @FXML
    private ToggleButton DESBtn, AESBtn, RSABtn;
    @FXML
    private ToggleButton MD5Btn, SHA1Btn, SHA256Btn;

    @FXML
    private ToggleButton encryptBtn, decryptBtn;

    @FXML
    private Button inputPathBtn, inputKeyPathBtn, outputPathBtn, hashPathBtn;

    @FXML
    private Button startBtn;

    @FXML
    private TextField inputPathTxt, inputKeyPathTxt, outputPathTxt, hashPathTxt;

    @FXML
    private CheckBox checkFolder;

    //  Create alert
    private Alert alert;
    boolean decryptFolder = false;


    @FXML
    private void initialize(){

        // Type Toggle Buttons
        DESBtn.setToggleGroup(typeGroup);
        DESBtn.setUserData("DES");
        AESBtn.setToggleGroup(typeGroup);
        AESBtn.setUserData("AES");
        RSABtn.setToggleGroup(typeGroup);
        RSABtn.setUserData("RSA");

        typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle old_toggle, Toggle new_toggle) {
                if (typeGroup.getSelectedToggle() != null){
                    String type = new_toggle.getUserData().toString();
                    String hash = hashGroup.getSelectedToggle().getUserData().toString();
                    typeModeLabel.setText(type + " " + hash);
                    inputPathTxt.setText("");
                    inputKeyPathTxt.setText("");
                } else{
                    old_toggle.setSelected(true);
                }
            }
        });

        // Hash Toggle Buttons
        MD5Btn.setToggleGroup(hashGroup);
        MD5Btn.setUserData("MD5");
        SHA1Btn.setToggleGroup(hashGroup);
        SHA1Btn.setUserData("SHA-1");
        SHA256Btn.setToggleGroup(hashGroup);
        SHA256Btn.setUserData("SHA-256");

        hashGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle old_toggle, Toggle new_toggle) {
                if (hashGroup.getSelectedToggle() != null){
                    String hash = new_toggle.getUserData().toString();
                    String type = typeGroup.getSelectedToggle().getUserData().toString();
                    typeModeLabel.setText(type + " " + hash);
                    hashPathTxt.setText("");
                } else{
                    old_toggle.setSelected(true);
                }
            }
        });

        // Mode Toggle Buttons
        encryptBtn.setToggleGroup(modeGroup);
        encryptBtn.setUserData("Encryption");
        decryptBtn.setToggleGroup(modeGroup);
        decryptBtn.setUserData("Decryption");

        modeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle old_toggle, Toggle new_toggle) {
                if (modeGroup.getSelectedToggle() != null){
                    if (modeGroup.getSelectedToggle().getUserData().toString() == "Encryption") {
                        try {
                            String type = typeGroup.getSelectedToggle().getUserData().toString();
                            String hash = hashGroup.getSelectedToggle().getUserData().toString();
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("encryptScene.fxml"));
                            AnchorPane pane = loader.load();
                            encryptController controller = loader.getController();
                            controller.setState(type, hash);
                            rootPane.getChildren().setAll(pane);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    old_toggle.setSelected(true);
                }
            }
        });

    }

    @FXML
    private void pressInputPathBtn(ActionEvent event){
        inputPathBtn.setDisable(true);

        String algorithm = typeGroup.getSelectedToggle().getUserData().toString();
        if (decryptFolder) {
            DirectoryChooser dc = new DirectoryChooser();
            File selectedDirectory = dc.showDialog(null);

            if (selectedDirectory != null){
                inputPathTxt.setText(selectedDirectory.getAbsolutePath());
            }else{
                if (inputPathTxt.getText().isEmpty()) {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("No folder selected");
                    alert.show();
                }
            }
        } else{

            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Cipher Files", "*." + algorithm)
            );
            File selectedFile = fc.showOpenDialog(null);

            if (selectedFile != null) {
                inputPathTxt.setText(selectedFile.getAbsolutePath());
            }else{
                if (inputPathTxt.getText().isEmpty()) {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("No file selected");
                    alert.show();
                }
            }
        }

        inputPathBtn.setDisable(false);
    }

    @FXML
    private void pressInputKeyPathBtn(ActionEvent event){
        FileChooser fc = new FileChooser();

        String typeChoice = typeGroup.getSelectedToggle().getUserData().toString();
        String mode = modeGroup.getSelectedToggle().getUserData().toString();

        if (typeChoice != "RSA"){
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("File Key", "*" + typeChoice + ".key")
            );
        } else {
            if (mode == "Encryption") {
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Public Key", "*.pvt")
                );
            } else{
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Public Key", "*.pub")
                );
            }
        }

        inputKeyPathBtn.setDisable(true);
        File selectedFile = fc.showOpenDialog(null);

        if (selectedFile != null) {
            inputKeyPathTxt.setText(selectedFile.getAbsolutePath());
        }else{
            if (inputKeyPathTxt.getText().isEmpty()) {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("No key file selected");
                alert.show();
            }
        }
        inputKeyPathBtn.setDisable(false);
    }

    @FXML
    private void pressOutputPathBtn(ActionEvent event){
        DirectoryChooser dc = new DirectoryChooser();

        outputPathBtn.setDisable(true);
        File selectedDirectory = dc.showDialog(null);

        if (selectedDirectory != null){
            outputPathTxt.setText(selectedDirectory.getAbsolutePath());
        }else{
            if (outputPathTxt.getText().isEmpty()){
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("No directory selected");
                alert.show();
            }
        }
        outputPathBtn.setDisable(false);
    }

    @FXML
    private void pressHashPathBtn(ActionEvent event){
        hashPathBtn.setDisable(true);
        String hashType = hashGroup.getSelectedToggle().getUserData().toString();
        if (decryptFolder){
            DirectoryChooser dc = new DirectoryChooser();
            File selectedDirectory = dc.showDialog(null);

            if (selectedDirectory != null){
                hashPathTxt.setText(selectedDirectory.getAbsolutePath());
            }else{
                if (hashPathTxt.getText().isEmpty()){
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("No directory selected");
                    alert.show();
                }
            }
        } else {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(
                            "Hash Files",
                            "*"
                                    + hashType
                                    + ".hsh")
            );

            File selectedFile = fc.showOpenDialog(null);

            if (selectedFile != null) {
                hashPathTxt.setText(selectedFile.getAbsolutePath());
            } else {
                if (hashPathTxt.getText().isEmpty()) {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("No file selected");
                    alert.show();
                }
            }
        }
        hashPathBtn.setDisable(false);
    }

    @FXML
    private void pressStartBtn(ActionEvent event){
        boolean inputPath = inputPathTxt.getText().isEmpty();
        boolean inputKeyPath = inputKeyPathTxt.getText().isEmpty();
        boolean outputPath = outputPathTxt.getText().isEmpty();
        boolean hashPath = hashPathTxt.getText().isEmpty();

        String mode = modeGroup.getSelectedToggle().getUserData().toString();
        finalMessage = mode + ": Successfully!";

        // Check information is enough ??
        if ((inputPath | inputKeyPath | outputPath | hashPath)){
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Input/Output missing");
            alert.show();
            return;
        }
        if (!CryptAction()) {
            return;
        }

        if (!decryptFolder){
            alertInformation(mode
                    + ": Successfully!\n"
                    + "Data INTEGRITY!"
            );
            try {
                Desktop.getDesktop().open(new File(outputPathTxt.getText()));
            } catch (IOException e) {
                System.out.println(e.getMessage() + "File not Found");
            }
        }
    }

    @FXML
    private void pressResetBtn(ActionEvent event){
        inputPathTxt.setText("");
        inputKeyPathTxt.setText("");
        hashPathTxt.setText("");
        outputPathTxt.setText("");
        checkFolder.setSelected(false);
        inputPathTxt.setPromptText("Path to input file");
        hashPathTxt.setPromptText("Path to hash file");
    }

    @FXML
    private void checkBoxFolderAction(ActionEvent event){
        decryptFolder = checkFolder.isSelected();
        inputPathTxt.setText("");
        hashPathTxt.setText("");
        if (decryptFolder){
            inputPathTxt.setPromptText("Path to input folder");
            hashPathTxt.setPromptText("Path to hash folder");
        }else{
            inputPathTxt.setPromptText("Path to input file");
            hashPathTxt.setPromptText("Path to hash file");
        }
    }

    private boolean CryptAction(){
        // Create inputFile
        File inputFile = new File(inputPathTxt.getText());
        File hashFile = new File(hashPathTxt.getText());
        // Create algorithm - "DES", mode - "Encryption" and filename remove extension
        String algorithm = typeGroup.getSelectedToggle().getUserData().toString();
        String filename = inputFile.getName().substring(0, inputFile.getName().length() - 4);
        String saveLocation = outputPathTxt.getText();
        String keyFromFileName = inputKeyPathTxt.getText();

        // Create Key
        Key key = null;

            // Get Key from File
        try {
            key = getKeyFromFile(keyFromFileName, algorithm);
        } catch (InvalidKeySpecDecryptException e){
            String message = "ERROR from Private Key File";
            alertWarning(message);
        }

        // Encrypt or Decrypt
        if (decryptFolder){
            return decryptFolder(saveLocation, algorithm, key, inputFile, hashFile);
        } else{
            if (decrypt(saveLocation, filename, algorithm, key, inputFile, hashFile)){
                progressBar.setProgress(1);
                progressIndicator.setProgress(1);
                return true;
            }else{
                return false;
            }
        }
    }

    private Key getKeyFromFile(String keyFromFileName, String algorithm)
            throws InvalidKeySpecDecryptException{

        try {
            File keyFile = new File(keyFromFileName);

            FileInputStream fis = new FileInputStream(keyFile);

            byte[] byteArray = new byte[(int)keyFile.length()];
            fis.read(byteArray);

            // If RSA
            if (algorithm == "RSA") {
                // Encryption -> private Key
                Key keyExtract = null;
                X509EncodedKeySpec ks = new X509EncodedKeySpec(byteArray);
                try {
                    KeyFactory kf = KeyFactory.getInstance(algorithm);
                    keyExtract = kf.generatePublic(ks);
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeySpecDecryptException();
                } catch (NoSuchAlgorithmException e){
                    System.out.println("No Such Algorithm RSA Decryption");
                }
                return keyExtract;
            } else {
                return new SecretKeySpec(byteArray, algorithm);
            }
        } catch (IOException e){
            System.out.println(e.getMessage() + "Read File");
        }
        return null;
    }

    private boolean decrypt(String saveLocation, String filename, String algorithm, Key key, File inputFile, File hashFile){
        // Get some values
        String hashType = hashGroup.getSelectedToggle().getUserData().toString();
        FileInputStream inputStream = null;
        FileInputStream hashStream = null;
        FileOutputStream outputStream = null;

        try {
            // Initialize
            inputStream = new FileInputStream(inputFile);
            hashStream = new FileInputStream(hashFile);


            Cipher cipher;
            if (algorithm != "RSA") {
                cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");       // PKCS5Padding (64 bits)
            } else {
                cipher = Cipher.getInstance(algorithm);
            }
            // Initialize
            cipher.init(Cipher.DECRYPT_MODE, key);
            MessageDigest md = MessageDigest.getInstance(hashType);

            // Decrypt inputFile to outputFile
            byte[] inputBytes = new byte[(int)inputFile.length()];
            inputStream.read(inputBytes);
            byte[] outputBytes = cipher.doFinal(inputBytes);
            byte[] extensionNameArray = Arrays.copyOfRange(outputBytes, 0, 4);
            byte[] outputByteData = Arrays.copyOfRange(outputBytes, 4, outputBytes.length);

            // hash output file and check
            byte[] hashBytes = new byte[(int)hashFile.length()];
            hashStream.read(hashBytes);
            byte[] outputHashedBytes = md.digest(outputByteData);

            if (!Arrays.equals(hashBytes, outputHashedBytes)) {
                if (!decryptFolder) {
                    alertWarning("File is NOT integrity!! OR Hash File is Wrong");
                }
                return false;
            }

            String extensionName = new String(extensionNameArray);
            String outputFileName = getOutputFileNameDec(saveLocation, filename, algorithm, extensionName.trim());
            File outputFile = new File(outputFileName);
            outputStream = new FileOutputStream(outputFile);
            // Write output to file
            outputStream.write(outputByteData);
        } catch (InvalidKeyException | NoSuchPaddingException e) {
            if (!decryptFolder) {
                alertWarning("Key is not relevant");
            }
            return false;
        } catch (BadPaddingException e) {   // doFinal
            if (!decryptFolder) {
                alertWarning("File input is not relevant");
            }
            return false;
        } catch (IllegalBlockSizeException e) {
            if (!decryptFolder) {
                alertWarning("Illegal Block Size Exception");
            }
            return false;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("getInstance" + algorithm);
            return false;
        } catch (IOException e) {
            System.out.println("Save file Exception");
            return false;
        }finally {
            try {
                // Close all stream
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (hashStream != null)
                    hashStream.close();
            } catch (Exception e){ }
        }
        return true;
    }

    private boolean decryptFolder(String saveLocation, String algorithm, Key key, File inputFolder, File hashFolder){
        // Get some values

        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                int length = inputFolder.listFiles().length;
                double progress = 0;
                String mess = "";
                for (File inputFileEntry : inputFolder.listFiles()){
                    // Get inputFileExtension from list file input -> take this
                    File hashFile = null;
                    String inputFileExtension = inputFileEntry.getName().substring(inputFileEntry.getName().length() - 3);
                    String hashFileName1 = inputFileEntry.getName().substring(0, inputFileEntry.getName().length() - 4);
                    if (inputFileExtension.equals(algorithm.toLowerCase())){
                        for (File hashFileEntry : hashFolder.listFiles()){
                            if (hashFileEntry.getName().length() > hashFileName1.length()) {
                                String hashFileName2 = hashFileEntry.getName().substring(0, hashFileName1.length());
                                String fileExtension = hashFileEntry.getName().substring(hashFileEntry.getName().length() - 3);
                                // type of file's hash
                                String hashType1 = hashFileEntry.getName().substring(hashFileName1.length() + 1, hashFileEntry.getName().lastIndexOf("."));
                                String hashType2 = hashGroup.getSelectedToggle().getUserData().toString();  //type of hash used
                                // If found hashFile appropriate
                                if (hashFileName1.equals(hashFileName2) && hashType1.equals(hashType2) && fileExtension.equals("hsh")) {
                                    hashFile = hashFileEntry;
                                    break;
                                }
                            }
                        }
                    }
                    if (hashFile != null){
                        if (!decrypt(saveLocation, hashFileName1, algorithm, key, inputFileEntry, hashFile)){
                            mess = mess + hashFileName1 + ",";
                            updateMessage(mess);        // update files can't decrypt
                        }
                    }
                    progress = progress + 1.0/length;
                    updateProgress(progress, 1);
                }
                return null;
            }
        };

        // When task is over
        task.setOnSucceeded(e->{
            String message = task.getMessage();
            if (!message.equals("")) {
                message = message.substring(0, message.length() - 1);
                alertWarning("File: " + message + " can't decrypt!");
            }
            alertInformation("Decryption is finished!");
            try {
                Desktop.getDesktop().open(new File(outputPathTxt.getText()));
            } catch (IOException event) {
                System.out.println(event.getMessage() + "File not Found");
            }
        });
        progressBar.progressProperty().bind(task.progressProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());

        new Thread(task).start();
        return true;
    }

    private String getOutputFileNameDec(String saveLocation, String filename, String algorithm, String extension){
        return saveLocation
                + "\\"
                + "Decrypted"
                + "_"
                + filename
                + "_"
                + algorithm
                + "."
                + extension;
    }

    private void alertWarning(String message){
        alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void alertInformation(String message){
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Message");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class InvalidKeySpecDecryptException extends InvalidKeySpecException{}

    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public void setState(String type, String hash){
        if (type == "DES"){
            DESBtn.setSelected(true);
        }else if (type == "AES"){
            AESBtn.setSelected(true);
        }else{
            RSABtn.setSelected(true);
        }

        if (hash == "MD5"){
            MD5Btn.setSelected(true);
        }else if (hash == "SHA-1"){
            SHA1Btn.setSelected(true);
        }else{
            SHA256Btn.setSelected(true);
        }
    }
}
