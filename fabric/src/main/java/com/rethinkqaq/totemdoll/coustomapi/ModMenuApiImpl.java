package com.rethinkqaq.totemdoll.coustomapi;

import com.rethinkqaq.totemdoll.client.gui.DollSelectionScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DollSelectionScreen::new;
    }
}
