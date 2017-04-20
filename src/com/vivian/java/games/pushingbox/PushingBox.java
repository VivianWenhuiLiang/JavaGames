package com.vivian.java.games.pushingbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PushingBox {
    private enum Element {
        BLANK(0, "|   "), PLAYER(1, "| \uc6c3 "), BOX(2, "| \u25A0 "), DESTINATION(3, "| \u2605 "), OBSTACLE(4, "||||");

        private int value;
        private String symbol;

        Element(int value, String symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        public int getValue() {
            return value;
        }

        public String getSymbol() {
            return symbol;
        }

        public static Element fromInt(int n) {
            for (Element e : Element.values()) {
                if (n == e.getValue()) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Invalid element value: " + n);
        }
    }

    private enum Operation {
        UP('w'), LEFT('a'), RIGHT('d'), DOWN('s'), SAVE('x'), LOAD('z'), RESET('r'), QUIT('q');
        private char code;

        Operation(char code) {
            this.code = code;
        }

        public char getCode() {
            return code;
        }
    }

    private Element[][] state;
    private Element[][] destination;

    public PushingBox(String fileName) throws IOException {
        load(fileName);
    }

    public void load(String fileName) {
        try (BufferedReader b = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;

            int i = 0;
            String section = "State";
            while ((line = b.readLine()) != null) {
                if (i == 0) {
                    String[] ss = line.split(" ");
                    // First line, PushingBox numberOfRow numberOfColumn
                    if ((3 != ss.length) || !"PushingBox".equals(ss[0])) {
                        throw new IllegalArgumentException("Invalid PushingBox file: " + fileName);
                    }
                    int row = Integer.valueOf(ss[1]);
                    int column = Integer.valueOf(ss[2]);
                    state = new Element[row][column];
                    destination = new Element[row][column];
                    for (int r = 0; r < row; r++) {
                        for (int c = 0; c < column; c++) {
                            state[r][c] = Element.BLANK;
                            destination[r][c] = Element.BLANK;
                        }
                    }
                } else {
                    if ("State".equals(line)) {
                        section = "State";
                    } else if ("Destination".equals(line)) {
                        section = "Destination";
                    } else {
                        String[] ss = line.split(" ");
                        int row = Integer.valueOf(ss[0]);
                        int column = Integer.valueOf(ss[1]);
                        Element value = Element.fromInt(Integer.valueOf(ss[2]));
                        if ("State".equals(section)) {
                            set_point(state, row, column, value);
                        } else if ("Destination".equals(section)) {
                            set_point(destination, row, column, value);
                        }
                    }
                }

                i++;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to initialize game with file " + fileName, e);
        }
    }

    public void save(String fileName) {
        StringBuilder sb = new StringBuilder();
        // 第一行为PushingBox 行数 列数
        sb.append("PushingBox ").append(state.length).append(' ').append(state[0].length)
                .append(System.lineSeparator());
        // 保存state
        sb.append("State").append(System.lineSeparator());
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                if (!Element.BLANK.equals(state[i][j])) {
                    sb.append(i).append(' ').append(j).append(' ').append(state[i][j]).append(System.lineSeparator());
                }
            }
        }
        // 保存destination
        sb.append("Destination").append(System.lineSeparator());
        for (int i = 0; i < destination.length; i++) {
            for (int j = 0; j < destination[i].length; j++) {
                if (Element.DESTINATION.equals(destination[i][j])) {
                    sb.append(i).append(' ').append(j).append(' ').append(destination[i][j])
                            .append(System.lineSeparator());
                }
            }
        }

        try (BufferedWriter b = new BufferedWriter(new FileWriter(new File(fileName)))) {
            b.write(sb.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to save game to file " + fileName, e);
        }
    }

    void draw_point(Element n) {
        System.out.print(n.getSymbol());
    }

    void draw_line(int row) {
        int count = 4 * state[row].length + 1;
        for (int i = 0; i < count; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    public void draw() {
        for (int i = 0; i < state.length; i++) {
            draw_line(i);
            for (int j = 0; j < state[i].length; j++) {
                draw_point(state[i][j]);
            }
            System.out.print("|");
            System.out.println();
        }
        draw_line(state.length - 1);
    }

    void set_point(Element x[][], int i, int j, Element n) {
        x[i][j] = n;
    }

    int get_a_x(Element x[][]) { // get player's x location
        int i, j;
        for (i = 0; i < x.length; i++) {
            for (j = 0; j < x[i].length; j++) {
                if (Element.PLAYER.equals(state[i][j])) {
                    return i;
                }
            }
        }
        return -1;
    }

    int get_a_y(Element x[][]) {
        int i, j;
        for (i = 0; i < x.length; i++) {
            for (j = 0; j < x[i].length; j++) {
                if (Element.PLAYER.equals(state[i][j])) {
                    return j;
                }
            }
        }
        return -1;
    }

    void move(char operation) {
        int i = get_a_x(state);
        int j = get_a_y(state);
        if (operation == Operation.LEFT.getCode()) {
            if (j == 0) {
                return;
            }
            if (Element.OBSTACLE.equals(state[i][j - 1])) {
                return;
            }
            if (Element.BOX.equals(state[i][j - 1])) {
                if (j - 1 == 0) {
                    return;
                }
                if (Element.BOX.equals(state[i][j - 2]) || Element.OBSTACLE.equals(state[i][j - 2])) {
                    return;
                }
                state[i][j - 2] = state[i][j - 1];
                state[i][j - 1] = state[i][j];
                state[i][j] = Element.BLANK;
            } else {
                state[i][j - 1] = state[i][j];
                state[i][j] = Element.BLANK;
            }
        } else if (operation == Operation.UP.getCode()) {
            if (i == 0) {
                return;
            }
            if (Element.OBSTACLE.equals(state[i - 1][j])) {
                return;
            }
            if (Element.BOX.equals(state[i - 1][j])) {
                if (i - 1 == 0) {
                    return;
                }
                if (Element.BOX.equals(state[i - 2][j]) || Element.OBSTACLE.equals(state[i - 2][j])) {
                    return;
                }
                state[i - 2][j] = state[i - 1][j];
                state[i - 1][j] = state[i][j];
                state[i][j] = Element.BLANK;
            } else {
                state[i - 1][j] = state[i][j];
                state[i][j] = Element.BLANK;
            }
        } else if (operation == Operation.RIGHT.getCode()) {
            if (j == state[i].length - 1) {
                return;
            }
            if (Element.OBSTACLE.equals(state[i][j + 1])) {
                return;
            }
            if (Element.BOX.equals(state[i][j + 1])) {
                if (j + 1 == state[i].length - 1) {
                    return;
                }
                if (Element.BOX.equals(state[i][j + 2]) || Element.OBSTACLE.equals(state[i][j + 2])) {
                    return;
                }
                state[i][j + 2] = state[i][j + 1];
                state[i][j + 1] = state[i][j];
                state[i][j] = Element.BLANK;
            } else {
                state[i][j + 1] = state[i][j];
                state[i][j] = Element.BLANK;
            }
        } else if (operation == Operation.DOWN.getCode()) {
            if (i == state.length - 1) {
                return;
            }
            if (Element.OBSTACLE.equals(state[i + 1][j])) {
                return;
            }
            if (Element.BOX.equals(state[i + 1][j])) {
                if (i + 1 == state.length - 1) {
                    return;
                }
                if (Element.BOX.equals(state[i + 2][j]) || Element.OBSTACLE.equals(state[i + 2][j])) {
                    return;
                }
                state[i + 2][j] = state[i + 1][j];
                state[i + 1][j] = state[i][j];
                state[i][j] = Element.BLANK;
            } else {
                state[i + 1][j] = state[i][j];
                state[i][j] = Element.BLANK;
            }
        }
    }

    void match() {
        int i, j;
        for (i = 0; i < state.length; i++) {
            for (j = 0; j < state[i].length; j++) {
                if (Element.DESTINATION.equals(destination[i][j]) && Element.BLANK.equals(state[i][j])) {
                    state[i][j] = Element.DESTINATION;
                }
            }
        }
    }

    boolean isOK() {
        boolean bl = true;
        int i, j;
        for (i = 0; i < state.length; i++) {
            for (j = 0; j < state[i].length; j++) {
                if (Element.DESTINATION.equals(destination[i][j]) && !Element.BOX.equals(state[i][j])) {
                    bl = false;
                }
            }
        }
        return bl;
    }

    public static void main(String[] args) throws IOException {
        PushingBox p = new PushingBox("default.pb");
        while (true) {
            p.draw();
            if (p.isOK()) {
                System.out.println("恭喜，任务达成！");
                break;
            }

            System.out.println(
                    "请输入命令以移动 (w - UP, a - LEFT, s - DOWN, d - RIGHT, x - save game, z - load game, r - reset game, q - quit game):");
            char c = (char) System.in.read();
            System.in.skip(System.in.available());

            if (c == Operation.UP.getCode() || c == Operation.LEFT.getCode() || c == Operation.DOWN.getCode()
                    || c == Operation.RIGHT.getCode()) {
                p.move(c);
                p.match();
            } else if (c == Operation.SAVE.getCode()) {
                p.save("saved.pb");
                System.out.println("Game saved to saved.pb. ");
            } else if (c == Operation.LOAD.getCode()) {
                p = new PushingBox("saved.pb");
            } else if (c == Operation.RESET.getCode()) {
                p = new PushingBox("default.pb");
            } else if (c == Operation.QUIT.getCode()) {
                break;
            } else {
                System.out.println("只能输入wasdxzrq其中之一");
            }
        }
    }
}