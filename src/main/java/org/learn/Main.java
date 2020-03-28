package org.learn;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL4.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL4.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL4.GL_LINK_STATUS;
import static com.jogamp.opengl.GL4.GL_NO_ERROR;
import static com.jogamp.opengl.GL4.GL_POINTS;
import static com.jogamp.opengl.GL4.GL_VALIDATE_STATUS;
import static com.jogamp.opengl.GL4.GL_VERTEX_SHADER;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFrame;

public class Main extends JFrame implements GLEventListener {

  private GLCanvas myCanvas;
  private int rendering_program;
  private int vao[] = new int[1];

  public Main() {
    setTitle("Chapter2 - program1");
    setSize(600, 400);
    setLocation(200, 200);
    myCanvas = new GLCanvas();
    myCanvas.addGLEventListener(this);
    this.add(myCanvas);
    setVisible(true);
  }

  public void display(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    gl.glUseProgram(rendering_program);//указываем используемую программу(материал), загружает ее в GPU

    gl.glDrawArrays(GL_TRIANGLES, 0, 3);//отрисовываем 3 раза элемент буфера, это наш "draw call"!(у нас 1 элемент в буфере, но мы читаем его 3 раза, 3 вершины)
  }

  public static void main(String[] args) {
    new Main();
  }

  public void init(GLAutoDrawable drawable) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    rendering_program = createShaderProgram("shaders/start/vStart.glsl",
        "shaders/start/fStart.glsl");//компиляция шейдера, вернет его индекс.
    gl.glGenVertexArrays(vao.length, vao, 0);//создает буффер
    gl.glBindVertexArray(vao[0]);//связывет вертексный буффер с конвеером, копирует данные
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  public void dispose(GLAutoDrawable drawable) {
  }

  /**
   * Создает фактически материал- программа состоящая оз вертексного, фрагментного или др,
   * шейдеров.
   */
  private int createShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
    GL4 gl = (GL4) GLContext.getCurrentGL();
    // arrays to collect GLSL compilation status values.
    // note: one-element arrays are used because the associated JOGL calls require arrays.
    int[] vertCompiled = new int[1];
    int[] fragCompiled = new int[1];
    int[] linked = new int[1];
    int[] verified = new int[1];
    String vshaderSource[];
    String fshaderSource[];

    try {
      vshaderSource = readShaderSource(
          new File(getClass().getClassLoader().getResource(vertexShaderPath).getFile()));
      fshaderSource = readShaderSource(
          new File(getClass().getClassLoader().getResource(fragmentShaderPath).getFile()));
    } catch (Exception e) {
      throw new RuntimeException(
          "Не удалось загрузить файлы шейдеров " + vertexShaderPath + ", " + fragmentShaderPath, e);
    }


    int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
    gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
    gl.glCompileShader(vShader);
    checkOpenGLError();
    gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
    if (vertCompiled[0] == 1) {
      System.out.println("... vertex compilation success.");
    } else {
      System.out.println("... vertex compilation failed.");
      printShaderLog(vShader);
    }

    int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
    gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
    gl.glCompileShader(fShader);
    checkOpenGLError();
    gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
    if (fragCompiled[0] == 1) {
      System.out.println("... fragment compilation success.");
    } else {
      System.out.println("... fragment compilation failed.");
      printShaderLog(fShader);
    }

    if ((vertCompiled[0] != 1) || (fragCompiled[0] != 1)) {
      System.out.println("\nCompilation error; return-flags:");
      System.out
          .println(" vertCompiled = " + vertCompiled[0] + " ; fragCompiled = " + fragCompiled[0]);
    } else {
      System.out.println("Successful compilation");
    }

    int vfprogram = gl.glCreateProgram();
    gl.glAttachShader(vfprogram, vShader);
    gl.glAttachShader(vfprogram, fShader);
    gl.glLinkProgram(vfprogram);
    checkOpenGLError();
    gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
    if (linked[0] == 1) {
      System.out.println("... linking succeeded.");

      gl.glValidateProgram(vfprogram);
      gl.glGetProgramiv(vfprogram, GL_VALIDATE_STATUS, verified, 0);
      if (verified[0] == 1) {
        System.out.println("Program " + vfprogram + " successfully verified");
      } else {
        printProgramLog(vfprogram);
      }

    } else {
      System.out.println("... linking failed.");
      printProgramLog(vfprogram);
    }

    gl.glDeleteShader(vShader);
    gl.glDeleteShader(fShader);
    return vfprogram;
  }

    private void printShaderLog ( int shader){
      GL4 gl = (GL4) GLContext.getCurrentGL();
      int[] len = new int[1];
      int[] chWrittn = new int[1];
      byte[] log = null;
      gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
      if (len[0] > 0) {
        log = new byte[len[0]];
        gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
        System.out.println("Shader Info Log: ");
        for (int i = 0; i < log.length; i++) {
          System.out.print((char) log[i]);
        }
      }
    }

    void printProgramLog ( int prog){
      GL4 gl = (GL4) GLContext.getCurrentGL();
      int[] len = new int[1];
      int[] chWrittn = new int[1];
      byte[] log = null;
      gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
      if (len[0] > 0) {
        log = new byte[len[0]];
        gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
        System.out.println("Program Info Log: ");
        for (int i = 0; i < log.length; i++) {
          System.out.print((char) log[i]);
        }
      }
    }

    boolean checkOpenGLError () {
      GL4 gl = (GL4) GLContext.getCurrentGL();
      boolean foundError = false;
      GLU glu = new GLU();
      int glErr = gl.glGetError();
      while (glErr != GL_NO_ERROR) {
        System.err.println("glError: " + glu.gluErrorString(glErr));
        foundError = true;
        glErr = gl.glGetError();
      }
      return foundError;
    }

  /**
   * Получает строки шейдерного файла.
   * @param file
   * @return
   * @throws IOException
   */
    private String[] readShaderSource (File file) throws IOException {
      List<String> strings = Files.readAllLines(file.toPath()).stream()
          .map(s->s+"\n").collect(Collectors.toList());//в каждую строку обязательно "\n" иначе не скомпилируется шэйдер.
      return strings.toArray(new String[strings.size()]);
    }

  }
