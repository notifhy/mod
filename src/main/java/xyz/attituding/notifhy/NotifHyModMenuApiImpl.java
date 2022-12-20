package xyz.attituding.notifhy;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.ClothConfigDemo;

// https://github.com/TerraformersMC/ModMenu/wiki/API
// https://shedaniel.gitbook.io/cloth-config/
// https://github.com/shedaniel/cloth-config/blob/v8/fabric/src/main/java/me/shedaniel/clothconfig2/fabric/ClothConfigModMenuDemo.java
// https://github.com/shedaniel/cloth-config/blob/v8/common/src/main/java/me/shedaniel/clothconfig2/ClothConfigDemo.java

public class NotifHyModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            return ClothConfigDemo.getConfigBuilderWithDemo().setParentScreen(screen).build();
        };
    }
}