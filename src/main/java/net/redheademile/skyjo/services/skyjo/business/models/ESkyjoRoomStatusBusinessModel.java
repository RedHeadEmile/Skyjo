package net.redheademile.skyjo.services.skyjo.business.models;

import net.redheademile.skyjo.services.skyjo.data.models.ESkyjoRoomStatusDataModel;

public enum ESkyjoRoomStatusBusinessModel {
    WAITING_FOR_PLAYERS,
    SELECTING_CARDS_PHASE,
    TURNS_IN_PROGRESS,
    FINISHED;

    public ESkyjoRoomStatusDataModel toDataModel() {
        return switch (this) {
            case WAITING_FOR_PLAYERS -> ESkyjoRoomStatusDataModel.WAITING_FOR_PLAYERS;
            case SELECTING_CARDS_PHASE -> ESkyjoRoomStatusDataModel.SELECTING_CARDS_PHASE;
            case TURNS_IN_PROGRESS -> ESkyjoRoomStatusDataModel.TURNS_IN_PROGRESS;
            case FINISHED -> ESkyjoRoomStatusDataModel.FINISHED;
        };
    }

    public static ESkyjoRoomStatusBusinessModel fromDataModel(ESkyjoRoomStatusDataModel dataModel) {
        return switch (dataModel) {
            case WAITING_FOR_PLAYERS -> WAITING_FOR_PLAYERS;
            case SELECTING_CARDS_PHASE -> SELECTING_CARDS_PHASE;
            case TURNS_IN_PROGRESS -> TURNS_IN_PROGRESS;
            case FINISHED -> FINISHED;
        };
    }
}
