package net.kroet.tradeshuffler.common.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import net.kroet.tradeshuffler.common.rules.ShuffleEligibility.Verdict;
import org.junit.jupiter.api.Test;

class TradeShufflerTextKeysTest {
    private static final Path EN_US = Path.of("src/main/resources/assets/tradeshuffler/lang/en_us.json");

    /**
     * Reasons the server can send with a trade state; the others only apply to
     * requests.
     */
    private static final EnumSet<Verdict> STATE_REASONS = EnumSet.of(
            Verdict.SHUFFLE_DISABLED, Verdict.ALREADY_TRADED, Verdict.HAS_XP, Verdict.NOT_NOVICE, Verdict.NO_TRADES, Verdict.NO_JOB_SITE);

    @Test
    void shuffleUnavailableKeyUsesLowerCaseReason() {
        assertEquals("tradeshuffler.screen.shuffle.tooltip.unavailable.no_job_site", TradeShufflerTextKeys.shuffleUnavailable("NO_JOB_SITE"));
    }

    @Test
    void everyStateReasonAndTheFallbackHaveText() throws IOException {
        JsonObject english = JsonParser.parseString(Files.readString(EN_US, StandardCharsets.UTF_8)).getAsJsonObject();

        for (Verdict reason : STATE_REASONS) {
            assertTrue(english.has(TradeShufflerTextKeys.shuffleUnavailable(reason.name())), reason.name());
        }
        assertTrue(english.has(TradeShufflerTextKeys.SCREEN_SHUFFLE_UNAVAILABLE_OTHER));
    }
}
