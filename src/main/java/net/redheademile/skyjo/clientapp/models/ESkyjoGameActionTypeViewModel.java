package net.redheademile.skyjo.clientapp.models;

import net.redheademile.skyjo.services.skyjo.business.models.ESkyjoGameActionTypeBusinessModel;

public enum ESkyjoGameActionTypeViewModel {
    DRAW_A_CARD,
    EXCHANGE_WITH_DRAWN_CARD,
    IGNORE_DRAWN_CARD,
    EXCHANGE_WITH_DISCARDED_CARD,
    FLIP_A_CARD;

    public ESkyjoGameActionTypeBusinessModel toBusinessModel() {
        return switch (this) {
            case DRAW_A_CARD -> ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD;
            case EXCHANGE_WITH_DRAWN_CARD -> ESkyjoGameActionTypeBusinessModel.EXCHANGE_WITH_DRAWN_CARD;
            case IGNORE_DRAWN_CARD -> ESkyjoGameActionTypeBusinessModel.IGNORE_DRAWN_CARD;
            case EXCHANGE_WITH_DISCARDED_CARD -> ESkyjoGameActionTypeBusinessModel.EXCHANGE_WITH_DISCARDED_CARD;
            case FLIP_A_CARD -> ESkyjoGameActionTypeBusinessModel.FLIP_A_CARD;
        };
    }
}
