package password.controller;

public interface Default {
    int TOTAL_NUMBERS = 10;
    int TOTAL_SYMBOLS = 30;
    int TOTAL_LETTERS = 26;
    int MAX_GENERATED_PASSWORDS = 100;
    int DEFAULT_LENGTH = 12;

    int[] ASCII_RANGE_NUMBERS = {47, 58};
    int [] ASCII_RANGE_SYMBOLS = {32, 48, 57, 65, 90, 97, 122, 127};
    int [] ASCII_RANGE_LOWER = {96, 123};
    int [] ASCII_RANGE_UPPER = {64, 91};

    Integer[] ASCII_NUMBERS = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 }; //10 numbers
    Integer[] ASCII_SYMBOLS = { 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59, 61,
            63, 64, 91, 92, 93, 94, 95, 96, 123, 124, 125, 126 }; //30 symbols
    Integer[] ASCII_LOWERCASE = { 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112,
            113, 114, 115, 116, 117, 118, 119, 120, 121, 122 }; //26 lowercase letters
    Integer[] ASCII_UPPERCASE = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
            84, 85, 86, 87, 88, 89, 90 }; //26 lowercase letters
}
