package net.kroet.tradeshuffler.common.core;

import java.util.Locale;

/**
 * Translation keys used from code. All UI text is resolved on the client from
 * the bundled {@code assets/tradeshuffler/lang/*.json} files.
 */
public final class TradeShufflerTextKeys {
    public static final String SCREEN_SHUFFLE_TOOLTIP = "tradeshuffler.screen.shuffle.tooltip";
    public static final String SCREEN_PREVIEW_UNLOCKS_AT = "tradeshuffler.screen.preview.unlocks_at";
    public static final String SCREEN_PREVIEW_BASE_PRICE = "tradeshuffler.screen.preview.base_price";
    /** Fallback for block reasons without their own text. */
    public static final String SCREEN_SHUFFLE_UNAVAILABLE_OTHER = "tradeshuffler.screen.shuffle.tooltip.unavailable.other";
    public static final String UPDATE_AVAILABLE = "tradeshuffler.update.available";
    public static final String UPDATE_PROVIDER_OK = "tradeshuffler.update.provider.ok";
    public static final String UPDATE_PROVIDER_ERROR = "tradeshuffler.update.provider.error";

    private TradeShufflerTextKeys() {
    }

    /**
     * Text key for a shuffle block reason sent by the server, e.g.
     * {@code NO_JOB_SITE}.
     */
    public static String shuffleUnavailable(String reason) {
        return "tradeshuffler.screen.shuffle.tooltip.unavailable." + reason.toLowerCase(Locale.ROOT);
    }
}
