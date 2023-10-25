package net.redheademile.skyjo.clientapp.models;

import net.redheademile.skyjo.services.skyjo.business.models.ESkyjoGameActionTypeBusinessModel;

public enum ESkyjoGameActionTypeViewModel {
    PICK_A_CARD,
    EXCHANGE_WITH_PICKED_CARD,
    IGNORE_PICKED_CARD,
    EXCHANGE_WITH_DISCARDED_CARD,
    FLIP_A_CARD;

    public ESkyjoGameActionTypeBusinessModel toBusinessModel() {
        return switch (this) {
            case PICK_A_CARD -> ESkyjoGameActionTypeBusinessModel.DRAW_A_CARD;
            case EXCHANGE_WITH_PICKED_CARD -> ESkyjoGameActionTypeBusinessModel.EXCHANGE_WITH_PICKED_CARD;
            case IGNORE_PICKED_CARD -> ESkyjoGameActionTypeBusinessModel.IGNORE_PICKED_CARD;
            case EXCHANGE_WITH_DISCARDED_CARD -> ESkyjoGameActionTypeBusinessModel.EXCHANGE_WITH_DISCARDED_CARD;
            case FLIP_A_CARD -> ESkyjoGameActionTypeBusinessModel.FLIP_A_CARD;
        };
    }
}
