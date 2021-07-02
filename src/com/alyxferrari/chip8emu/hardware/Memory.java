package com.alyxferrari.chip8emu.hardware;
class Memory {
	int ram[];
	int gpuMem[];
	Memory() {
		ram = new int[0x1000];
		gpuMem = new int[0x800];
		for (int i = 0; i < gpuMem.length; i++) {
			gpuMem[i] = 0x0;
		}
		for (int i = 0; i < ram.length; i++) {
			ram[i] = 0x0;
		}
		for (int i = 0; i < Registers.font.length; i++) {
			ram[i] = Registers.font[i];
		}
	}
}