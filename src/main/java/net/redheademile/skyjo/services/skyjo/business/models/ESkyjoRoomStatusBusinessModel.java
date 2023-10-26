package net.redheademile.skyjo.services.skyjo.business.models;

import net.redheademile.skyjo.services.skyjo.data.models.ESkyjoRoomStatusDataModel;

public enum ESkyjoRoomStatusBusinessModel {
    WAITING_FOR_PLAYERS,
    SELECTING_CARDS,
    TURN_IN_PROGRESS,
    INTERRUPTED,
    CRASHED,
    FINISHED;

    public ESkyjoRoomStatusDataModel toDataModel() {
        return switch (this) {
            case WAITING_FOR_PLAYERS -> ESkyjoRoomStatusDataModel.WAITING_FOR_PLAYERS;
            case SELECTING_CARDS -> ESkyjoRoomStatusDataModel.SELECTING_CARD;
            case TURN_IN_PROGRESS -> ESkyjoRoomStatusDataModel.TURN_IN_PROGRESS;
            case INTERRUPTED -> ESkyjoRoomStatusDataModel.INTERRUPTED;
            case CRASHED -> ESkyjoRoomStatusDataModel.CRASHED;
            case FINISHED -> ESkyjoRoomStatusDataModel.FINISHED;
        };
    }

    public static ESkyjoRoomStatusBusinessModel fromDataModel(ESkyjoRoomStatusDataModel dataModel) {
        return switch (dataModel) {
            case WAITING_FOR_PLAYERS -> WAITING_FOR_PLAYERS;
            case SELECTING_CARD -> SELECTING_CARDS;
            case TURN_IN_PROGRESS -> TURN_IN_PROGRESS;
            case INTERRUPTED -> INTERRUPTED;
            case CRASHED -> CRASHED;
            case FINISHED -> FINISHED;
        };
    }
}
