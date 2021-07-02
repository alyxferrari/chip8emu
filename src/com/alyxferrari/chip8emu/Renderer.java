package com.alyxferrari.chip8emu;
import java.io.*;
import java.util.*;
import java.awt.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.system.MemoryUtil.*;
import static com.alyxferrari.chip8emu.CHIP8Const.*;
public class Renderer {
	private long window = NULL;
	private int shader = -1;
	private int width = -1;
	private int height = -1;
	private int[] framebuffer;
	private Color clear;
	private Color draw;
	public Renderer(int width, int height, int[] framebuffer, Color clear, Color draw) throws IOException {
		if (window == NULL) {
			if (glfwInit()) {
				glfwDefaultWindowHints();
				glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
				glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
				window = glfwCreateWindow(width, height, EMU_NAME + " " + EMU_VERSION, NULL, NULL);
				if (window != NULL) {
					this.width = width;
					this.height = height;
					this.framebuffer = framebuffer;
					this.clear = clear;
					this.draw = draw;
					glfwMakeContextCurrent(window);
					glfwSetFramebufferSizeCallback(window, this::framebufferSizeCallback);
					GL.createCapabilities();
					shader = ShaderUtils.createProgram(new File("shaders/render.vert"), new File("shaders/render.frag"));
				}
			}
		}
	}
	public void startRender() {
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		glfwSwapInterval(0);
		glViewport(0, 0, width, height);
		int vbo = glGenBuffers();
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		setVertexAttributePointers();
		float[] attribs = calculateAttribs();
		glBufferData(GL_ARRAY_BUFFER, attribs, GL_DYNAMIC_DRAW);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glfwShowWindow(window);
		glUseProgram(shader);
		long lastFpsTime = 0L;
		long lastLoopTime = System.nanoTime();
		int fps = 0;
		glUniform2f(glGetUniformLocation(shader, "dim"), width, height);
		while (!glfwWindowShouldClose(window)) {
			long now = System.nanoTime();
			long updateLength = now-lastLoopTime;
			lastLoopTime = now;
			lastFpsTime += updateLength;
			if (lastFpsTime >= 1000000000) {
				glfwSetWindowTitle(window, EMU_NAME + " " + EMU_VERSION + " - FPS: " + fps);
				lastFpsTime = 0;
				fps = 0;
			}
			attribs = calculateAttribs();
			glBufferData(GL_ARRAY_BUFFER, attribs, GL_DYNAMIC_DRAW);
			processInput();
			glClear(GL_COLOR_BUFFER_BIT);
			glDrawArrays(GL_TRIANGLES, 0, 64*32*6);
		}
	}
	private void processInput() {
		if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) {
			Launcher.setKey(0, true);
		}
		// etc
	}
	private void setVertexAttributePointers() {
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 5*SizeOf.FLOAT, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 5*SizeOf.FLOAT, 2*SizeOf.FLOAT);
		glEnableVertexAttribArray(1);
	}
	private float[] calculateAttribs() {
		ArrayList<Float> attribs = new ArrayList<Float>();
		for (int a = 0; a < 64; a++) {
			for (int b = 0; b < 32; b++) {
				float x = ((float) width) / 64.0f;
				float y = ((float) height) / 32.0f;
				if (framebuffer[(64*b)+a] == 0) {
					attribs.add(a*x);
					attribs.add(b*y); // top left corner
					attribs.add(((float) clear.getRed())/255.0f);
					attribs.add(((float) clear.getGreen())/255.0f);
					attribs.add(((float) clear.getBlue())/255.0f);
					attribs.add(a*x);
					attribs.add((b*y)+y); // bottom left corner
					attribs.add(((float) clear.getRed())/255.0f);
					attribs.add(((float) clear.getGreen())/255.0f);
					attribs.add(((float) clear.getBlue())/255.0f);
					attribs.add((a*x)+x);
					attribs.add(b*y); // top right corner
					attribs.add(((float) clear.getRed())/255.0f);
					attribs.add(((float) clear.getGreen())/255.0f);
					attribs.add(((float) clear.getBlue())/255.0f);
					attribs.add((a*x)+x);
					attribs.add((b*y)+y); // bottom right corner
					attribs.add(((float) clear.getRed())/255.0f);
					attribs.add(((float) clear.getGreen())/255.0f);
					attribs.add(((float) clear.getBlue())/255.0f);
					attribs.add(a*x);
					attribs.add((b*y)+y); // bottom left corner
					attribs.add(((float) clear.getRed())/255.0f);
					attribs.add(((float) clear.getGreen())/255.0f);
					attribs.add(((float) clear.getBlue())/255.0f);
					attribs.add((a*x)+x);
					attribs.add(b*y); // top right corner
					attribs.add(((float) clear.getRed())/255.0f);
					attribs.add(((float) clear.getGreen())/255.0f);
					attribs.add(((float) clear.getBlue())/255.0f);
				} else if (framebuffer[(64*b)+a] == 1) {
					attribs.add(a*x);
					attribs.add(b*y); // top left corner
					attribs.add(((float) draw.getRed())/255.0f);
					attribs.add(((float) draw.getGreen())/255.0f);
					attribs.add(((float) draw.getBlue())/255.0f);
					attribs.add(a*x);
					attribs.add((b*y)+y); // bottom left corner
					attribs.add(((float) draw.getRed())/255.0f);
					attribs.add(((float) draw.getGreen())/255.0f);
					attribs.add(((float) draw.getBlue())/255.0f);
					attribs.add((a*x)+x);
					attribs.add(b*y); // top right corner
					attribs.add(((float) draw.getRed())/255.0f);
					attribs.add(((float) draw.getGreen())/255.0f);
					attribs.add(((float) draw.getBlue())/255.0f);
					attribs.add((a*x)+x);
					attribs.add((b*y)+y); // bottom right corner
					attribs.add(((float) draw.getRed())/255.0f);
					attribs.add(((float) draw.getGreen())/255.0f);
					attribs.add(((float) draw.getBlue())/255.0f);
					attribs.add(a*x);
					attribs.add((b*y)+y); // bottom left corner
					attribs.add(((float) draw.getRed())/255.0f);
					attribs.add(((float) draw.getGreen())/255.0f);
					attribs.add(((float) draw.getBlue())/255.0f);
					attribs.add((a*x)+x);
					attribs.add(b*y); // top right corner
					attribs.add(((float) draw.getRed())/255.0f);
					attribs.add(((float) draw.getGreen())/255.0f);
					attribs.add(((float) draw.getBlue())/255.0f);
				}
			}
		}
		float[] ret = new float[attribs.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = attribs.get(i);
		}
		return ret;
	}
	private void framebufferSizeCallback(long window, int width, int height) {
		glViewport(0, 0, width, height);
		this.width = width;
		this.height = height;
		glUniform2f(glGetUniformLocation(shader, "dim"), width, height);
	}
	public void terminate() {
		if (window != NULL) {
			glfwSetWindowShouldClose(window, true);
			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
			glfwTerminate();
		}
	}
	public void updateFramebuffer(int[] framebuffer) {
		this.framebuffer = framebuffer;
	}
}