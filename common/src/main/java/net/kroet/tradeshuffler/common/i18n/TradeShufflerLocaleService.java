package net.kroet.tradeshuffler.common.i18n;

import net.kroet.tradeshuffler.common.facade.LocaleFacade;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Locale helpers for TradeShuffler based on TurtleLib LangLoader.
 */
public final class TradeShufflerLocaleService {
    private TradeShufflerLocaleService() {
    }

    public static String resolveSupportedLocale(String gameLocaleTag, List<String> supportedLocales) {
        return LocaleFacade.resolveSupportedLocale(gameLocaleTag, supportedLocales);
    }

    public static Properties loadMessages(Class<?> resourceAnchor, Path langDir, String gameLocaleTag) {
        return LocaleFacade.loadMessages(resourceAnchor, langDir, gameLocaleTag);
    }

    public static String normalizeGameLocale(String gameLocaleTag) {
        return LocaleFacade.normalizeGameLocale(gameLocaleTag);
    }
}

