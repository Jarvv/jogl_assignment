Classes used from previous exercies:
Camera / CameraMovement / CameraType
Material
MyMouseInput
MyKeyboardInput
TextureLibrary
Mesh (altered to allow for lighting controls for meshes)
MeshNode
NameNode
SGNode
TwoTriangles (altered to allow for lighting)
Cube (altered to allow for lighting)

Modelling:
Room Model - The room is modelled in Room.java. The room also contains a painting and a lamp.
Outside Model - The outside is modlled similar to the toom in Outside.java. It was a prefered to model the outside this way, rather than a texture painted onto the 
                window as it gives the scene more depth and has the potential to be added to with trees/buildings etc.
Hand Model - The hand scene graph is a hierarchical structure made out of 'cubes'. There are TransformNodes nodes between each MeshNode nodes allowing for flexibility
             for the gestures.

Texturing:
Room Model - The room texturing consists of a wooden textured floor and a painted wall and ceiling.
Outside Model - The outside texturing consists of a green grass texture, blue sky and blue sky with cloud texture for the 'ceiling' of the sky.
Hand Model - The hand texturing consists of of a metal and metal specular texture. The SpecularHand mesh uses both these textures to create a new shiny
            metal look for these hand pieces, and the DiffuseHand mesh only contains the diffuse metal texture giving it a worn out look. The ring and gemstone
            uses the jade and jade specular textures to give the effect of jewelery.

Lighting:
SpotLight - The SpotLight is not attatched to the gemstone, instead it is placed below the hand to illuminate it when the other lights are off.
WorldLight - The WorldLight is the general illumination source for all meshes in the scene. If turned off the outside lighting is dark (no sun) and the room and hand are dimmed.
LampLight - The LampLight is a point light in which provides the room with light, it is shown as the lamp but is only part of the shader.

Hand animation:
The way animation has been handled it should be easy enough to edit or add a new animation for the hand. All that
needs to be done is for new animations to be given a new row in the gestureRotations array and assigned values
for their rotations.

Shaders:
fs_lamp.txt - Shader for the lamp 'bulb'.
fs_multiLight.txt - Shader for the hand and room, makes use of all three shaders.
fs_tt_04.txt - Used from previous exercies for the outside scene.