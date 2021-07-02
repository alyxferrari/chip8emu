package com.alyxferrari.chip8emu;
import com.alyxferrari.chip8emu.hardware.*;
import javax.swing.*;
import java.io.*;
public class Launcher {
	@SuppressWarnings("unused")
	private static final String PROGRAM_VERSION = "v1.0 beta 2";
	private static final Launcher CLASS_OBJ = new Launcher();
	private static int clock = 33;
	private CPU cpu;
	public static int fps = 0;
	public static void main(String[] args) throws IOException, InterruptedException {
		File file = new File("");
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("-rom")) {
				file = new File(args[1]);
			} else {
				Launcher.printUsage();
			}
		} else if (args.length == 4) {
			if (args[0].equalsIgnoreCase("-rom")) {
				file = new File(args[1]);
				if (args[2].equalsIgnoreCase("-clock")) {
					try {
						clock = Integer.parseInt(args[3]);
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
						System.exit(1);
					}
				} else {
					Launcher.printUsage();
				}
			} else if (args[0].equalsIgnoreCase("-clock")) {
				try {
					clock = Integer.parseInt(args[3]);
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
				if (args[2].equalsIgnoreCase("-rom")) {
					file = new File(args[3]);
				}
			} else {
				Launcher.printUsage();
			}
		} else {
			file = new File("/Users/alyx/Downloads/testrom.ch8");
			//Launcher.printUsage();
		}
		CLASS_OBJ.cpu = new CPU(file);
		CLASS_OBJ.cpu.setSize(800, 600);
		CLASS_OBJ.cpu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CLASS_OBJ.cpu.setVisible(true);
		CLASS_OBJ.cpu.revalidate();
		CLASS_OBJ.cpu.repaint();
		long lastLoopTime = System.nanoTime();
		while (true) {
			long now = System.nanoTime();
		    lastLoopTime = now;
		    CLASS_OBJ.cpu.cycle(1);
		    long tmp = (lastLoopTime-System.nanoTime()+clock)/1000000;
	    	if (tmp > 0) {
	    		try {Thread.sleep(tmp);} catch (InterruptedException ex) {ex.printStackTrace();}
	    	}
		}
	}
	public static void printUsage() {
		System.out.println("Arguments:");
		System.out.println("  -rom rom: specifies a program to be loaded into the CHIP-8");
		System.out.println("  -clock speed: (optional) specifies a clock speed, up to 1000 Hz, with which to run the emulated CHIP-8");
		System.out.println("  -help: Displays this menu");
		System.exit(1);
	}
}