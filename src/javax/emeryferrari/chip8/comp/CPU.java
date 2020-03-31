package javax.emeryferrari.chip8.comp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
@SuppressWarnings("serial")
public class CPU extends JFrame {
	private JButton button;
	private boolean drawFlag;
	private int opcode;
	private int[] memory;
	private int[] v;
	private int index;
	private int pc;
	private int[] graphics;
	private int delayTimer;
	private int soundTimer;
	private int[] stack;
	private int sp;
	private boolean[] keypad;
	private int[] font = {0xF0, 0x90, 0x90, 0x90, 0xF0, 0x20, 0x60, 0x20, 0x20, 0x70, 0xF0, 0x10, 0xF0, 0x80, 0xF0, 0xF0, 0x10, 0xF0, 0x10, 0xF0, 0x90, 0x90, 0xF0, 0x10, 0x10, 0xF0, 0x80, 0xF0, 0x10, 0xF0, 0xF0, 0x80, 0xF0, 0x90, 0xF0, 0xF0, 0x10, 0x20, 0x40, 0x40, 0xF0, 0x90, 0xF0, 0x90, 0xF0, 0xF0, 0x90, 0xF0, 0x10, 0xF0, 0xF0, 0x90, 0xF0, 0x90, 0x90, 0xE0, 0x90, 0xE0, 0x90, 0xE0, 0xF0, 0x80, 0x80, 0x80, 0xF0, 0xE0, 0x90, 0x90, 0x90, 0xE0, 0xF0, 0x80, 0xF0, 0x80, 0xF0, 0xF0, 0x80, 0xF0, 0x80, 0x80};
	public CPU(File file) throws IOException {
		this();
		if (file.length() <= 0) {
			throw new IOException();
		} else if (file.length() > 3232) {
			System.err.println("Error: CHIP-8 binaries must not be more than 3232 bytes due to memory constraints.");
			System.exit(1);
		}
		FileInputStream fis = new FileInputStream(file);
		for (int i = 0x200; i < file.length() + 0x200; i++) {
			int x;
			if ((x = fis.read()) > -1) {
				memory[i] = x;
			}
		}
		fis.close();
	}
	public CPU() {
		button = new JButton("Cycle");
		button.addActionListener(new CycleListener());
		this.getContentPane().add(BorderLayout.SOUTH, button);
		drawFlag = true;
		opcode = 0x0;
		memory = new int[0x1000];
		v = new int[0x10];
		index = 0x0;
		pc = 0x200;
		graphics = new int[0x800];
		delayTimer = 0x0;
		soundTimer = 0x0;
		stack = new int[0x18];
		sp = 0x0;
		keypad = new boolean[0x10];
		for (int i = 0; i < graphics.length; i++) {
			graphics[i] = 0;
		}
		for (int i = 0; i < stack.length; i++) {
			stack[i] = 0x0;
		}
		for (int i = 0; i < v.length; i++) {
			v[i] = 0x0;
		}
		for (int i = 0; i < memory.length; i++) {
			memory[i] = 0x0;
		}
		for (int i = 0; i < font.length; i++) {
			memory[i] = font[i];
		}
		Thread thread = new Timers();
		thread.start();
	}
	public void cycle(int cycles) {
		for (int a = 0; a < cycles; a++) {
			opcode = (memory[pc] << 8) | memory[pc + 1];
			int op = opcode & 0xF000;
			System.out.println(Integer.toHexString(op));
			System.out.println(Integer.toHexString(opcode));
			System.out.println(Integer.toHexString(pc));
			System.out.println();
			if (op == 0x0) {
				if (opcode == 0xE0) {
					for (int b = 0; b < graphics.length; b++) {
						graphics[b] = 0;
					}
					drawFlag = true;
				} else if (opcode == 0xEE) {
					sp--;
					pc = stack[sp];
				}
			} else if (op == 0x1000) {
				pc = opcode & 0xFFF;
				pc -= 2;
			} else if (op == 0x2000) {
				stack[sp] = pc;
				sp++;
				pc = opcode & 0xFFF;
				pc -= 2;
			} else if (op == 0x3000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				if (v[x] == nn) {
					pc += 2;
				}
			} else if (op == 0x4000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				if (v[x] != nn) {
					pc += 2;
				}
			} else if (op == 0x5000) {
				int x = (opcode & 0xF00) >> 8;
				int y = (opcode & 0xF0) >> 4;
				int suffix = opcode & 0xF;
				if (suffix == 0) {
					if (v[x] == v[y]) {
						pc += 2;
					}
				}
			} else if (op == 0x6000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				v[x] = nn;
			} else if (op == 0x7000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				v[x] += nn;
			} else if (op == 0x8000) {
				int x = (opcode & 0xF00) >> 8;
				int y = (opcode & 0xF0) >> 4;
				int suffix = opcode & 0xF;
				if (suffix == 0) {
					v[x] = v[y];
				} else if (suffix == 1) {
					v[x] |= v[y];
				} else if (suffix == 2) {
					v[x] &= v[y];
				} else if (suffix == 3) {
					v[x] ^= v[y];
				} else if (suffix == 4) {
					if (v[(opcode & 0xF0) >> 4] > (0xFF - v[(opcode & 0xF00) >> 8])) {
						v[0xF] = 1;
					} else {
						v[0xF] = 0;
					}
					v[x] += v[y];
				} else if (suffix == 5) {
					if (v[(opcode & 0xF0) >> 4] > v[(opcode & 0xF00) >> 8]) {
						v[0xF] = 0;
					} else {
						v[0xF] = 1;
					}
					v[x] -= v[y];
				} else if (suffix == 6) {
					v[0xF] = v[x] & 0x1;
					v[x] >>= 1;
				} else if (suffix == 7) {
					if (v[(opcode & 0xF00) >> 8] > v[(opcode & 0xF0) >> 4]) {
						v[0xF] = 0;
					} else {
						v[0xF] = 1;
					}
					v[x] = v[y] - v[x];
				} else if (suffix == 0xE) {
					v[0xF] = v[x] & 0x80;
					v[x] <<= 1;
				}
			} else if (op == 0x9000) {
				int x = (opcode & 0xF00) >> 8;
				int y = (opcode & 0xF0) >> 4;
				int suffix = opcode & 0xF;
				if (suffix == 0) {
					if (v[x] != v[y]) {
						pc += 2;
					}
				}
			} else if (op == 0xA000) {
				int address = opcode & 0xFFF;
				index = address;
			} else if (op == 0xB000) {
				int address = opcode & 0xFFF;
				pc = address + v[0x0] - 2;
			} else if (op == 0xC000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				int rand = (int) (Math.random() * 255.0);
				v[x] = rand & nn;
			} else if (op == 0xD000) {
				int x = v[(opcode & 0xF00) >> 8];
				int y = v[(opcode & 0xF0) >> 4];
				int height = opcode & 0xF;
				int pixel;
				v[0xF] = 0;
				for (int yint = 0; yint < height; yint++) {
					pixel = memory[index+yint];
					for (int xint = 0; xint < 8; xint++) {
						if ((pixel & (0x80 >> xint)) != 0) {
							if (graphics[(x + xint + ((y + yint) * 64))] == 1) {
								v[0xF] = 1;
							}
							graphics[x + xint + ((y + yint) * 64)] ^= 1;
						}
					}
				}
				drawFlag = true;
			} else if (op == 0xE000) {
				int x = (opcode & 0xF00) >> 8;
				int suffix = opcode & 0xFF;
				int key = v[x];
				if (suffix == 0x9E) {
					if (keypad[key]) {
						pc += 2;
					}
				} else if (suffix == 0xA1) {
					if (!keypad[key]) {
						pc += 2;
					}
				}
			} else if (op == 0xF000) {
				int x = (opcode & 0xF00) >> 8;
				int suffix = opcode & 0xFF;
				if (suffix == 0x7) {
					v[x] = delayTimer;
				} else if (suffix == 0xA) {
					while (true) {
						boolean c = false;
						for (int b = 0; b < keypad.length; b++) {
							if (keypad[b]) {
								v[x] = b;
								c = true;
								break;
							}
						}
						if (c) {
							break;
						}
						try {
							Thread.sleep(1);
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}
				} else if (suffix == 0x15) {
					delayTimer = v[x];
				} else if (suffix == 0x18) {
					soundTimer = v[x];
				} else if (suffix == 0x1E) {
					index += v[x];
				} else if (suffix == 0x29) {
					index = v[(opcode & 0xF00) >> 8] * 0x5;
				} else if (suffix == 0x33) {
					memory[index] = v[(opcode & 0xF00) >> 8] / 100;
					memory[index+1] = (v[(opcode & 0xF00) >> 8]/10) % 10;
					memory[index+2] = (v[(opcode & 0xF00) >> 8] % 100) % 10;
				} else if (suffix == 0x55) {
					for (int b = 0; b < x+1; b++) {
						memory[index+b] = v[b];
					}
				} else if (suffix == 0x65) {
					for (int b = 0; b < x+1; b++) {
						v[b] = memory[index+b];
					}
				}
			}
			pc += 2;
			if (drawFlag) {
				this.repaint();
			}
		}
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 32; y++) {
				if (graphics[(64*y) + x] == 0) {
					g.setColor(Color.BLACK);
					g.fillRect((10*x)+20, (10*y)+50, 10, 10);
				} else if (graphics[(64*y) + x] == 1) {
					g.setColor(Color.RED);
					g.fillRect((10*x)+20, (10*y)+50, 10, 10);
				}
			}
		}
		/*
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 32; y++) {
				g.setColor(Color.RED);
				g.drawRect((10*x)+20, (10*y)+50, 10, 10);
			}
		}
		*/
		drawFlag = false;
	}
	public class Timers extends Thread {
		@Override
		public void run() {
			while (true) {
				if (delayTimer > 0) {
					delayTimer--;
				}
				if (soundTimer > 0) {
					soundTimer--;
					if (soundTimer == 0) {
						System.out.println("Beep");
					}
				}
				try {
					Thread.sleep(17);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public class CycleListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			cycle(1);
		}
	}
	public class FrameListener implements KeyListener {
		public void keyPressed(KeyEvent ev) {
			
		}
		public void keyReleased(KeyEvent ev) {
			
		}
		public void keyTyped(KeyEvent ev) {
			
		}
	}
}