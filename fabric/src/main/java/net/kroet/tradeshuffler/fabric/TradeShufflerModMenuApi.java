package net.kroet.tradeshuffler.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.kroet.tradeshuffler.shared.client.TradeShufflerConfigScreen;

/**
 * Optional Mod Menu integration: opens the shared config screen.
 */
public final class TradeShufflerModMenuApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TradeShufflerConfigScreen::new;
    }
}
