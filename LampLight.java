import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

/* I declare that this code is my own work 
    Author James Harvey jharvey3@sheffield.ac.uk*/
public class LampLight extends Light{

    public boolean lampOn;

    private Vec3[] pointLightPositions = new Vec3[]{
        new Vec3( 7f,10, 7f),
        new Vec3(-7f,10f,-7f)
    };

    public LampLight(){
        this.lampOn = true;
    }

    public void applyToShader(Shader shader, GL3 gl){
        
        shader.setVec3(gl, "lampLight[0].position", pointLightPositions[0]);
        shader.setFloat(gl, "lampLight[0].constant", 1.0f);
        shader.setFloat(gl,"lampLight[0].linear", 0.09f);
        shader.setFloat(gl,"lampLight[0].quadratic", 0.032f);
        
        /*
        shader.setVec3(gl, "lampLight[1].position", pointLightPositions[1]);
        shader.setFloat(gl,"lampLight[1].constant", 1.0f);
        shader.setFloat(gl,"lampLight[1].linear", 0.09f);
        shader.setFloat(gl,"lampLight[1].quadratic", 0.032f);
        */
        if(lampOn){
            shader.setVec3(gl, "lampLight[0].diffuse", new Vec3(0.7f,0.7f,0.7f));
            shader.setVec3(gl, "lampLight[0].ambient", new Vec3(0.8f, 0.8f, 0.8f));       
            shader.setVec3(gl, "lampLight[0].specular", new Vec3(1.0f, 1.0f, 1.0f));

            /*
            shader.setVec3(gl, "lampLight[1].diffuse", new Vec3(0.5f,0.5f,0.5f));
            shader.setVec3(gl, "lampLight[1].ambient", new Vec3(0.8f, 0.8f, 0.8f));       
            shader.setVec3(gl, "lampLight[1].specular", new Vec3(1.0f, 1.0f, 1.0f));
            */
        }else{
            shader.setVec3(gl, "lampLight[0].diffuse", new Vec3(0,0,0));
            shader.setVec3(gl, "lampLight[0].ambient", new Vec3(0,0,0));       
            shader.setVec3(gl, "lampLight[0].specular", new Vec3(0,0,0));

            /*
            shader.setVec3(gl, "lampLight[1].diffuse", new Vec3(0,0,0));
            shader.setVec3(gl, "lampLight[1].ambient", new Vec3(0,0,0));       
            shader.setVec3(gl, "lampLight[1].specular", new Vec3(0,0,0));
            */
        }
    }

}