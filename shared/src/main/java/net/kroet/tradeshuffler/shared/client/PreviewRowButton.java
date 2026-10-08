package net.kroet.tradeshuffler.shared.client;

/**
 * Implemented by vanilla's trade offer buttons of the merchant screen (via
 * {@code MerchantScreenTradeOfferButtonMixin}), so the screen can find them
 * among its widgets; the button class itself is private.
 */
public interface PreviewRowButton {
    /** Row on screen (0 to 6); add the scroll offset for the list index. */
    int tradeshuffler$rowIndex();
}
