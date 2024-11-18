package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class TAR_modPlugin extends BaseModPlugin {
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        //Check ShaderLib for lights
        try {  
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");  
            ShaderLib.init();  
            TextureData.readTextureDataCSV("data/config/modFiles/TAR_texture_data.csv"); 
        } catch (ClassNotFoundException ex) {
        }
    }
}
