package milk.revivalblock;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class RevivalBlock extends PluginBase{

    @Override
    public void onEnable(){
        this.getServer().getLogger().info(TextFormat.GOLD + "[RevivalBlock]Plugin has been enabled");
    }

    @Override
    public void onDisable(){
        this.getServer().getLogger().info(TextFormat.GOLD + "[RevivalBlock]Plugin has been disabled");
    }

}
