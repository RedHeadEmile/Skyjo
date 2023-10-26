import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SkyjoService} from "../../services/skyjo.service";

@Component({
  selector: 'skyjo-board',
  templateUrl: './skyjo-board.component.html',
  styleUrls: ['./skyjo-board.component.scss']
})
export class SkyjoBoardComponent {
  @Input() cards: number[] = [];
  @Input() clickAction: 'none' | 'disallowed' | 'allowed' = 'none';

  @Output() onCardClicked: EventEmitter<number> = new EventEmitter<number>();

  isClickAllowed(cardIndex: number) {
    return this.getCardValue(cardIndex) !== '' && this.clickAction === 'allowed';
  }

  isClickDisallowed() {
    return this.clickAction === 'disallowed';
  }

  isCardDeleted(cardIndex: number): boolean {
    if (this.cards.length > cardIndex)
      return this.cards[cardIndex] === SkyjoService.DELETED_CARD;
    return false;
  }

  getCardValue(cardIndex: number): string {
    if (this.isCardDeleted(cardIndex))
      return '';

    if (this.cards.length > cardIndex) {
      const value = this.cards[cardIndex];
      if (value < -2 || value > 12)
        return '?';
      return value.toString();
    }
    return '?';
  }
}
