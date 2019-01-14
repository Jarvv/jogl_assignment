import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class WorldLight extends Light{
    
    public boolean worldLightOn;

    public WorldLight(){
        this.worldLightOn = true;
    }

    public void applyToShader(Shader shader, GL3 gl){
        shader.setVec3(gl, "worldLight.direction", new Vec3(-0.2f, -1.0f, -0.3f));

        if(worldLightOn){
            shader.setVec3(gl, "worldLight.ambient", new Vec3(0.05f, 0.05f, 0.05f));
            shader.setVec3(gl, "worldLight.diffuse", new Vec3(0.4f, 0.4f, 0.4f));
            shader.setVec3(gl, "worldLight.specular", new Vec3(0.5f, 0.5f, 0.5f));
        }else{
            shader.setVec3(gl, "worldLight.ambient", new Vec3(0f, 0f, 0f));
            shader.setVec3(gl, "worldLight.diffuse", new Vec3(0f, 0f, 0f));
            shader.setVec3(gl, "worldLight.specular", new Vec3(0f, 0f, 0f));
        }
    }

}