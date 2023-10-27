package net.redheademile.skyjo.services.skyjo.business.models;

import net.redheademile.skyjo.services.skyjo.data.models.ESkyjoGameActionTypeDataModel;

public enum ESkyjoGameActionTypeBusinessModel {
    DRAW_A_CARD,
    EXCHANGE_WITH_DRAWN_CARD,
    IGNORE_DRAWN_CARD,
    EXCHANGE_WITH_DISCARDED_CARD,
    FLIP_A_CARD;

    public ESkyjoGameActionTypeDataModel toDataModel() {
        return switch (this) {
            case DRAW_A_CARD -> ESkyjoGameActionTypeDataModel.DRAW_A_CARD;
            case EXCHANGE_WITH_DRAWN_CARD -> ESkyjoGameActionTypeDataModel.EXCHANGE_WITH_DRAWN_CARD;
            case IGNORE_DRAWN_CARD -> ESkyjoGameActionTypeDataModel.IGNORE_DRAWN_CARD;
            case EXCHANGE_WITH_DISCARDED_CARD -> ESkyjoGameActionTypeDataModel.EXCHANGE_WITH_DISCARDED_CARD;
            case FLIP_A_CARD -> ESkyjoGameActionTypeDataModel.FLIP_A_CARD;
        };
    }

    public static ESkyjoGameActionTypeBusinessModel fromDataModel(ESkyjoGameActionTypeDataModel dataModel) {
        return switch (dataModel) {
            case DRAW_A_CARD -> DRAW_A_CARD;
            case EXCHANGE_WITH_DRAWN_CARD -> EXCHANGE_WITH_DRAWN_CARD;
            case IGNORE_DRAWN_CARD -> IGNORE_DRAWN_CARD;
            case EXCHANGE_WITH_DISCARDED_CARD -> EXCHANGE_WITH_DISCARDED_CARD;
            case FLIP_A_CARD -> FLIP_A_CARD;
        };
    }
}
