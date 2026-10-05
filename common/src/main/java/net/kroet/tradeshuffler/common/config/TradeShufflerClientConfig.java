package net.kroet.tradeshuffler.common.config;

/**
 * Loader-neutral client config model ({@code config/tradeshuffler.json}).
 * Values are normalized on construction, so every instance is valid.
 *
 * @param shuffleButtonEnabled
 *            whether the shuffle button is shown in the merchant screen
 * @param shuffleButtonOffsetX
 *            horizontal pixel offset of the shuffle button from its default
 *            position
 * @param shuffleButtonOffsetY
 *            vertical pixel offset of the shuffle button from its default
 *            position
 * @param previewEnabled
 *            whether future-level trades are shown in the merchant screen
 * @param previewDimAlpha
 *            opacity of the darkening overlay on preview rows, in percent
 * @param previewLevelStripe
 *            whether preview rows get a level-colored stripe
 * @param updateCheckEnabled
 *            whether an update check runs on game start
 * @param updateChannel
 *            release channel used by the update check
 */
public record TradeShufflerClientConfig(
        boolean shuffleButtonEnabled,
        int shuffleButtonOffsetX,
        int shuffleButtonOffsetY,
        boolean previewEnabled,
        int previewDimAlpha,
        boolean previewLevelStripe,
        boolean updateCheckEnabled,
        UpdateChannel updateChannel) {
    public static final int MIN_BUTTON_OFFSET = -64;
    public static final int MAX_BUTTON_OFFSET = 64;
    public static final int MIN_PREVIEW_DIM_ALPHA = 0;
    public static final int MAX_PREVIEW_DIM_ALPHA = 100;
    public static final int DEFAULT_PREVIEW_DIM_ALPHA = 45;

    public TradeShufflerClientConfig {
        shuffleButtonOffsetX = clampButtonOffset(shuffleButtonOffsetX);
        shuffleButtonOffsetY = clampButtonOffset(shuffleButtonOffsetY);
        previewDimAlpha = clampPreviewDimAlpha(previewDimAlpha);
        updateChannel = updateChannel == null ? UpdateChannel.BETA : updateChannel;
    }

    public static TradeShufflerClientConfig defaults() {
        return new TradeShufflerClientConfig(true, 0, 0, true, DEFAULT_PREVIEW_DIM_ALPHA, true, true, UpdateChannel.BETA);
    }

    public static int clampButtonOffset(int offset) {
        return Math.max(MIN_BUTTON_OFFSET, Math.min(MAX_BUTTON_OFFSET, offset));
    }

    public static int clampPreviewDimAlpha(int alpha) {
        return Math.max(MIN_PREVIEW_DIM_ALPHA, Math.min(MAX_PREVIEW_DIM_ALPHA, alpha));
    }
}
