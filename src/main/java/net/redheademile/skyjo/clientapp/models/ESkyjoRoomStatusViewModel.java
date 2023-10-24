package net.redheademile.skyjo.clientapp.models;

import net.redheademile.skyjo.services.skyjo.business.models.ESkyjoRoomStatusBusinessModel;

public enum ESkyjoRoomStatusViewModel {
    WAITING_FOR_PLAYERS,
    SELECTING_CARDS_PHASE,
    TURNS_IN_PROGRESS,
    FINISHED;

    public ESkyjoRoomStatusBusinessModel toBusinessModel() {
        return switch (this) {
            case WAITING_FOR_PLAYERS -> ESkyjoRoomStatusBusinessModel.WAITING_FOR_PLAYERS;
            case SELECTING_CARDS_PHASE -> ESkyjoRoomStatusBusinessModel.SELECTING_CARDS_PHASE;
            case TURNS_IN_PROGRESS -> ESkyjoRoomStatusBusinessModel.TURNS_IN_PROGRESS;
            case FINISHED -> ESkyjoRoomStatusBusinessModel.FINISHED;
        };
    }

    public static ESkyjoRoomStatusViewModel fromBusinessModel(ESkyjoRoomStatusBusinessModel businessModel) {
        return switch (businessModel) {
            case WAITING_FOR_PLAYERS -> WAITING_FOR_PLAYERS;
            case SELECTING_CARDS_PHASE -> SELECTING_CARDS_PHASE;
            case TURNS_IN_PROGRESS -> TURNS_IN_PROGRESS;
            case FINISHED -> FINISHED;
        };
    }
}
