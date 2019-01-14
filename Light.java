import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public abstract class Light {
  public abstract void applyToShader(Shader shader, GL3 gl);
}