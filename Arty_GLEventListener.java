import gmaths.*;
import java.nio.*;
import org.w3c.dom.NamedNodeMap;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import meshes.*;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class Arty_GLEventListener implements GLEventListener {
  
  private static final boolean DISPLAY_SHADERS = false;
  private float aspect;
  
  // TransformNodes for the rotations of each finger segment.
  private TransformNode littleFingerRotate, littleFingerMiddleRotate, littleFingerTopRotate;
  private TransformNode ringFingerRotate, ringFingerMiddleRotate, ringFingerTopRotate;
  private TransformNode middleFingerRotate, middleFingerMiddleRotate, middleFingerTopRotate;
  private TransformNode indexFingerRotate, indexFingerMiddleRotate, indexFingerTopRotate;
  private TransformNode thumbRotate, thumbMiddleRotate, thumbTopRotate;

  // For the scene and lighting
  private Camera camera;
  private Mat4 perspective, trans;
  private SGNode hand;
  private TransformNode translateX, rotateThumbRest;
  private float xPosition = 0;
  private Mesh outside, wall, ring, room, diffuseCube, specularCube, painting, lamp;
  private boolean worldLightOn = true, lampLightOn = true, worldLightChanged = false, lampLightChanged = false;

  // A 2D array for the values of the rotations for each rotation segment for each gesture.
  private float[][] gestureRotations = new float[][]{
    {0  ,0  ,  0, -80,-90,-90, -70,-90,-90, -60,-90,-90, 20,-30,20, 0,20, 0}, // J Gestrue
    {-90,-90,-90, -80,-90,-90, -70,-90,-90, -60,-90,-90, 20,-30,20, 0,20, 0}, // A Gesture
    {-90,-90,-90, -40,-90,-90, -70,-90,-60, -80,-90,-30, 20,-35, 60,10, 20, 0}, // M Gesture
    {-90,-90,-90, -80,-90,-90,  -0, -0, -0,   0,  0,  0,  20,-55, 60,10, 10, 0}, // Peace Gesture
    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0} // Default
  };

  // Array which contains the TransformNodes of the finger/thumb rotations.
  private TransformNode[] rotationSequence;
  
  public Arty_GLEventListener(Camera camera) {
    this.camera = camera;
  }
  
  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {   
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); 
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }
  
  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    aspect = (float)width/(float)height;
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory, if necessary */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    disposeMeshes(gl);
  }

  // ***************************************************
  /* TIME
   */ 

  private double startTime;
  private double savedTime = 0;
  private double elapsedTime = 0;
  
  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }

  // ***************************************************
  /* INTERACTION
   *
   *
  */

  private boolean animation = false;

  // Indicates which gesture is currently playing.
  private boolean jGesture = false, aGesture = false, mGesture = false, peaceGesture = false;

  // Sets the animation and the first gesture to true.
  public void startAnimation() {
    animation = true;
    jGesture = true;
    aGesture = false; 
    mGesture = false; 
    peaceGesture = false;
    startTime = getSeconds()-savedTime;
    savedTime = 0.0;
  }
   
  public void stopAnimation() {
    animation = false;
    elapsedTime = getSeconds()-startTime;
    savedTime = elapsedTime;
  }
   
  // Reset any playing gestures to false and reset the rotation transforms back to 0 (the original resting state)
  public void resetAnimation() {
    jGesture = false;
    aGesture = false; 
    mGesture = false; 
    peaceGesture = false;
    animation = false;

    for(TransformNode rotNode: rotationSequence){
      rotNode.setTransform(Mat4.multiply(Mat4Transform.rotateAroundX(0), Mat4Transform.rotateAroundZ(0)));
      rotNode.update();
    }
  }

  /*
  * xGesture
  * Called from the glEventListener, sets the playing gesture to true and gets the start time.
  */
  public void jGesture() {
    jGesture = true;
    startTime = getSeconds()-savedTime;
  }

  /*
  * doXGesture
  * If the related gesture boolean is true then this method will be called from render until the
  * elapsed time of the transform reaches 1 second at which point the next gesture in the sequence
  * will be played in inSequence is true. The start time is also updated.
  *
  * inSequence - If true then the next gesture in the animation sequence will play next,
  *              if false then no animation will be played after the current has finished.
  */
  public void doJGesture(boolean inSequence){
    int i = 0;
    jGesture = true;
    elapsedTime = getSeconds()-startTime;

    doTransform(elapsedTime, inSequence, i);

    if (elapsedTime > 1){
      if(inSequence){
        jGesture = false;
        startTime = getSeconds();
        aGesture = true;
      }
      else{
        jGesture = false;
      }
    }
  }

  public void aGesture() {
    aGesture = true;
    startTime = getSeconds()-savedTime;
  }

  public void doAGesture(boolean inSequence){
    int i = 1;
    aGesture= true;
    elapsedTime = getSeconds()-startTime;

    doTransform(elapsedTime, inSequence, i);

    if(elapsedTime > 1){
      if(inSequence){
        aGesture = false;
        startTime = getSeconds();
        mGesture = true;
      }
      else{
        aGesture = false;
      }
    }
  }

  public void mGesture(){
    mGesture = true;
    startTime = getSeconds()-savedTime;
  }

  public void doMGesture(boolean inSequence) {
    int i = 2;
    mGesture= true;
    elapsedTime = getSeconds()-startTime;

    doTransform(elapsedTime, inSequence, i);

    if(elapsedTime > 1){
      if(inSequence){
        mGesture = false;
        startTime = getSeconds();
        peaceGesture = true;
      }
      else{
        mGesture = false;
      }
    }
  }

  public void peaceGesture(){
    peaceGesture = true;
    startTime = getSeconds()-savedTime;
  }

  public void doPeaceGesture(boolean inSequence) {
    int i = 3, counter = 0;
    peaceGesture = true;
    elapsedTime = getSeconds()-startTime;

    doTransform(elapsedTime, inSequence, i);

    if (elapsedTime > 1){
      peaceGesture = false;
      animation = false;  
    }
  }

  // Liner interpolation between the startPos and finalPos.
  float interpolate(float startPos, float finalPos, double time) {
    return startPos + (float)((finalPos - startPos) * time);
  }

  /*
  * Performs the rotations on each of the rotation nodes of the fingers for the chosen gesture.
  *
  * time - The current elapsed time of the gesture that is used in interpolation
  * inSequence - If true then the animation will start at the previous animations end position,
  *               of false then the animation is played from the hands resting position.
  * i - Which gesture is to be played according to the gestureRotations array.
  */
  public void doTransform(double time, boolean inSequence, int i){
    // Used for thumb rotations as they have both an x and y component.
    int counter = 0;

    for(int j=0; j< rotationSequence.length; j++){
      TransformNode rotNode = rotationSequence[j];
      float rotAngle = gestureRotations[i][j];
      float secRotAngle = 0f;
      float startPos = 0f;
      float secondStartPos = 0f;
      float rotateAngle = 0f;
      float secondRotateAngle = 0f;
      
      // Fingers
      if(j < 12){
        if(inSequence && i != 0){
          startPos = gestureRotations[i-1][j];
          rotateAngle = interpolate(startPos, rotAngle, time);
        }
        // Get rotation angle and perform rotation
        rotateAngle = interpolate(startPos, rotAngle, time);
        rotNode.setTransform(Mat4Transform.rotateAroundZ(rotateAngle));
      }
      // Thumb
      else{
        if(inSequence && i !=0){
          startPos = gestureRotations[i-1][counter];
          rotAngle = gestureRotations[i][counter];
          secondStartPos = gestureRotations[i-1][counter+1];
          secRotAngle = gestureRotations[i][counter+1];
        }else if(inSequence && i == 0){
          startPos = 0;
          rotAngle = gestureRotations[i][counter];
          secondStartPos = 0;
          secRotAngle = gestureRotations[i][counter+1];
        }else{
          startPos = 0;
          rotAngle = gestureRotations[i][counter];
          secondStartPos = 0;
          secRotAngle = gestureRotations[i][counter+1];
        }
        
        // Get rotation angles and perform rotation
        rotateAngle = interpolate(startPos, rotAngle, time); 
        secondRotateAngle = interpolate(secondStartPos, secRotAngle, time); 
        rotNode.setTransform(Mat4.multiply(Mat4Transform.rotateAroundX(rotateAngle), Mat4Transform.rotateAroundZ(secondRotateAngle)));
        
        counter +=1;
      }
      counter +=1;
    }

    // Only parent nodes need to call update()
    littleFingerRotate.update();
    ringFingerRotate.update();
    middleFingerRotate.update();
    indexFingerRotate.update();
    thumbRotate.update();
  }

  /*
  * xSwitch
  * Called from the glEventListener, sets the xLightChanged boolean to true, indicating on the
  * next render call that the light type will be updated (turned on or off).
  */
  public void lightSwitch(){
    lampLightChanged = true;
  }

  public void globalLightSwitch(){
    worldLightChanged = true;
  }

  /* 
  * doUpdateXLight
  * Switches the boolean value of the xLighOn variable, then calls the updateWorldLight method
  * on each of the meshes in the scene that we want the light to cast on. 
  */
  public void doUpdateWorldLight(){
    worldLightChanged = false;
    if(worldLightOn)
      worldLightOn = false;
    else
      worldLightOn = true;

    // Update all meshes
    room.updateWorldLight();
    diffuseCube.updateWorldLight();
    specularCube.updateWorldLight();
    ring.updateWorldLight();
    outside.updateWorldLight();
    painting.updateWorldLight();
  }
  
  private Material lampMaterial;

  // Also updates the material of the lamp light, to give the impression of a lightbulb being on/off.
  public void doUpdateLampLight(){
    lampMaterial = new Material();
    lampLightChanged = false;
    if(lampLightOn){
      lampMaterial.setAmbient(0.1f, 0.1f, 0.1f);
      lampMaterial.setDiffuse(0.1f, 0.1f, 0.1f);
      lampMaterial.setSpecular(0.1f, 0.1f, 0.1f);
      lampLightOn = false;
    }
    else{
      lampMaterial.setAmbient(0.5f, 0.5f, 0.5f);
      lampMaterial.setDiffuse(0.5f, 0.5f, 0.5f);
      lampMaterial.setSpecular(1.0f, 1.0f, 1.0f);
      lampLightOn = true;
    }

    lamp.setMaterial(lampMaterial);
    room.updateLampLight();
    diffuseCube.updateLampLight();
    specularCube.updateLampLight();
    ring.updateLampLight();
    painting.updateLampLight();
    // Do not want the outside mesh to be affected by the lamp lights in the room.
  }
  
  // ***************************************************
  /* THE SCENE
   */

  private void initialise(GL3 gl) {
    // Load Textures
    int[] textureId0 = TextureLibrary.loadTexture(gl, "textures/floor.jpg");
    int[] textureId1 = TextureLibrary.loadTexture(gl, "textures/jade.jpg");
    int[] textureId2 = TextureLibrary.loadTexture(gl, "textures/jade_specular.jpg");
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/wall.jpg");
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/ear0xuu2.jpg");
    int[] textureId5 = TextureLibrary.loadTexture(gl, "textures/metal.jpg");
    int[] textureId6 = TextureLibrary.loadTexture(gl, "textures/metal_specular.jpg");
    int[] textureId7 = TextureLibrary.loadTexture(gl, "textures/grass.jpg");
    int[] textureId8 = TextureLibrary.loadTexture(gl, "textures/sky.jpg");
    int[] textureId9 = TextureLibrary.loadTexture(gl, "textures/cloud.jpg");
    
    // Make meshes
    outside = new Outside(gl, textureId7, textureId8, textureId9);
    room = new Room(gl, textureId0, textureId3);
    painting = new TwoTriangles(gl, textureId4);
    lamp = new Cube(gl);

    /*
    * diffuseCube - Only takes a diffuse texture, material properties are set to give it a worn metal look.
    * specularCube - Takes a diffuse and specular texture, material properties lead it to being more of a new
    * 'shiny' sort of metal.
    */
    diffuseCube = new DiffuseHand(gl,textureId5);
    specularCube = new SpecularHand(gl,textureId5,textureId6);

    // The ring is a cube which is positioned in the centre of a finger segment,
    // giving the impression of a ring shape.
    ring = new Ring(gl, textureId1,textureId2);

    // Set Camera
    ring.setCamera(camera);
    diffuseCube.setCamera(camera);
    specularCube.setCamera(camera);
    outside.setCamera(camera);
    room.setCamera(camera);
    painting.setCamera(camera);
    lamp.setCamera(camera);

    // Make mesh nodes for robot hand
    MeshNode armShape = new MeshNode("Cube(0)", diffuseCube);
    MeshNode handShape = new MeshNode("Cube(1)", diffuseCube);

    // Little finger
    MeshNode lowerLF = new MeshNode("Cube(2)", diffuseCube);
    MeshNode middleLF = new MeshNode("Cube(3)", specularCube);
    MeshNode topLF = new MeshNode("Cube(4)", diffuseCube);

    // Ring Finger
    MeshNode lowerRF = new MeshNode("Cube(5)", specularCube);
    MeshNode middleRF = new MeshNode("Cube(6)", diffuseCube);
    MeshNode topRF = new MeshNode("Cube(7)", diffuseCube);

    // Ring
    MeshNode ringShape = new MeshNode("Ring(1)", ring);
    MeshNode gemstoneShape = new MeshNode("Gemstone", ring);

    // Middle Finger
    MeshNode lowerMF = new MeshNode("Cube(8)", diffuseCube);
    MeshNode middleMF = new MeshNode("Cube(9)", diffuseCube);
    MeshNode topMF = new MeshNode("Cube(10)", specularCube);

    // Index Finger
    MeshNode lowerIF = new MeshNode("Cube(11)", specularCube);
    MeshNode middleIF = new MeshNode("Cube(12)", diffuseCube);
    MeshNode topIF = new MeshNode("Cube(13)", specularCube);

    // Thumb
    MeshNode lowerTh = new MeshNode("Cube(14)", specularCube);
    MeshNode middleTh = new MeshNode("Cube(15)", specularCube);
    MeshNode topTh = new MeshNode("Cube(16)", diffuseCube);

    // Name Nodes for robot hand
    hand = new NameNode("hand structure");
    NameNode arm_Branch = new NameNode("arm");
    NameNode hand_Branch = new NameNode("hand");

    NameNode littleFinger = new NameNode("little finger");
    NameNode litteFingerLower = new NameNode("little finger lower");
    NameNode littleFingerMiddle = new NameNode("little finger middle");
    NameNode littleFingerTop = new NameNode("little finger top");

    NameNode ringFinger = new NameNode("ring finger");
    NameNode ringFingerLower = new NameNode("ring finger lower");
    NameNode ringFingerMiddle = new NameNode("ring finger middle");
    NameNode ringFingerTop = new NameNode("ring finger top");

    NameNode ring = new NameNode("ring");
    NameNode gemstone = new NameNode("gemstone");

    NameNode middleFinger = new NameNode("middle finger");
    NameNode middleFingeLower = new NameNode("middle finger lower");
    NameNode middleFingerMiddle = new NameNode("middle finger middle");
    NameNode middleFingerTop = new NameNode("middle finger top");

    NameNode indexFinger = new NameNode("index finger");
    NameNode indexFingerLower = new NameNode("index finger lower");
    NameNode indexFingerMiddle = new NameNode("index finger middle");
    NameNode indexFingerTop = new NameNode("index finger top");

    NameNode thumb = new NameNode("thumb");
    NameNode thumbLower = new NameNode("thumb lower");
    NameNode thumbMiddle = new NameNode("thumb middle");
    NameNode thumbTop = new NameNode("thumb top");

    // Lengths of each segment for the robot hand.
    // Little finger - 3 small segments
    // Ring finger - 3 medium segments
    // Middle finger - 3 large segments
    // Index finger - 2 medium, 1 large segment
    // Thumn - 1 medium, 2 large segments
    float smallSegment = 0.5f;
    float mediumSegment = 0.75f;
    float largeSegment = 1f;
    float handSegment = 2f;
    float armSegment = 3f;

    // Position in centre of the world.
    translateX = new TransformNode("translate("+xPosition+",0,0)", Mat4Transform.translate(xPosition,0,0));

    //Arm
    Mat4 robotTrans = Mat4Transform.scale(0.5f,armSegment,1.5f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeArm = new TransformNode("scale(0.5f,3,1.5f);translate(0,0.5f,0)", robotTrans);

    //Hand
    TransformNode translateHandToArm = new TransformNode("translate(0,3,0)",Mat4Transform.translate(0,armSegment,0));
    robotTrans = Mat4Transform.scale(0.5f,handSegment,handSegment);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeHand = new TransformNode("scale(0.5f,2,2);translate(0,0.5f,0)", robotTrans);

    //Fingers

    //Little Finger
    TransformNode translateLfToHand = new TransformNode("translate(0,2,0.8f)", Mat4Transform.translate(0,handSegment,0.8f));
    littleFingerRotate = new TransformNode("little finger rotate",Mat4Transform.rotateAroundX(0));
    robotTrans = Mat4Transform.scale(0.3f, smallSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeLowerLF = new TransformNode("scale(0.3f, 0.5f,0.3f);translate(0,0.5f,0)", robotTrans);
    
    TransformNode translateLfLowerToMid = new TransformNode("translate(0,0.5f,0)", Mat4Transform.translate(0,smallSegment,0));
    littleFingerMiddleRotate = new TransformNode("little finger middle rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeMiddleLF = new TransformNode("scale(0.3f, 0.5f,0.3f);translate(0,0.5f,0)", robotTrans);
    
    TransformNode translateLfMidToTop = new TransformNode("translate(0,0.5f,0)", Mat4Transform.translate(0,smallSegment,0));
    littleFingerTopRotate = new TransformNode("little finger middle rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeTopLF = new TransformNode("scale(0.3f, 0.5f,0.3f);translate(0,0.5f,0)", robotTrans);

    //Ring Finger
    TransformNode translateRfToHand = new TransformNode("translate(0,2,0.3f)", Mat4Transform.translate(0,handSegment,0.3f));
    ringFingerRotate = new TransformNode("ring finger rotate",Mat4Transform.rotateAroundX(0));
    robotTrans = Mat4Transform.scale(0.3f, mediumSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeLowerRF = new TransformNode("scale(0.3f, 0.75f,0.3f);translate(0,0.5f,0)", robotTrans);

    TransformNode translateRfLowerToMid = new TransformNode("translate(0,0.75f,0)", Mat4Transform.translate(0,mediumSegment,0));
    ringFingerMiddleRotate = new TransformNode("ring finger middle rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeMiddleRF = new TransformNode("scale(0.3f, 0.75f,0.3f);translate(0,0.5f,0)", robotTrans);
    
    TransformNode translateRfMidToTop = new TransformNode("translate(0,0.75f,0)", Mat4Transform.translate(0,mediumSegment,0));
    ringFingerTopRotate = new TransformNode("ring finger top rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeTopRF = new TransformNode("scale(0.3f, 0.75f,0.3f);translate(0,0.5f,0)", robotTrans);

    //Ring
    TransformNode translateRing = new TransformNode("translate(0,0.375f,0)", Mat4Transform.translate(0f,mediumSegment/2f,0f));
    robotTrans = Mat4Transform.scale(0.5f,0.25f,0.5f);
    TransformNode makeRing = new TransformNode("scale(0.5f,0.25f,0.5f);translate(0,0.375f,0)", robotTrans);

    //Gemstone
    TransformNode translateGemstoneToRing = new TransformNode("translate(-0.25f,0,0)", Mat4Transform.translate(-0.25f,0,0));
    robotTrans = Mat4Transform.scale(0.1f,0.1f,0.1f);
    TransformNode makeGemstone = new TransformNode("scale(0.1f,0.1f,0.1f);translate(0,0.25f,0)", robotTrans);
    
    //Middle Finger
    TransformNode translateMfToHand = new TransformNode("translate(0,2,-0.3f)", Mat4Transform.translate(0,handSegment,-0.3f));
    middleFingerRotate = new TransformNode("middle finger rotate",Mat4Transform.rotateAroundX(0));
    robotTrans = Mat4Transform.scale(0.3f, largeSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeLowerMF = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);

    TransformNode translateMfLowerToMid = new TransformNode("translate(0,1f,0)", Mat4Transform.translate(0,largeSegment,0));
    middleFingerMiddleRotate = new TransformNode("middle finger middle rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeMiddleMF = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);
    
    TransformNode translateMfMidToTop = new TransformNode("translate(0,1f,0)", Mat4Transform.translate(0,largeSegment,0));
    middleFingerTopRotate = new TransformNode("middle finger top rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeTopMF = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);

    //Index Finger
    TransformNode translateIfToHand = new TransformNode("translate(0,2,-0.8f)", Mat4Transform.translate(0,handSegment,-0.8f));
    indexFingerRotate = new TransformNode("index finger rotate",Mat4Transform.rotateAroundX(0));
    robotTrans = Mat4Transform.scale(0.3f, mediumSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeLowerIF = new TransformNode("scale(0.3f, 0.75f,0.3f);translate(0,0.5f,0)", robotTrans);

    TransformNode translateIfLowerToMid = new TransformNode("translate(0,0.75f,0)", Mat4Transform.translate(0,mediumSegment,0));
    indexFingerMiddleRotate = new TransformNode("index finger middle rotate",Mat4Transform.rotateAroundX(0));
    robotTrans = Mat4Transform.scale(0.3f, largeSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeMiddleIF = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);
    
    TransformNode translateIfMidToTop = new TransformNode("translate(0,1f,0)", Mat4Transform.translate(0,largeSegment,0));
    indexFingerTopRotate = new TransformNode("index finger top rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeTopIF = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);

    //Thumb
    TransformNode translateThToHand = new TransformNode("translate(0,0,-1f)", Mat4Transform.translate(0,0,-1f));
    thumbRotate = new TransformNode("thumb rotate",Mat4Transform.rotateAroundX(0));
    rotateThumbRest = new TransformNode("rotateAroundX(-45)",Mat4Transform.rotateAroundX(-45));
    robotTrans = Mat4Transform.scale(0.3f, mediumSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeLowerTH = new TransformNode("scale(0.3f, 0.75f,0.3f);translate(0,0.5f,0)", robotTrans);

    TransformNode translateThLowerToMid = new TransformNode("translate(0,0.75f,0)", Mat4Transform.translate(0,mediumSegment,0));
    thumbMiddleRotate = new TransformNode("thumb middle rotate",Mat4Transform.rotateAroundX(0));
    robotTrans = Mat4Transform.scale(0.3f, largeSegment, 0.3f);
    robotTrans = Mat4.multiply(robotTrans, Mat4Transform.translate(0,0.5f,0));
    TransformNode makeMiddleTH = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);
    
    TransformNode translateThMidToTop = new TransformNode("translate(0,1f,0)", Mat4Transform.translate(0,largeSegment,0));
    thumbTopRotate = new TransformNode("thumb top rotate",Mat4Transform.rotateAroundX(0));
    TransformNode makeTopTH = new TransformNode("scale(0.3f, 1f,0.3f);translate(0,0.5f,0)", robotTrans);

    // Hand Scene Graph
    hand.addChild(translateX);

        // Arm
        translateX.addChild(arm_Branch);
          arm_Branch.addChild(makeArm);
              makeArm.addChild(armShape);

          // Hand
          arm_Branch.addChild(translateHandToArm);
            translateHandToArm.addChild(hand_Branch);
              hand_Branch.addChild(makeHand);
                makeHand.addChild(handShape);

              // Little Finger
              hand_Branch.addChild(translateLfToHand);
                translateLfToHand.addChild(littleFingerRotate);
                  littleFingerRotate.addChild(littleFinger);
                    //Lower
                    littleFinger.addChild(litteFingerLower);
                      litteFingerLower.addChild(makeLowerLF);
                        makeLowerLF.addChild(lowerLF);
                      //Middle
                      litteFingerLower.addChild(translateLfLowerToMid);
                        translateLfLowerToMid.addChild(littleFingerMiddleRotate);
                          littleFingerMiddleRotate.addChild(littleFingerMiddle);
                            littleFingerMiddle.addChild(makeMiddleLF);
                              makeMiddleLF.addChild(middleLF);
                            //Top     
                            littleFingerMiddle.addChild(translateLfMidToTop);
                              translateLfMidToTop.addChild(littleFingerTopRotate);
                                littleFingerTopRotate.addChild(littleFingerTop);
                                  littleFingerTop.addChild(makeTopLF);
                                    makeTopLF.addChild(topLF);

              // Ring Finger
              hand_Branch.addChild(translateRfToHand);
                translateRfToHand.addChild(ringFingerRotate);
                  ringFingerRotate.addChild(ringFinger);
                    //Lower
                    ringFinger.addChild(ringFingerLower);
                      ringFingerLower.addChild(makeLowerRF);
                        makeLowerRF.addChild(lowerRF);
                      // Ring
                      ringFinger.addChild(translateRing);
                        translateRing.addChild(ring);
                          ring.addChild(makeRing);
                            makeRing.addChild(ringShape);
                          // Gemstone
                          ring.addChild(translateGemstoneToRing);
                            translateGemstoneToRing.addChild(makeGemstone);
                              makeGemstone.addChild(gemstoneShape);
                      //Middle
                      ringFingerLower.addChild(translateRfLowerToMid);
                        translateRfLowerToMid.addChild(ringFingerMiddleRotate);
                          ringFingerMiddleRotate.addChild(ringFingerMiddle);
                            ringFingerMiddle.addChild(makeMiddleRF);
                              makeMiddleRF.addChild(middleRF);
                            //Top     
                            ringFingerMiddle.addChild(translateRfMidToTop);
                              translateRfMidToTop.addChild(ringFingerTopRotate);
                                ringFingerTopRotate.addChild(ringFingerTop);
                                  ringFingerTop.addChild(makeTopRF);
                                    makeTopRF.addChild(topRF);

              // Middle Finger
              hand_Branch.addChild(translateMfToHand);
                translateMfToHand.addChild(middleFingerRotate);
                  middleFingerRotate.addChild(middleFinger);
                    //Lower
                    middleFinger.addChild(middleFingeLower);
                      middleFingeLower.addChild(makeLowerMF);
                        makeLowerMF.addChild(lowerMF);
                      //Middle
                      middleFingeLower.addChild(translateMfLowerToMid);
                        translateMfLowerToMid.addChild(middleFingerMiddleRotate);
                          middleFingerMiddleRotate.addChild(middleFingerMiddle);
                            middleFingerMiddle.addChild(makeMiddleMF);
                              makeMiddleMF.addChild(middleMF);
                            //Top     
                            middleFingerMiddle.addChild(translateMfMidToTop);
                              translateMfMidToTop.addChild(middleFingerTopRotate);
                                middleFingerTopRotate.addChild(middleFingerTop);
                                  middleFingerTop.addChild(makeTopMF);
                                    makeTopMF.addChild(topMF);

              // Index Finger
              hand_Branch.addChild(translateIfToHand);
                translateIfToHand.addChild(indexFingerRotate);
                  indexFingerRotate.addChild(indexFinger);
                    //Lower
                    indexFinger.addChild(indexFingerLower);
                      indexFingerLower.addChild(makeLowerIF);
                        makeLowerIF.addChild(lowerIF);
                      //Middle
                      indexFingerLower.addChild(translateIfLowerToMid);
                        translateIfLowerToMid.addChild(indexFingerMiddleRotate);
                          indexFingerMiddleRotate.addChild(indexFingerMiddle);
                            indexFingerMiddle.addChild(makeMiddleIF);
                              makeMiddleIF.addChild(middleIF);
                            //Top     
                            indexFingerMiddle.addChild(translateIfMidToTop);
                              translateIfMidToTop.addChild(indexFingerTopRotate);
                                indexFingerTopRotate.addChild(indexFingerTop);
                                  indexFingerTop.addChild(makeTopIF);
                                    makeTopIF.addChild(topIF);

              // Thumb
              hand_Branch.addChild(translateThToHand);
                translateThToHand.addChild(rotateThumbRest);
                  //Rotate Thumb To Rest position
                  rotateThumbRest.addChild(thumbRotate);
                    thumbRotate.addChild(thumb);
                      //Lower
                      thumb.addChild(thumbLower);
                        thumbLower.addChild(makeLowerTH);
                          makeLowerTH.addChild(lowerTh);
                        //Middle
                        thumbLower.addChild(translateThLowerToMid);
                          translateThLowerToMid.addChild(thumbMiddleRotate);
                            thumbMiddleRotate.addChild(thumbMiddle);
                              thumbMiddle.addChild(makeMiddleTH);
                                makeMiddleTH.addChild(middleTh);
                              //Top     
                              thumbMiddle.addChild(translateThMidToTop);
                                translateThMidToTop.addChild(thumbTopRotate);
                                  thumbTopRotate.addChild(thumbTop);
                                    thumbTop.addChild(makeTopTH);
                                      makeTopTH.addChild(topTh);
                                

    hand.update();

    // Set Painting Position
    painting.setModelMatrix(Mat4.multiply(Mat4.multiply(Mat4Transform.scale(6,4,1),Mat4Transform.rotateAroundX(90)),Mat4Transform.translate(0,-8.999f,-1)));

    // Set Lamp Position
    lamp.setModelMatrix(Mat4Transform.translate(7,10,7));

    rotationSequence = new TransformNode[]{
      littleFingerRotate,littleFingerMiddleRotate,littleFingerTopRotate,
      ringFingerRotate,ringFingerMiddleRotate,ringFingerTopRotate,
      middleFingerRotate,middleFingerMiddleRotate,middleFingerTopRotate,
      indexFingerRotate,indexFingerMiddleRotate,indexFingerTopRotate,
      thumbRotate,thumbMiddleRotate,thumbTopRotate
    };
  }
 
  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    updatePerspectiveMatrices();

    // Render the room, outside and hand
    outside.render(gl);
    painting.render(gl);
    room.render(gl);
    lamp.render(gl);
    hand.draw(gl);
    
    // Animation Controls
    if (animation) updateFingers();
    else if (jGesture) doJGesture(false);
    else if (aGesture) doAGesture(false);
    else if (mGesture) doMGesture(false);
    else if (peaceGesture) doPeaceGesture(false);

    // Light Controls
    if (worldLightChanged) doUpdateWorldLight();
    if (lampLightChanged) doUpdateLampLight();
  }
  
  private void updatePerspectiveMatrices() {
    perspective = Mat4Transform.perspective(45, aspect);
    ring.setPerspective(perspective);
    diffuseCube.setPerspective(perspective);
    specularCube.setPerspective(perspective);
    outside.setPerspective(perspective);
    room.setPerspective(perspective);
    painting.setPerspective(perspective);
    lamp.setPerspective(perspective);
  }
  
  private void disposeMeshes(GL3 gl) {
  }

  // Used for when the start button is pressed, goes through the animation sequence.
  private void updateFingers() {   
    if(jGesture) doJGesture(true);
    if(aGesture) doAGesture(true);
    if(mGesture) doMGesture(true);
    if(peaceGesture) doPeaceGesture(true);
  }

  private float[] addFloatArray(float[] a, float[] b){
    float[] c = new float[a.length];
    for(int i = 0; i < a.length; i++){
      c[i] = a[i] + b[i];
    }

    return c;
  }
}