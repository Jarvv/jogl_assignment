import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.sun.javafx.geom.Vec3d;

package lights;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class SpotLight extends Light{
    
    public boolean spotLightOn;
    public Vec3 position;
    public Vec3 direction; 

    public SpotLight(){
        this.spotLightOn = true;
    }
    
    public void applyToShader(Shader shader, GL3 gl){

        // Light Unifroms for a spotlight
        shader.setVec3(gl, "spotLight.direction", new Vec3(0.5f,-1,0f));
        shader.setVec3(gl, "spotLight.position", new Vec3(2,-1,2));
        shader.setVec3(gl, "spotLight.ambient", new Vec3(0.1f,0.1f,0.1f));
        shader.setVec3(gl, "spotLight.diffuse", new Vec3(1,1,1));
        shader.setVec3(gl, "spotLight.specular", new Vec3(1,1,1));
        shader.setFloat(gl,"spotLight.cutOff", 0.95f);
        shader.setFloat(gl,"spotLight.outerCutOff", 0.97f);
    }

}