package password.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.security.SecureRandom;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    @FXML
    private Button generateButton;

    @FXML
    private Label lengthLabel;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private Label passwordCountLabel;

    @FXML
    private Label toastLabel;

    @FXML
    private ListView<String> passwordListView;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private RadioButton symbolRadioButton;

    @FXML
    private RadioButton numberRadioButton;

    @FXML
    private RadioButton lowercaseRadioButton;

    @FXML
    private RadioButton uppercaseRadioButton;

    @FXML
    private TextField numberOfPasswordsTextField;

    @FXML
    private TextField lengthTextField;

    @FXML
    private StackPane toast;

    private final int TOTAL_NUMBERS = 10;
    private final int TOTAL_SYMBOLS = 30;
    private final int TOTAL_LETTERS = 26;
    private final int MAX_GENERATED_PASSWORDS = 100;
    private final int DEFAULT_LENGTH = 12;

    private final DecimalFormat TO_HUND = new DecimalFormat("#.##");
    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    private boolean useNumbers;
    private boolean useSymbols;
    private boolean useLowercase;
    private boolean useUppercase;

    private int length;
    private int minLength;
    private int maxLength;
    private int numberOfPasswords;
    private int maxCombosPerLetter;
    private int maxPasswordsCombos;

    private double colorValue;
    private double strength;

    @FXML
    private void initialize() {
        passwordListView.setCellFactory(param -> new CustomListCell());

        lengthTextField.setText(Integer.toString(DEFAULT_LENGTH));

        lengthTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,9}"))
                lengthTextField.setText(oldValue);

            updateValues();
        }));

        lengthTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                generatePassword();
        });

        lengthTextField.setOnMouseClicked(event -> lengthTextField.selectAll());

        numberOfPasswordsTextField.setText(Integer.toString(1));

        numberOfPasswordsTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,9}"))
                numberOfPasswordsTextField.setText(oldValue);

            updateValues();
        }));

        numberOfPasswordsTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                generatePassword();
        });

        numberOfPasswordsTextField.setOnMouseClicked(event -> numberOfPasswordsTextField.selectAll());

        symbolRadioButton.setSelected(true);
        numberRadioButton.setSelected(true);
        lowercaseRadioButton.setSelected(true);
        uppercaseRadioButton.setSelected(true);

        updateValues();
        generatePassword();
    }

    @FXML
    private void updateValues() {
        minLength = 0;
        maxLength = 0;
        maxPasswordsCombos = 0;
        maxCombosPerLetter = 0;

        useNumbers = numberRadioButton.isSelected();
        useSymbols = symbolRadioButton.isSelected();
        useLowercase = lowercaseRadioButton.isSelected();
        useUppercase = uppercaseRadioButton.isSelected();

        length = getIntegerTextFieldValue(lengthTextField);
        numberOfPasswords = getIntegerTextFieldValue(numberOfPasswordsTextField);

        colorValue = 100.0;
        strength = 0;

        boolean didInputLengths = length > 0 && numberOfPasswords > 0;

        updateValuesDriver(useNumbers, TOTAL_NUMBERS, didInputLengths);
        updateValuesDriver(useSymbols, TOTAL_SYMBOLS, didInputLengths);
        updateValuesDriver(useLowercase, TOTAL_LETTERS, didInputLengths);
        updateValuesDriver(useUppercase, TOTAL_LETTERS, didInputLengths);

        maxPasswordsCombos = (int)Math.pow(maxCombosPerLetter, length);
        if (maxPasswordsCombos > MAX_GENERATED_PASSWORDS) {
            maxPasswordsCombos = MAX_GENERATED_PASSWORDS;
        }

        //Length
        if ((length < minLength) || (length > maxLength)) {
            colorValue = 100.0;
            strength = 0;
        } else if (didInputLengths) {
            double increment = 20.0 * Math.min(1.0, length / (maxLength < 12 ? maxLength : 12.0));
            colorValue -= increment;
            strength += increment;
        }

        generateButton.setDisable(isInputInvalid());

        lengthLabel.setText("- " + (minLength == 0 ? "..." : minLength) + " < length <= " + (maxLength == 0 ? "..." : maxLength));
        passwordCountLabel.setText("- 0 < number of passwords <= " + (maxPasswordsCombos == 0  ? "..." : maxPasswordsCombos));

        passwordStrengthLabel.setText(TO_HUND.format(strength) + "%");
        passwordStrengthLabel.setTextFill(new Color((255 * colorValue) / 25500, (255 * (100 - colorValue)) / 25500, 0, 1));
    }

    @FXML
    private void generatePassword() {
        if(isInputInvalid())
            return;

        final AtomicInteger NUMBER_OF_PASSWORDS = new AtomicInteger(numberOfPasswords);
        final Integer[] ASCII_VALUES = getSelectedASCIIValues(useNumbers, useSymbols, useLowercase, useUppercase);
        AtomicInteger totalPasswords = new AtomicInteger(0);

        List<String> generatedPasswords = new ArrayList<>();

        disableInput();

        passwordListView.getItems().clear();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            StringBuilder password;

            //Validating password
            do{
                List<Integer> asciiValuesCopy = new ArrayList<>(Arrays.asList(ASCII_VALUES));
                password = new StringBuilder();

                //Generating password.
                for (int j = 0; j < length; j++) {
                    int index = SECURE_RANDOM.nextInt(asciiValuesCopy.size());
                    int asciiValue = asciiValuesCopy.get(index);

                    password.append((char) asciiValue);

                    asciiValuesCopy.remove(index);
                }

            }while(Collections.synchronizedList(generatedPasswords).contains(password.toString()) ||
                    isPasswordInvalid(password.toString(), useNumbers, useSymbols, useLowercase, useUppercase));

            String finalPassword = password.toString();
            generatedPasswords.add(finalPassword);
            Platform.runLater(() -> passwordListView.getItems().add(finalPassword));

            totalPasswords.getAndIncrement();

            progressIndicator.setProgress(totalPasswords.doubleValue() / numberOfPasswords);

            if(totalPasswords.intValue() == NUMBER_OF_PASSWORDS.intValue()) {
                progressIndicator.setProgress(1);

                scheduledExecutorService.schedule(() -> progressIndicator.setProgress(0), 1, TimeUnit.MILLISECONDS);

                passwordListView.refresh();

                enableInput();

                System.gc();

                scheduledExecutorService.shutdown();
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    private boolean isInputInvalid() {
        return length == 0 || length < minLength || length > maxLength || numberOfPasswords == 0
                || numberOfPasswords > maxPasswordsCombos || (!useNumbers && !useSymbols && !useLowercase && !useUppercase);
    }

    private boolean isPasswordInvalid(String password, boolean numbers, boolean symbols, boolean lowercase, boolean uppercase) {

        boolean hasNumbers = false;
        boolean hasSymbols = false;
        boolean hasLowercase = false;
        boolean hasUppercase= false;

        //Check numbers
        for (int i = 0; i < password.length(); i++) {
            int charValue = password.charAt(i);
            hasNumbers = charValue > 47 && charValue < 58;

            if(hasNumbers)
                break;
        }

        //Check symbols
        for(int i = 0; i < password.length(); i++) {
            int charValue = password.charAt(i);

            hasSymbols = (charValue > 32 && charValue < 48) ||
                    (charValue > 57 && charValue < 65) ||
                    (charValue > 90 && charValue < 97) ||
                    (charValue > 122 && charValue < 127);

            if(hasSymbols)
                break;
        }

        //Check lowercase
        for (int i = 0; i < password.length(); i++) {
            int charValue = password.charAt(i);

            hasLowercase = charValue > 96 && charValue < 123;

            if(hasLowercase)
                break;
        }

        //Check uppercase
        for (int i = 0; i < password.length(); i++) {
            int charValue = password.charAt(i);

            hasUppercase = charValue > 64 && charValue < 91;

            if(hasUppercase)
                break;
        }

        return hasNumbers != numbers || hasSymbols != symbols || hasLowercase != lowercase || hasUppercase != uppercase;
    }

    private int getIntegerTextFieldValue(TextField textField) {
        String text = textField.getText();

        if(text == null || text.isEmpty())
            return 0;

        return Integer.parseInt(text);
    }

    private Integer[] getSelectedASCIIValues(boolean numbers, boolean symbols, boolean lowercase, boolean uppercase) {
        final Integer[] NUMBERS = new Integer[]{ 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 }; //10 numbers
        final Integer[] SYMBOLS = new Integer[]{ 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59, 61,
                63, 64, 91, 92, 93, 94, 95, 96, 123, 124, 125, 126 }; //30 symbols
        final Integer[] LOWERCASE = new Integer[]{ 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112,
                113, 114, 115, 116, 117, 118, 119, 120, 121, 122 }; //26 lowercase letters
        final Integer[] UPPERCASE = new Integer[]{ 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
                84, 85, 86, 87, 88, 89, 90 }; //26 uppercase letters

        List<Integer> asciiValues = new ArrayList<>();

        if(numbers)
            asciiValues.addAll(Arrays.asList(NUMBERS));

        if(symbols)
            asciiValues.addAll(Arrays.asList(SYMBOLS));

        if(lowercase)
            asciiValues.addAll(Arrays.asList(LOWERCASE));

        if(uppercase)
            asciiValues.addAll(Arrays.asList(UPPERCASE));

        return asciiValues.toArray(new Integer[0]);
    }

    private void disableInput() {
        progressIndicator.setVisible(true);
        numberOfPasswordsTextField.setDisable(true);
        lengthTextField.setDisable(true);
        numberRadioButton.setDisable(true);
        symbolRadioButton.setDisable(true);
        lowercaseRadioButton.setDisable(true);
        uppercaseRadioButton.setDisable(true);
        generateButton.setDisable(true);
    }

    private void enableInput() {
        progressIndicator.setVisible(false);
        numberOfPasswordsTextField.setDisable(false);
        lengthTextField.setDisable(false);
        numberRadioButton.setDisable(false);
        symbolRadioButton.setDisable(false);
        lowercaseRadioButton.setDisable(false);
        uppercaseRadioButton.setDisable(false);
        generateButton.setDisable(false);
    }

    private void showToast() {
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(500), new KeyValue(toast.opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey1);
        fadeInTimeline.setOnFinished((ae) ->
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Timeline fadeOutTimeline = new Timeline();
                    KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(500), new KeyValue (toast.opacityProperty(), 0));
                    fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
                    fadeOutTimeline.play();
                }).start());
        fadeInTimeline.play();
    }

    private void updateValuesDriver(boolean useType, int length, boolean input) {
        if(useType) {
            minLength++;
            maxLength += length;

            maxCombosPerLetter += length;

            if (input) {
                colorValue -= 20;
                strength += 20;
            }
        }
    }

    private class CustomListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                TextField textField = new TextField(item) {
                    @Override
                    public void copy() {
                        super.copy();

                        String password = Clipboard.getSystemClipboard().getString();

                        if (password.length() > 30) {
                            password = password.substring(0, 30) + " ...";
                        }
                        toastLabel.setText("Password Copied: " + password);

                        showToast();
                    }
                };

                textField.setEditable(false);
                textField.setOnMouseClicked(event -> {
                    textField.selectAll();
                    textField.copy();
                });

                setGraphic(textField);
            }
        }
    }
}
