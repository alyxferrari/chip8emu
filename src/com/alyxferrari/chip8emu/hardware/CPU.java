package com.alyxferrari.chip8emu.hardware;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import com.alyxferrari.chip8emu.*;
@SuppressWarnings("serial")
public class CPU extends JFrame {
	private JButton button;
	Memory mem;
	Registers reg;
	public CPU(File file) throws IOException {
		this();
		if (file.length() <= 0) {
			System.err.println("Error: CHIP-8 binaries must not be empty.");
			System.exit(2);
		} else if (file.length() > 3232) {
			System.err.println("Error: CHIP-8 binaries must not be more than 3232 bytes due to memory constraints.");
			System.exit(1);
		}
		FileInputStream fis = new FileInputStream(file);
		for (int i = 0x200; i < file.length() + 0x200; i++) {
			int x;
			if ((x = fis.read()) > -1) {
				mem.ram[i] = x;
			}
		}
		fis.close();
	}
	public CPU() {
		mem = new Memory();
		reg = new Registers();
		button = new JButton("Cycle");
		button.addActionListener(new CycleListener());
		this.getContentPane().add(BorderLayout.SOUTH, button);
		Thread thread = new Timers();
		thread.start();
	}
	public void cycle(int cycles) {
		for (int a = 0; a < cycles; a++) {
			int opcode = (mem.ram[reg.pc] << 8) | mem.ram[reg.pc+1];
			int op = opcode & 0xF000;
			if (op == 0x0) {
				if (opcode == 0xE0) {
					for (int b = 0; b < mem.gpuMem.length; b++) {
						mem.gpuMem[b] = 0;
					}
					reg.drawFlag = true;
				} else if (opcode == 0xEE) {
					reg.sp--;
					reg.pc = reg.stack[reg.sp];
				}
			} else if (op == 0x1000) {
				reg.pc = opcode & 0xFFF;
				reg.pc -= 2;
			} else if (op == 0x2000) {
				reg.stack[reg.sp] = reg.pc;
				reg.sp++;
				reg.pc = opcode & 0xFFF;
				reg.pc -= 2;
			} else if (op == 0x3000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				if (reg.reg[x] == nn) {
					reg.pc += 2;
				}
			} else if (op == 0x4000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				if (reg.reg[x] != nn) {
					reg.pc += 2;
				}
			} else if (op == 0x5000) {
				int x = (opcode & 0xF00) >> 8;
				int y = (opcode & 0xF0) >> 4;
				int suffix = opcode & 0xF;
				if (suffix == 0) {
					if (reg.reg[x] == reg.reg[y]) {
						reg.pc += 2;
					}
				}
			} else if (op == 0x6000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				reg.reg[x] = nn;
			} else if (op == 0x7000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				reg.reg[x] += nn;
			} else if (op == 0x8000) {
				int x = (opcode & 0xF00) >> 8;
				int y = (opcode & 0xF0) >> 4;
				int suffix = opcode & 0xF;
				if (suffix == 0) {
					reg.reg[x] = reg.reg[y];
				} else if (suffix == 1) {
					reg.reg[x] |= reg.reg[y];
				} else if (suffix == 2) {
					reg.reg[x] &= reg.reg[y];
				} else if (suffix == 3) {
					reg.reg[x] ^= reg.reg[y];
				} else if (suffix == 4) {
					if (reg.reg[(opcode & 0xF0) >> 4] > (0xFF - reg.reg[(opcode & 0xF00) >> 8])) {
						reg.reg[0xF] = 1;
					} else {
						reg.reg[0xF] = 0;
					}
					reg.reg[x] += reg.reg[y];
				} else if (suffix == 5) {
					if (reg.reg[(opcode & 0xF0) >> 4] > reg.reg[(opcode & 0xF00) >> 8]) {
						reg.reg[0xF] = 0;
					} else {
						reg.reg[0xF] = 1;
					}
					reg.reg[x] -= reg.reg[y];
				} else if (suffix == 6) {
					reg.reg[0xF] = reg.reg[x] & 0x1;
					reg.reg[x] >>= 1;
				} else if (suffix == 7) {
					if (reg.reg[(opcode & 0xF00) >> 8] > reg.reg[(opcode & 0xF0) >> 4]) {
						reg.reg[0xF] = 0;
					} else {
						reg.reg[0xF] = 1;
					}
					reg.reg[x] = reg.reg[y] - reg.reg[x];
				} else if (suffix == 0xE) {
					reg.reg[0xF] = reg.reg[x] & 0x80;
					reg.reg[x] <<= 1;
				}
			} else if (op == 0x9000) {
				int x = (opcode & 0xF00) >> 8;
				int y = (opcode & 0xF0) >> 4;
				int suffix = opcode & 0xF;
				if (suffix == 0) {
					if (reg.reg[x] != reg.reg[y]) {
						reg.pc += 2;
					}
				}
			} else if (op == 0xA000) {
				int address = opcode & 0xFFF;
				reg.index = address;
			} else if (op == 0xB000) {
				int address = opcode & 0xFFF;
				reg.pc = address + reg.reg[0x0] - 2;
			} else if (op == 0xC000) {
				int x = (opcode & 0xF00) >> 8;
				int nn = opcode & 0xFF;
				int rand = (int) (Math.random() * 255.0);
				reg.reg[x] = rand & nn;
			} else if (op == 0xD000) {
				int x = reg.reg[(opcode & 0xF00) >> 8];
				int y = reg.reg[(opcode & 0xF0) >> 4];
				int height = opcode & 0xF;
				int pixel;
				reg.reg[0xF] = 0;
				for (int yint = 0; yint < height; yint++) {
					pixel = mem.ram[reg.index+yint];
					for (int xint = 0; xint < 8; xint++) {
						if ((pixel & (0x80 >> xint)) != 0) {
							if (mem.gpuMem[(x + xint + ((y + yint) * 64))] == 1) {
								reg.reg[0xF] = 1;
							}
							mem.gpuMem[x + xint + ((y + yint) * 64)] ^= 1;
						}
					}
				}
				reg.drawFlag = true;
			} else if (op == 0xE000) {
				int x = (opcode & 0xF00) >> 8;
				int suffix = opcode & 0xFF;
				int key = reg.reg[x];
				if (suffix == 0x9E) {
					if (reg.keypad[key]) {
						reg.pc += 2;
					}
				} else if (suffix == 0xA1) {
					if (!reg.keypad[key]) {
						reg.pc += 2;
					}
				}
			} else if (op == 0xF000) {
				int x = (opcode & 0xF00) >> 8;
				int suffix = opcode & 0xFF;
				if (suffix == 0x7) {
					reg.reg[x] = reg.delayTimer;
				} else if (suffix == 0xA) {
					while (true) {
						boolean c = false;
						for (int b = 0; b < reg.keypad.length; b++) {
							if (reg.keypad[b]) {
								reg.reg[x] = b;
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
					reg.delayTimer = reg.reg[x];
				} else if (suffix == 0x18) {
					reg.soundTimer = reg.reg[x];
				} else if (suffix == 0x1E) {
					reg.index += reg.reg[x];
				} else if (suffix == 0x29) {
					reg.index = reg.reg[(opcode & 0xF00) >> 8] * 0x5;
				} else if (suffix == 0x33) {
					mem.ram[reg.index] = reg.reg[(opcode & 0xF00) >> 8] / 100;
					mem.ram[reg.index+1] = (reg.reg[(opcode & 0xF00) >> 8]/10) % 10;
					mem.ram[reg.index+2] = (reg.reg[(opcode & 0xF00) >> 8] % 100) % 10;
				} else if (suffix == 0x55) {
					for (int b = 0; b < x+1; b++) {
						mem.ram[reg.index+b] = reg.reg[b];
					}
				} else if (suffix == 0x65) {
					for (int b = 0; b < x+1; b++) {
						reg.reg[b] = mem.ram[reg.index+b];
					}
				}
			}
			reg.pc += 2;
			if (reg.drawFlag) {
				this.repaint();
			}
		}
		Launcher.fps++;
	}
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 32; y++) {
				if (mem.gpuMem[(64*y) + x] == 0) {
					g.setColor(Color.BLACK);
					g.fillRect((10*x)+20, (10*y)+50, 10, 10);
				} else if (mem.gpuMem[(64*y) + x] == 1) {
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
		reg.drawFlag = false;
	}
	public class Timers extends Thread {
		@Override
		public void run() {
			while (true) {
				if (reg.delayTimer > 0) {
					reg.delayTimer--;
				}
				if (reg.soundTimer > 0) {
					reg.soundTimer--;
					if (reg.soundTimer == 0) {
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