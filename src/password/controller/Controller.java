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

public class Controller implements Default{
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
            if (!newValue.matches("\\d{0,9}")) {
                lengthTextField.setText(oldValue);
            }

            updateValues();
        }));
        lengthTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                generatePassword();
            }
        });
        lengthTextField.setOnMouseClicked(event -> lengthTextField.selectAll());

        numberOfPasswordsTextField.setText(Integer.toString(1));
        numberOfPasswordsTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,9}")) {
                numberOfPasswordsTextField.setText(oldValue);
            }

            updateValues();
        }));
        numberOfPasswordsTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                generatePassword();
            }
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
        strength = 0;
        colorValue = 100.0;

        useNumbers = numberRadioButton.isSelected();
        useSymbols = symbolRadioButton.isSelected();
        useLowercase = lowercaseRadioButton.isSelected();
        useUppercase = uppercaseRadioButton.isSelected();

        length = getIntegerTextFieldValue(lengthTextField);
        numberOfPasswords = getIntegerTextFieldValue(numberOfPasswordsTextField);

        boolean didInputLengths = length > 0 && numberOfPasswords > 0;

        //Calculates max password length.
        updateValuesDriver(useNumbers, TOTAL_NUMBERS, didInputLengths);
        updateValuesDriver(useSymbols, TOTAL_SYMBOLS, didInputLengths);
        updateValuesDriver(useLowercase, TOTAL_LETTERS, didInputLengths);
        updateValuesDriver(useUppercase, TOTAL_LETTERS, didInputLengths);

        //Calculates max password combos.
        maxPasswordsCombos = (int)Math.pow(maxCombosPerLetter, length);
        if (maxPasswordsCombos > MAX_GENERATED_PASSWORDS) {
            maxPasswordsCombos = MAX_GENERATED_PASSWORDS;
        }

        //Calculates strength color and value.
        if ((length < minLength) || (length > maxLength)) {
            colorValue = 100.0;
            strength = 0;
        } else if (didInputLengths) {
            double increment = 20.0 * Math.min(1.0, length / (maxLength < 12 ? maxLength : 12.0));
            colorValue -= increment;
            strength += increment;
        }

        generateButton.setDisable(isInputInvalid());

        //Updates the password criteria text.
        lengthLabel.setText("- " + (minLength == 0 ? "..." : minLength) + " < length <= " + (maxLength == 0 ? "..." : maxLength));
        passwordCountLabel.setText("- 0 < number of passwords <= " + (maxPasswordsCombos == 0  ? "..." : maxPasswordsCombos));

        //Updates strength color and value text.
        passwordStrengthLabel.setText(TO_HUND.format(strength) + "%");
        passwordStrengthLabel.setTextFill(new Color((255 * colorValue) / 25500, (255 * (100 - colorValue)) / 25500, 0, 1));
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

    @FXML
    private void generatePassword() {
        if(isInputInvalid())
            return;

        final AtomicInteger NUMBER_OF_PASSWORDS = new AtomicInteger(numberOfPasswords);
        final Integer[] ASCII_CHARS = getSelectedASCIIValues(useNumbers, useSymbols, useLowercase, useUppercase);
        AtomicInteger totalPasswords = new AtomicInteger(0);

        List<String> generatedPasswords = new ArrayList<>();

        disableInput();

        passwordListView.getItems().clear();

        //A thread is created for each password.
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            StringBuilder password;

            do{
                List<Integer> asciiCharsCopy = new ArrayList<>(Arrays.asList(ASCII_CHARS));
                password = new StringBuilder();

                //Generate password by appending random non-assigned ASCII chars to password.
                for (int j = 0; j < length; j++) {
                    int index = SECURE_RANDOM.nextInt(asciiCharsCopy.size());
                    int asciiValue = asciiCharsCopy.get(index);

                    password.append((char) asciiValue);

                    asciiCharsCopy.remove(index);
                }

            //Generate new password if current already exists or is invalid.
            }while(Collections.synchronizedList(generatedPasswords).contains(password.toString()) ||
                    isPasswordInvalid(password.toString(), useNumbers, useSymbols, useLowercase, useUppercase));

            //Password is generated, and will be added to gui list later. A copy of the password is added to a threaded
            //list for validating the next password.
            String finalPassword = password.toString();
            generatedPasswords.add(finalPassword);
            Platform.runLater(() -> passwordListView.getItems().add(finalPassword));

            totalPasswords.getAndIncrement();

            //Updates the progress indicator
            progressIndicator.setProgress(totalPasswords.doubleValue() / numberOfPasswords);

            //When all passwords are generated, progress indicator is set to 100%.
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
        return isPasswordInvalidDriver(password, ASCII_RANGE_NUMBERS) != numbers ||
                isPasswordInvalidDriver(password, ASCII_RANGE_SYMBOLS) != symbols ||
                isPasswordInvalidDriver(password, ASCII_RANGE_LOWER) != lowercase ||
                isPasswordInvalidDriver(password, ASCII_RANGE_UPPER) != uppercase;
    }

    private boolean isPasswordInvalidDriver(String password, int[] asciiRanges) {
        //For each char, determine if it is within the range of ASCII chars.
        for (int i = 0; i < password.length(); i++) {
            int charValue = password.charAt(i);

            for (int j = 0; j < asciiRanges.length; j+=2) {
                if(charValue > asciiRanges[j] && charValue < asciiRanges[j + 1]) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getIntegerTextFieldValue(TextField textField) {
        String text = textField.getText();

        if(text == null || text.isEmpty())
            return 0;

        return Integer.parseInt(text);
    }

    private Integer[] getSelectedASCIIValues(boolean numbers, boolean symbols, boolean lowercase, boolean uppercase) {
        List<Integer> asciiValues = new ArrayList<>();

        if(numbers) {
            asciiValues.addAll(Arrays.asList(ASCII_NUMBERS));
        }

        if(symbols) {
            asciiValues.addAll(Arrays.asList(ASCII_SYMBOLS));
        }


        if(lowercase) {
            asciiValues.addAll(Arrays.asList(ASCII_LOWERCASE));
        }

        if(uppercase) {
            asciiValues.addAll(Arrays.asList(ASCII_UPPERCASE));
        }

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
