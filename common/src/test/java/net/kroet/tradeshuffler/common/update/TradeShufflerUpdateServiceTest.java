package net.kroet.tradeshuffler.common.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.kroet.tradeshuffler.common.facade.UpdateFacade.UpdateCheckResult;
import net.kroet.tradeshuffler.common.update.TradeShufflerUpdateService.UpdateSources;
import org.junit.jupiter.api.Test;

class TradeShufflerUpdateServiceTest {
    private static final UpdateSources SOURCES = new UpdateSources(
            "https://modrinth.com/project/tradeshuffler-mod",
            "tradeshuffler-mod",
            "https://www.curseforge.com/minecraft/mc-mods/tradeshuffler-mod",
            "1487139");

    @Test
    void keepsReleaseUrlsAndFallsBackToProjectPage() {
        UpdateCheckResult result = new UpdateCheckResult(
                "2.0.0",
                "2.1.0",
                Map.of(
                        UpdateProvider.MODRINTH, new ProviderRelease("2.1.0", "https://modrinth.com/project/p/version/v"),
                        UpdateProvider.CURSEFORGE, new ProviderRelease("2.1.0", null)),
                Map.of());

        TradeShufflerUpdateService.UpdateStatus status = TradeShufflerUpdateService.toStatus(result, SOURCES);

        assertTrue(status.hasUpdate());
        assertEquals("https://modrinth.com/project/p/version/v", status.releases().get(UpdateProvider.MODRINTH).url());
        assertEquals(
                "https://www.curseforge.com/minecraft/mc-mods/tradeshuffler-mod",
                status.releases().get(UpdateProvider.CURSEFORGE).url());
    }

    @Test
    void failedPlatformIsReportedWithoutRelease() {
        UpdateCheckResult result = new UpdateCheckResult(
                "2.0.0",
                "2.1.0",
                Map.of(UpdateProvider.MODRINTH, new ProviderRelease("2.1.0", null)),
                Map.of(UpdateProvider.CURSEFORGE, "IOException: HTTP 503"));

        TradeShufflerUpdateService.UpdateStatus status = TradeShufflerUpdateService.toStatus(result, SOURCES);

        assertFalse(status.releases().containsKey(UpdateProvider.CURSEFORGE));
        assertEquals("IOException: HTTP 503", status.errors().get(UpdateProvider.CURSEFORGE));
    }

    @Test
    void noLatestVersionMeansNoUpdate() {
        UpdateCheckResult result = new UpdateCheckResult("2.0.0", null, Map.of(), Map.of());

        assertFalse(TradeShufflerUpdateService.toStatus(result, SOURCES).hasUpdate());
        assertFalse(TradeShufflerUpdateService.UpdateStatus.notConfigured().hasUpdate());
    }
}
