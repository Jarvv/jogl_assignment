import gmaths.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class Arty extends JFrame implements ActionListener {
  
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private static final Dimension dimension = new Dimension(WIDTH, HEIGHT);
  private GLCanvas canvas;
  private Arty_GLEventListener glEventListener;
  private final FPSAnimator animator; 
  private Camera camera;

  public static void main(String[] args) {
    Arty arty = new Arty("Arty");
    arty.getContentPane().setPreferredSize(dimension);
    arty.pack();
    arty.setVisible(true);
  }

  public Arty(String textForTitleBar) {
    super(textForTitleBar);
    GLCapabilities glcapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL3));
    canvas = new GLCanvas(glcapabilities);
    camera = new Camera(new Vec3(6f,8f,0f), Camera.DEFAULT_TARGET, Camera.DEFAULT_UP);
    glEventListener = new Arty_GLEventListener(camera);
    canvas.addGLEventListener(glEventListener);
    canvas.addMouseMotionListener(new MyMouseInput(camera));
    canvas.addKeyListener(new MyKeyboardInput(camera));
    getContentPane().add(canvas, BorderLayout.CENTER);
    
    JPanel p = new JPanel();
      JButton b = new JButton("Start");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Stop");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Reset");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("J Gesture");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("A Gesture");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("M Gesture");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Peace Gesture");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("Lamps On/Off");
      b.addActionListener(this);
      p.add(b);
      b = new JButton("World Light On/Off");
      b.addActionListener(this);
      p.add(b);
    this.add(p, BorderLayout.SOUTH);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        animator.stop();
        remove(canvas);
        dispose();
        System.exit(0);
      }
    });

    animator = new FPSAnimator(canvas, 60);
    animator.start();
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("Start")){
      glEventListener.startAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Stop")){
      glEventListener.stopAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Reset")){
      glEventListener.resetAnimation();
    }
    else if (e.getActionCommand().equalsIgnoreCase("J Gesture")){
      glEventListener.jGesture();
    }
    else if (e.getActionCommand().equalsIgnoreCase("A Gesture")){
      glEventListener.aGesture();
    }
    else if (e.getActionCommand().equalsIgnoreCase("M Gesture")){
      glEventListener.mGesture();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Peace Gesture")){
      glEventListener.peaceGesture();
    }
    else if (e.getActionCommand().equalsIgnoreCase("Lamps On/Off")){
      glEventListener.lightSwitch();
    }
    else if (e.getActionCommand().equalsIgnoreCase("World Light On/Off")){
      glEventListener.globalLightSwitch();
    }
  }
}

// Camera Controls used from previous exercises.
class MyKeyboardInput extends KeyAdapter  {
  private Camera camera;
  
  public MyKeyboardInput(Camera camera) {
    this.camera = camera;
  }
  
  public void keyPressed(KeyEvent e) {
    Camera.Movement m = Camera.Movement.NO_MOVEMENT;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT:  m = Camera.Movement.LEFT;  break;
      case KeyEvent.VK_RIGHT: m = Camera.Movement.RIGHT; break;
      case KeyEvent.VK_UP:    m = Camera.Movement.UP;    break;
      case KeyEvent.VK_DOWN:  m = Camera.Movement.DOWN;  break;
      case KeyEvent.VK_A:  m = Camera.Movement.FORWARD;  break;
      case KeyEvent.VK_Z:  m = Camera.Movement.BACK;  break;
    }
    camera.keyboardInput(m);
  }
}

// Mouse Controls used from previous exercises.
class MyMouseInput extends MouseMotionAdapter {
  private Point lastpoint;
  private Camera camera;
  
  public MyMouseInput(Camera camera) {
    this.camera = camera;
  }
  
    /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */    
  public void mouseDragged(MouseEvent e) {
    Point ms = e.getPoint();
    float sensitivity = 0.001f;
    float dx=(float) (ms.x-lastpoint.x)*sensitivity;
    float dy=(float) (ms.y-lastpoint.y)*sensitivity;
    //System.out.println("dy,dy: "+dx+","+dy);
    if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
      camera.updateYawPitch(dx, -dy);
    lastpoint = ms;
  }

  /**
   * mouse is used to control camera position
   *
   * @param e  instance of MouseEvent
   */  
  public void mouseMoved(MouseEvent e) {   
    lastpoint = e.getPoint(); 
  }
}