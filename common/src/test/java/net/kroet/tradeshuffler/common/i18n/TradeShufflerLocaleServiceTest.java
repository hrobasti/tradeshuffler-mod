package net.kroet.tradeshuffler.common.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kroet.tradeshuffler.common.facade.LocaleFacade;
import java.util.List;
import org.junit.jupiter.api.Test;

class TradeShufflerLocaleServiceTest {
    @Test
    void resolvesExactLocaleWhenAvailable() {
        String resolved = TradeShufflerLocaleService.resolveSupportedLocale("de_de", List.of("en_US", "de_DE"));
        assertEquals("de_DE", resolved);
    }

    @Test
    void fallsBackToLanguageMatchThenDefault() {
        String languageMatch = TradeShufflerLocaleService.resolveSupportedLocale("de_AT", List.of("en_US", "de_DE"));
        assertEquals("de_DE", languageMatch);

        String defaultLocale = TradeShufflerLocaleService.resolveSupportedLocale("fr_FR", List.of("en_US", "de_DE"));
        assertEquals(LocaleFacade.defaultLocale(), defaultLocale);
    }
}

