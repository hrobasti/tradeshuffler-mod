package net.kroet.tradeshuffler.shared.client;

/**
 * Implemented by the merchant screen (via {@code MerchantScreenMixin}) so
 * screen-independent hooks can reach its shuffle button.
 */
public interface ShuffleButtonHost {
    /** Updates visibility, state and position of the shuffle button. */
    void tradeshuffler$tickShuffleButton();

    /** Sends a shuffle request if the button is currently usable. */
    boolean tradeshuffler$tryShuffle();
}
