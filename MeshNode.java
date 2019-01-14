import com.jogamp.opengl.*;

public class MeshNode extends SGNode {

  protected Mesh mesh;

  public MeshNode(String name, Mesh m) {
    super(name);
    mesh = m; 
  }

  public void draw(GL3 gl) {
    mesh.render(gl, worldTransform);
    for (int i=0; i<children.size(); i++) {
      children.get(i).draw(gl);
    }
  }

}