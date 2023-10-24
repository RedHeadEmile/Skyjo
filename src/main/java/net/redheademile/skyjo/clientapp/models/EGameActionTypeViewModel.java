package net.redheademile.skyjo.clientapp.models;

import net.redheademile.skyjo.services.skyjo.business.models.EGameActionTypeBusinessModel;

public enum EGameActionTypeViewModel {
    DRAW_A_CARD,
    FLIP_A_CARD,
    EXCHANGE_A_CARD;

    public EGameActionTypeBusinessModel toBusinessModel() {
        return switch (this) {
            case DRAW_A_CARD -> EGameActionTypeBusinessModel.DRAW_A_CARD;
            case FLIP_A_CARD -> EGameActionTypeBusinessModel.FLIP_A_CARD;
            case EXCHANGE_A_CARD -> EGameActionTypeBusinessModel.EXCHANGE_A_CARD;
        };
    }
}
