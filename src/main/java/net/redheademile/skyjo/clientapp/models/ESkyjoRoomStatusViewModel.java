package net.redheademile.skyjo.clientapp.models;

import net.redheademile.skyjo.services.skyjo.business.models.ESkyjoRoomStatusBusinessModel;

public enum ESkyjoRoomStatusViewModel {
    WAITING_FOR_PLAYERS,
    SELECTING_CARDS,
    TURN_IN_PROGRESS,
    INTERRUPTED,
    CRASHED,
    FINISHED;

    public ESkyjoRoomStatusBusinessModel toBusinessModel() {
        return switch (this) {
            case WAITING_FOR_PLAYERS -> ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS;
            case SELECTING_CARDS -> ESkyjoRoomStatusBusinessModel.SELECTING_CARDS;
            case TURN_IN_PROGRESS -> ESkyjoRoomStatusBusinessModel.TURN_IN_PROGRESS;
            case INTERRUPTED -> ESkyjoRoomStatusBusinessModel.INTERRUPTED;
            case CRASHED -> ESkyjoRoomStatusBusinessModel.CRASHED;
            case FINISHED -> ESkyjoRoomStatusBusinessModel.FINISHED;
        };
    }

    public static ESkyjoRoomStatusViewModel fromBusinessModel(ESkyjoRoomStatusBusinessModel businessModel) {
        return switch (businessModel) {
            case WAITING_FOR_PLAYERS -> WAITING_FOR_PLAYERS;
            case SELECTING_CARDS -> SELECTING_CARDS;
            case TURN_IN_PROGRESS -> TURN_IN_PROGRESS;
            case INTERRUPTED -> INTERRUPTED;
            case CRASHED -> CRASHED;
            case FINISHED -> FINISHED;
        };
    }
}
