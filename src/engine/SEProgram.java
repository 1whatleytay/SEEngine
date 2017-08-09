package engine;

//Have your program's class implement this to have it be an eligible SEProgram.
//You'll need to pass one of those to SEstart(SEProgram), so be sure to fill out
//the functions well.
public interface SEProgram {
    //Returns an SEProgramData. You can just return a new SEProgramData() and
    //continue on normally, or change something about the SEProgramData to
    //allocate more ressources, enable a feature or even go full screen.
    //Investigate the SEProgramData class for more. Don't use this function
    //for setting up your objects or textures. Avoid calling any non-inquiery SE
    //functions.
    SEProgramData program();
    //Called once as your program is being loaded. It should be safe to load
    //your textures and objects now, so feel free to setup your program here.
    //Call essentially any SE function you want.
    void setup();
    //Called every frame, right before a draw call. Have your program's
    //main behavior reside here. Move objects or check for key presses. It's up
    //to you.
    void update();
}
