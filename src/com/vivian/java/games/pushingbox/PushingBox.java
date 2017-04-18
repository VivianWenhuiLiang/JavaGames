package com.vivian.java.games.pushingbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PushingBox {
	public static final int OBSTACLE = 4;
	public static final int DESTINATION = 3;
	public static final int BOX = 2;
	public static final int PLAYER = 1;
	public static final int BLANK = 0;
	public static final char UP = 'w';
	public static final char LEFT = 'a';
	public static final char RIGHT = 'd';
	public static final char DOWN = 's';
	public static final char SAVE = 'x';
	public static final char LOAD = 'z';
	public static final char RESET = 'r';
	public static final char QUIT = 'q';

	private int[][] state;
	private int[][] destination;

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
					state = new int[row][column];
					destination = new int[row][column];
				} else {
					if ("State".equals(line)) {
						section = "State";
					} else if ("Destination".equals(line)) {
						section = "Destination";
					} else {
						String[] ss = line.split(" ");
						int row = Integer.valueOf(ss[0]);
						int column = Integer.valueOf(ss[1]);
						int value = Integer.valueOf(ss[2]);
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
				if (BLANK != state[i][j]) {
					sb.append(i).append(' ').append(j).append(' ').append(state[i][j]).append(System.lineSeparator());
				}
			}
		}
		// 保存destination
		sb.append("Destination").append(System.lineSeparator());
		for (int i = 0; i < destination.length; i++) {
			for (int j = 0; j < destination[i].length; j++) {
				if (DESTINATION == destination[i][j]) {
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

	void draw_point(int n) {
		switch (n) {
		case BLANK:
			System.out.print("|   ");
			break;
		case PLAYER:
			System.out.print("| \uc6c3 ");
			break;
		case BOX:
			System.out.print("| \u25A0 ");
			break;
		case DESTINATION:
			System.out.print("| \u2605 ");
			break;
		case OBSTACLE:
			System.out.print("||||");
			break;
		}
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

	void set_point(int x[][], int i, int j, int n) {
		x[i][j] = n;
	}

	int get_a_x(int x[][]) { // get player's x location
		int i, j;
		for (i = 0; i < x.length; i++) {
			for (j = 0; j < x[i].length; j++) {
				if (state[i][j] == PLAYER) {
					return i;
				}
			}
		}
		return -1;
	}

	int get_a_y(int x[][]) {
		int i, j;
		for (i = 0; i < x.length; i++) {
			for (j = 0; j < x[i].length; j++) {
				if (state[i][j] == PLAYER) {
					return j;
				}
			}
		}
		return -1;
	}

	void move(char c) {
		int i = get_a_x(state);
		int j = get_a_y(state);
		if (c == LEFT) {
			if (j == 0) {
				return;
			}
			if (state[i][j - 1] == OBSTACLE) {
				return;
			}
			if (state[i][j - 1] == BOX) {
				if (j - 1 == 0) {
					return;
				}
				if (state[i][j - 2] == BOX || state[i][j - 2] == OBSTACLE) {
					return;
				}
				state[i][j - 2] = state[i][j - 1];
				state[i][j - 1] = state[i][j];
				state[i][j] = BLANK;
			} else {
				state[i][j - 1] = state[i][j];
				state[i][j] = BLANK;
			}
		} else if (c == UP) {
			if (i == 0) {
				return;
			}
			if (state[i - 1][j] == OBSTACLE) {
				return;
			}
			if (state[i - 1][j] == BOX) {
				if (i - 1 == 0) {
					return;
				}
				if (state[i - 2][j] == BOX || state[i - 2][j] == OBSTACLE) {
					return;
				}
				state[i - 2][j] = state[i - 1][j];
				state[i - 1][j] = state[i][j];
				state[i][j] = BLANK;
			} else {
				state[i - 1][j] = state[i][j];
				state[i][j] = BLANK;
			}
		} else if (c == RIGHT) {
			if (j == state[i].length - 1) {
				return;
			}
			if (state[i][j + 1] == OBSTACLE) {
				return;
			}
			if (state[i][j + 1] == BOX) {
				if (j + 1 == state[i].length - 1) {
					return;
				}
				if (state[i][j + 2] == BOX || state[i][j + 2] == OBSTACLE) {
					return;
				}
				state[i][j + 2] = state[i][j + 1];
				state[i][j + 1] = state[i][j];
				state[i][j] = BLANK;
			} else {
				state[i][j + 1] = state[i][j];
				state[i][j] = BLANK;
			}
		} else if (c == DOWN) {
			if (i == state.length - 1) {
				return;
			}
			if (state[i + 1][j] == OBSTACLE) {
				return;
			}
			if (state[i + 1][j] == BOX) {
				if (i + 1 == state.length - 1) {
					return;
				}
				if (state[i + 2][j] == BOX || state[i + 2][j] == OBSTACLE) {
					return;
				}
				state[i + 2][j] = state[i + 1][j];
				state[i + 1][j] = state[i][j];
				state[i][j] = BLANK;
			} else {
				state[i + 1][j] = state[i][j];
				state[i][j] = BLANK;
			}
		}
	}

	void match() {
		int i, j;
		for (i = 0; i < state.length; i++) {
			for (j = 0; j < state[i].length; j++) {
				if (destination[i][j] == DESTINATION && state[i][j] == BLANK) {
					state[i][j] = DESTINATION;
				}
			}
		}
	}

	boolean isOK() {
		boolean bl = true;
		int i, j;
		for (i = 0; i < state.length; i++) {
			for (j = 0; j < state[i].length; j++) {
				if (destination[i][j] == DESTINATION && state[i][j] != BOX) {
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

			if (c == UP || c == LEFT || c == DOWN || c == RIGHT) {
				p.move(c);
				p.match();
			} else if (c == SAVE) {
				p.save("saved.pb");
				System.out.println("Game saved to saved.pb. ");
			} else if (c == LOAD) {
				p = new PushingBox("saved.pb");
			} else if (c == RESET) {
				p = new PushingBox("default.pb");
			} else if (c == QUIT) {
				break;
			} else {
				System.out.println("只能输入wasdxzrq其中之一");
			}
		}
	}
}