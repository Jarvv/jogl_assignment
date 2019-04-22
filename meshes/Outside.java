import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import lights.WorldLight;

package meshes;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class Outside extends Mesh {

  private int[] textureId1; 
  private int[] textureId2;
  private int[] textureId3;
  
  public Outside(GL3 gl, int[] textureId1, int[] textureId2, int[] textureId3) {
    super(gl);
    super.vertices = this.vertices;
    super.indices = this.indices;
    this.textureId1 = textureId1;
    this.textureId2 = textureId2;
    this.textureId3 = textureId3;
    material.setAmbient(0.1f, 0.5f, 0.81f);
    material.setDiffuse(0.1f, 0.5f, 0.81f);
    material.setSpecular(0.3f, 0.3f, 0.3f);
    material.setShininess(8.0f);
    shader = new Shader(gl, "./shaders/vs_tt_04.txt", "./shaders/fs_tt_04.txt");
    fillBuffers(gl);
  }

  public void render(GL3 gl, Mat4 model) {
    Mat4 mvpMatrix = Mat4.multiply(perspective, Mat4.multiply(camera.getViewMatrix(), model));
    
    shader.use(gl);
    shader.setFloatArray(gl, "model", model.toFloatArrayForGLSL());
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());    
    shader.setVec3(gl, "viewPos", camera.getPosition());

    // Only want the outside scene to be illuminated by the world light.
    worldLight.applyToShader(shader, gl);

    shader.setInt(gl, "material.diffuse", 0);
    shader.setInt(gl, "material.diffuse", 1);
    shader.setInt(gl, "material.diffuse", 2);

    shader.setFloat(gl, "material.shininess", material.getShininess());

    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId1[0]);
    gl.glActiveTexture(GL.GL_TEXTURE1);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId2[0]);
    gl.glActiveTexture(GL.GL_TEXTURE2);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureId3[0]);

    // Creating the outside scene, do not need the front wall as it wont be seen.
    Mat4 trans = new Mat4(1);
    for(int k = 0; k<5; k++){
      switch(k){
        // Ceiling (Cloudy Sky)
        case 0:
        shader.setInt(gl, "first_texture", 2);
        trans = Mat4Transform.rotateAroundX(180);
        trans = Mat4.multiply(Mat4Transform.translate(0,50,0), trans);
        model = Mat4.multiply(Mat4Transform.scale(50,1,50), trans);
        break;
        // Floor (Grass)
        case 1:
        shader.setInt(gl, "first_texture", 0);
        trans = Mat4Transform.translate(0,-1,0); 
        model = Mat4.multiply(Mat4Transform.scale(50,1,50), trans);
        break;
        // Right wall (Sky)
        case 2: 
        shader.setInt(gl, "first_texture", 1);
        trans = Mat4Transform.rotateAroundX(90);
        trans = Mat4.multiply(Mat4Transform.translate(0,0.95f,-50),trans);
        model = Mat4.multiply(Mat4Transform.scale(50,26,1), trans);
        break;
        // Left Wall (Sky)
        case 3:
        shader.setInt(gl, "first_texture", 1);
        trans = Mat4Transform.rotateAroundX(-90);
        trans = Mat4.multiply(Mat4Transform.translate(0,0.95f,50),trans);
        model = Mat4.multiply(Mat4Transform.scale(50,26,1), trans);
        break;
        // Back Wall (Sky)
        case 4: 
        shader.setInt(gl, "first_texture", 1);
        trans = Mat4Transform.rotateAroundZ(90);
        trans = Mat4.multiply(Mat4Transform.translate(50,0.95f,0),trans);
        model = Mat4.multiply(Mat4Transform.scale(1,26,50), trans);
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
    gl.glDeleteBuffers(1, textureId3, 0);
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