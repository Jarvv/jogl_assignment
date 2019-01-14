import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class Room extends Mesh {

  private int[] textureId1; 
  private int[] textureId2;
  
  public Room(GL3 gl, int[] textureId1, int[] textureId2) {
    super(gl);
    super.vertices = this.vertices;
    super.indices = this.indices;
    this.textureId1 = textureId1;
    this.textureId2 = textureId2;
    material.setAmbient(0.5f, 0.5f, 0.5f);
    material.setDiffuse(0.5f, 0.5f, 0.5f);
    material.setSpecular(1f, 1f, 1f);
    material.setShininess(32.0f);
    shader = new Shader(gl, "vs_multiLight.txt", "fs_multiLight.txt");
    fillBuffers(gl);
  }
  
  public void render(GL3 gl, Mat4 model) {
    Mat4 mvpMatrix = Mat4.multiply(perspective, Mat4.multiply(camera.getViewMatrix(), model));

    shader.use(gl);
    shader.setFloatArray(gl, "model", model.toFloatArrayForGLSL());
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());
    
    shader.setVec3(gl, "viewPos", camera.getPosition());

    worldLight.applyToShader(shader, gl);
    lampLight.applyToShader(shader, gl);
    spotLight.applyToShader(shader, gl);

    shader.setVec3(gl, "material.diffuse", material.getDiffuse());
    shader.setVec3(gl, "material.specular", material.getSpecular());
    shader.setFloat(gl, "material.shininess", material.getShininess());

    shader.setInt(gl, "material.diffuse", 0);
    shader.setInt(gl, "material.diffuse", 1);

    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId1[0]);
    gl.glActiveTexture(GL.GL_TEXTURE1);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId2[0]);
    
    // Creating the room
    Mat4 trans = new Mat4(1);
    for(int k = 0; k<6; k++){
      switch(k){
        // Ceiling
        case 0:
        shader.setInt(gl, "first_texture", 1);
        // Create the transformation Mat4 that the model will take.
        trans = Mat4Transform.rotateAroundX(180);
        trans = Mat4.multiply(Mat4Transform.translate(0,10,0), trans);
        model = Mat4.multiply(Mat4Transform.scale(9,1,9), trans);
        break;
        // Floor
        case 1:
        shader.setInt(gl, "first_texture", 0);
        model = Mat4Transform.scale(9,1,9);  
        break;
        // Right wall
        case 2: 
        shader.setInt(gl, "first_texture", 1);
        trans = Mat4Transform.rotateAroundX(90);
        trans = Mat4.multiply(Mat4Transform.translate(0,1,-9),trans);
        model = Mat4.multiply(Mat4Transform.scale(9,5,1), trans);
        break;
        // Left Wall
        case 3:
        shader.setInt(gl, "first_texture", 1);
        trans = Mat4Transform.rotateAroundX(-90);
        trans = Mat4.multiply(Mat4Transform.translate(0,1,9),trans);
        model = Mat4.multiply(Mat4Transform.scale(9,5,1), trans);
        break;
        // Back wall (wall with window)
        case 4:
        for (int i=-4; i<5; ++i) {
          for (int j=0; j<5; ++j) {
            // Leave out middle spaces to create the 'window'
            if(k == 4 && (i > -2 && i < 2) && (j > -0 && j < 3)){
            }
            else{
              shader.setInt(gl, "first_texture", 1);
              trans = Mat4Transform.rotateAroundZ(90);
              trans = Mat4.multiply(Mat4Transform.translate(9,1,0),trans);
              model = Mat4Transform.translate(0, 2*j, 2*i);
              model = Mat4.multiply(model, trans);
              mvpMatrix = Mat4.multiply(perspective, Mat4.multiply(camera.getViewMatrix(), model));
              shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());    
            
              gl.glBindVertexArray(vertexArrayId[0]);
              gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
              gl.glBindVertexArray(0);
            }
          }
        }
        break;
        // Front wall
        case 5:
        shader.setInt(gl, "first_texture", 1);
        trans = Mat4Transform.rotateAroundZ(-90);
        trans = Mat4.multiply(Mat4Transform.translate(-9,1,0),trans);
        model = Mat4.multiply(Mat4Transform.scale(1,5,9), trans);          
        break;
      }

      mvpMatrix = Mat4.multiply(perspective, Mat4.multiply(camera.getViewMatrix(), model));
      shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());

      gl.glBindVertexArray(vertexArrayId[0]);
      gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
      gl.glBindVertexArray(0);
    }
  }

  public void dispose(GL3 gl) {
    super.dispose(gl);
    gl.glDeleteBuffers(1, textureId1, 0);
    gl.glDeleteBuffers(1, textureId2, 0);
  }
  
  // ***************************************************
  /* THE DATA
   */
  // anticlockwise/counterclockwise ordering
  
  private float[] vertices = {      // position, colour, tex coords
    -1f, 0.0f, -1f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f,  // top left
    -1f, 0.0f,  1f,  0.0f, 1.0f, 0.0f,  0.0f, 0.0f,  // bottom left
     1f, 0.0f,  1f,  0.0f, 1.0f, 0.0f,  1.0f, 0.0f,  // bottom right
     1f, 0.0f, -1f,  0.0f, 1.0f, 0.0f,  1.0f, 1.0f   // top right
  };
  
  private int[] indices = {         // Note that we start from 0!
      0, 1, 2,
      0, 2, 3
  };

}