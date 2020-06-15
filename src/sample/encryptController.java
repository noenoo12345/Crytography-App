package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.awt.*;
import java.io.*;

public class encryptController {

    private boolean encryptFolder = false;

    @FXML
    private Label inputKeyLabel, typeModeLabel;

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
    private Button inputPathBtn, inputKeyPathBtn, outputPathBtn, resetBtn;

    @FXML
    private Button startBtn;

    @FXML
    private TextField inputPathTxt, inputKeyPathTxt, outputPathTxt;

    @FXML
    private CheckBox checkKeyGen, checkFolder;

    // Create Alert
    private Alert alert;


    @FXML
    private void initialize(){

        progressBar.setProgress(0);

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
                    try {
                        String type = typeGroup.getSelectedToggle().getUserData().toString();
                        String hash = hashGroup.getSelectedToggle().getUserData().toString();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("decryptScene.fxml"));
                        AnchorPane pane = loader.load();
                        decryptController controller = loader.getController();
                        controller.setState(type, hash);
                        rootPane.getChildren().setAll(pane);
                    } catch (IOException e) {
                        e.printStackTrace();
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

        if (!encryptFolder) {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("All Files", "*.*")
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
        }else{
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
        }
        inputPathBtn.setDisable(false);
    }

    @FXML
    private void pressInputKeyPathBtn(ActionEvent event){
        FileChooser fc = new FileChooser();

        String typeChoice = typeGroup.getSelectedToggle().getUserData().toString();

        if (!typeChoice.equals("RSA")){
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("File Key", "*" + typeChoice + ".key")
            );
        } else {
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Private Key", "*.pvt")
            );
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
                alertWarning("No directory selected");
            }
        }
        outputPathBtn.setDisable(false);
    }

    @FXML
    private void pressResetBtn(ActionEvent event){
        inputPathTxt.setText("");
        inputKeyPathTxt.setText("");
        outputPathTxt.setText("");
        checkFolder.setSelected(false);
        checkKeyGen.setSelected(false);
        inputKeyLabel.setDisable(false);
        inputKeyPathBtn.setDisable(false);
        inputKeyPathTxt.setDisable(false);
        inputPathTxt.setPromptText("Path to input file");
    }

    @FXML
    private void pressStartBtn(ActionEvent event){
        boolean inputPath = inputPathTxt.getText().isEmpty();
        boolean inputKeyPath = inputKeyPathTxt.getText().isEmpty();
        boolean outputPath = outputPathTxt.getText().isEmpty();
        boolean checkKeyGenBool = checkKeyGen.isSelected();

        String mode = modeGroup.getSelectedToggle().getUserData().toString();

        // Check information is enough ??
        if ((inputPath | (inputKeyPath & !checkKeyGenBool) | outputPath)){
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Input/Output missing");
            alert.show();
            return;
        }

        if (!CryptAction(checkKeyGenBool)) {
            return;
        }

        if (!encryptFolder){
            alertInformation(mode + ": Successfully!");
            try {
                Desktop.getDesktop().open(new File(outputPathTxt.getText()));
            } catch (IOException e) {
                System.out.println(e.getMessage() + "File not Found");
            }
        }
    }

    @FXML
    private void checkBoxKeyAction(ActionEvent event){
        if (checkKeyGen.isSelected()){
            inputKeyPathBtn.setDisable(true);
            inputKeyLabel.setDisable(true);
            inputKeyPathTxt.setDisable(true);
            inputKeyPathTxt.setText("");
        }else {
            inputKeyLabel.setDisable(false);
            inputKeyPathBtn.setDisable(false);
            inputKeyPathTxt.setDisable(false);
        }
    }

    @FXML
    private void checkBoxFolderAction(ActionEvent event){
        encryptFolder = checkFolder.isSelected();
        inputPathTxt.setText("");
        if (encryptFolder){
            inputPathTxt.setPromptText("Path to input folder");
        }else{
            inputPathTxt.setPromptText("Path to input file");
        }
    }

    private boolean CryptAction(boolean genKeyAuto){
        // Create inputFile
        File inputFile = new File(inputPathTxt.getText());
        // Create algorithm - "DES", mode - "Encryption" and filename remove extension
        String algorithm = typeGroup.getSelectedToggle().getUserData().toString();
        String extension = inputFile.getName().substring(inputFile.getName().lastIndexOf(".") + 1);
        String hashType = hashGroup.getSelectedToggle().getUserData().toString();
        String outputSaveLocation = outputPathTxt.getText();
        String keyFromFileName = inputKeyPathTxt.getText();

        // Create name for key and output


        // Create Key
        Key key = null;
        int keySize = getKeySize(algorithm);

        // Get Key
        if (genKeyAuto){
            // If AutoGenerate
            // Create a SecureRandom object
            File selectedDirectory = null;
            alertWarning("Select folder for key file to save");
            do {
                DirectoryChooser dc = new DirectoryChooser();
                selectedDirectory = dc.showDialog(null);
                if (selectedDirectory == null){
                    alertWarning("No folder is selected!!\n" +
                            "Retry!");
                }
            }while(selectedDirectory == null);
            String keySaveLocation = selectedDirectory.getAbsolutePath();

            try {
                if (encryptFolder){
                    key = getKeyAuto(keySaveLocation, algorithm, keySize);
                }else {
                    key = getKeyAuto(keySaveLocation, algorithm, keySize);
                }
            } catch (NoSuchAlgorithmExceptionForRSA e){
                String message = "NoSuchAlgorithmException on RSA Generate Key Auto";
                alertWarning(message);
            } catch (NoSuchAlgorithmExceptionForOther e){
                String message = "NoSuchAlgorithmException on NO_RSA Generate Key Auto";
                alertWarning(message);
            }
        }else{
            // Get Key from File
            try {
                key = getKeyFromFile(keyFromFileName, algorithm);
            } catch (InvalidKeySpecEncryptException e){
                String message = "ERROR from Private Key File";
                alertWarning(message);
            }
        }

        // Encrypt

        File selectedDirectory = null;
        alertWarning("Select folder for hash file to save");
        do {
            DirectoryChooser dc = new DirectoryChooser();
            selectedDirectory = dc.showDialog(null);
            if (selectedDirectory == null){
                alertWarning("No folder is selected!! \n" +
                        "Retry!");
            }
        }while(selectedDirectory == null);
        String hashSaveLocation = selectedDirectory.getAbsolutePath();

        if (encryptFolder){
            if (algorithm.equals("RSA")) {
                File[] inputFileList = new File[inputFile.listFiles().length];
                int counter = 0;
                for (File fileEntry : inputFile.listFiles()) {
                    if (fileEntry.length() > 245) {
                        alertWarning(fileEntry.getName() + " can't encrypt because size > 245 bytes");
                    } else {
                        inputFileList[counter] = fileEntry;
                        counter++;
                    }
                }
                File[] inputFileList1 = new File[counter];
                for (int i = 0; i < counter; i++) {
                    inputFileList1[i] = inputFileList[i];
                }

                return encryptFolder(algorithm, key, inputFileList1, outputSaveLocation, hashSaveLocation, hashType);
            }else {
                return encryptFolder(algorithm, key, inputFile.listFiles(), outputSaveLocation, hashSaveLocation, hashType);
            }
        }else {
            String filename = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
            String outputFileName = getOutputFileName(outputSaveLocation, filename, hashType, algorithm);
            String hashFileName = getHashFileName(hashSaveLocation, filename, hashType);
            if (encrypt(algorithm, extension, key, inputFile, outputFileName, hashFileName)){
                progressBar.setProgress(1);
                progressIndicator.setProgress(1);
                return true;
            }else{
                return false;
            }
        }
    }

    private Key getKeyAuto(String saveLocation, String algorithm, int keySize)
            throws NoSuchAlgorithmExceptionForRSA, NoSuchAlgorithmExceptionForOther {

        SecureRandom secureRandom = new SecureRandom();
        if (algorithm == "RSA"){
            KeyPairGenerator keyPairGen = null;
            try {
                keyPairGen = KeyPairGenerator.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new NoSuchAlgorithmExceptionForRSA();
            }

            keyPairGen.initialize(keySize, secureRandom);
            // Create keyPair
            KeyPair keyPair = keyPairGen.generateKeyPair();
            PublicKey pub = keyPair.getPublic();
            PrivateKey pvt = keyPair.getPrivate();
            // Save publicKey and privateKey
            String publicKeyName = getRSAKeyName(saveLocation, algorithm, ".pub");
            String privateKeyName = getRSAKeyName(saveLocation, algorithm, ".pvt");
            File privateKeyFile = new File(privateKeyName),
                    publicKeyFile = new File(publicKeyName);

            try {
                FileOutputStream out = new FileOutputStream(privateKeyFile);
                out.write(pvt.getEncoded());
                out.close();

                out = new FileOutputStream(publicKeyFile);
                out.write(pub.getEncoded());
                out.close();
            } catch (Exception e){
                System.out.println("Error from write keyfile RSA");
            }
            return pvt;
        }else {
            // Create a KeyGenerator object
            KeyGenerator keyGen = null;
            try {
                keyGen = KeyGenerator.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new NoSuchAlgorithmExceptionForOther();
            }
            // Initialize
            keyGen.init(keySize, secureRandom);
            SecretKey key = keyGen.generateKey();
            File keyFile = new File(getKeyFileName(saveLocation, algorithm));
            try{
                FileOutputStream keyStream = new FileOutputStream(keyFile);
                keyStream.write(key.getEncoded());
                keyStream.close();
            }catch (IOException e){
                System.out.println("Error write key file");
            }
            // Create a key
            return key;
        }
    }

    private Key getKeyFromFile(String keyFromFileName, String algorithm)
            throws InvalidKeySpecEncryptException{

        try {
            File keyFile = new File(keyFromFileName);

            FileInputStream fis = new FileInputStream(keyFile);

            byte[] byteArray = new byte[(int)keyFile.length()];
            fis.read(byteArray);

            // If RSA
            if (algorithm == "RSA") {
                // Encryption -> private Key
                Key keyExtract = null;

                PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(byteArray);
                try {
                    KeyFactory kf = KeyFactory.getInstance(algorithm);
                    keyExtract = kf.generatePrivate(ks);
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeySpecEncryptException();
                } catch (NoSuchAlgorithmException e){
                    System.out.println("No Such Algorithm RSA Encryption");
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

    private boolean encrypt(String algorithm, String extension, Key key, File inputFile, String outputFileName, String hashFileName){
        // Get some values
        String hashType = hashGroup.getSelectedToggle().getUserData().toString();
        MessageDigest md = null;
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        FileOutputStream hashStream = null;

        try {
            // Initialize
            md = MessageDigest.getInstance(hashType);
            inputStream = new FileInputStream(inputFile);

            Cipher cipher;
            if (algorithm != "RSA") {
                cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");       // PKCS5Padding (64 bits)
            } else {
                cipher = Cipher.getInstance(algorithm);
            }

            cipher.init(Cipher.ENCRYPT_MODE, key);

            // Encrypt inputFile to outputFile

//            byte[] extensionArray = extension.getBytes();       // extension
            byte[] extensionArray;
            if (extension.length() == 3){
                extensionArray = (extension + " ").getBytes();
            }else {
                extensionArray = extension.getBytes();
            }


            byte[] inputBytes = new byte[(int)inputFile.length()];
            inputStream.read(inputBytes);
            byte[] inputByteCombine = new byte[inputBytes.length + extensionArray.length];

            System.arraycopy(extensionArray,  0, inputByteCombine, 0, extensionArray.length);
            System.arraycopy(inputBytes, 0, inputByteCombine, extensionArray.length, inputBytes.length);
            byte[] outputBytes = cipher.doFinal(inputByteCombine);
            outputStream = new FileOutputStream(new File(outputFileName));
            outputStream.write(outputBytes);

            // Hash input file and write to file
            byte[] hashedBytes = md.digest(inputBytes);
            hashStream = new FileOutputStream(new File(hashFileName));
            hashStream.write(hashedBytes);

        } catch (InvalidKeyException | NoSuchPaddingException e) {
            if (!encryptFolder) {
                alertWarning("Key is not relevant");
            }
            return false;
        } catch (BadPaddingException e) {   // doFinal
            if (!encryptFolder) {
                alertWarning("File input is not relevant");
            }
            return false;
        } catch (IllegalBlockSizeException e) {
            if (!encryptFolder) {
                alertWarning("Illegal Block Size Exception");
            }
            return false;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("getInstance" + algorithm);
            return false;
        } catch (IOException e){
            System.out.println("Save file Exception");
            return false;
        } finally {
            // Close all stream
            try{
                if (hashStream != null)
                    hashStream.close();
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            }catch (Exception e){}
        }
        return true;
    }

    private boolean encryptFolder(String algorithm, Key key, File[] inputFolder, String outputSaveLocation, String hashSaveLocation, String hashType){
        // Get some values

        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                int length = inputFolder.length;
                double progress = 0;
                String mess = "";
                for (File fileEntry : inputFolder) {
                    String extension = fileEntry.getName().substring(fileEntry.getName().lastIndexOf(".") + 1);
                    String fileName = fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf("."));
                    String outputFileName = getOutputFileName(outputSaveLocation, fileName, hashType, algorithm);
                    String hashFileName = getHashFileName(hashSaveLocation, fileName, hashType);
                    if (!(encrypt(algorithm, extension, key, fileEntry, outputFileName, hashFileName))){
                        mess = mess + fileEntry.getName() + ",";
                        updateMessage(mess);        // update files can't encrypt
                    }
                    progress = progress + 1.0/length;
                    updateProgress(progress, 1);
                }
                return null;
            }
        };
        // When task is over.
        task.setOnSucceeded(e->{
            String message = task.getMessage();
            if (!message.equals("")){
                message = message.substring(0, message.length() - 1);
                alertWarning("File: " + message + " can't encrypt");
            }
            alertInformation("Encryption is finished!");
            try {
                Desktop.getDesktop().open(new File(outputPathTxt.getText()));
            } catch (IOException event) {
                System.out.println(event.getMessage() + "File not Found");
            }
        });
        // Bind value with task.progressProperty()
        progressBar.progressProperty().bind(task.progressProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
        return true;
    }

    private String getKeyFileName(String saveLocation, String algorithm){
        return  saveLocation
                + "\\"
                + algorithm
                + ".key";
    }

    private String getOutputFileName(String saveLocation, String filename, String hashType, String algorithm){
        return saveLocation
                + "\\"
                + filename
                + "."
                + algorithm.toLowerCase();
    }

    private String getHashFileName(String saveLocation, String filename, String hashType){
        return  saveLocation
                + "\\"
                + filename
                + "_"
                + hashType
                + ".hsh";
    }

    private String getRSAKeyName(String saveLocation, String algorithm, String extension){
        return saveLocation
                + "\\"
                + algorithm
                + extension;
    }

    private int getKeySize(String algorithm){
        int keySize = 0;
        switch (algorithm){
            case "AES":
                keySize = 256;
                break;
            case "DES":
                keySize = 56;
                break;
            case "RSA":
                keySize = 1024;
                break;
        }
        return keySize;
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

    private class NoSuchAlgorithmExceptionForRSA extends NoSuchAlgorithmException{}

    private class NoSuchAlgorithmExceptionForOther extends NoSuchAlgorithmException{}

    private class InvalidKeySpecEncryptException extends InvalidKeySpecException{}

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
